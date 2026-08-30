# Bulk Import & Evidence Upload — Backend Changes and UI Integration Guide

This document summarizes the backend changes made for CSV bulk import and Risk Response & Treatment supporting evidence upload, and describes how the UI should interact with the new/updated endpoints. Share this with the frontend/UI developer.

---

## Summary of backend changes

1. CSV bulk-import scaffold (CSV-only)
   - New controller: ermorg.erm.controller.BulkImportController
   - New service: ermorg.erm.service.IBulkImportService and implementation BulkImportService
   - DTOs: under ermorg.erm.dto.bulk (BulkImportRequest, BulkImportUploadResponse, BulkImportSummary, BulkImportValidationError)
   - Template generation uses SystemTable/SystemField metadata (IFieldService) when available. Falls back to a generic template with required headers (e.g., organizationName, companyName).
   - Validation rules: CSV-only, required fields, numeric parsing for numeric fields, and dropdown option checks using field metadata.
   - Processing: synchronous, creates Company rows (skips duplicates by name within organization). Implementation is intentionally lightweight and synchronous.

2. Supporting evidence (Risk Response & Treatment)
   - Evidence endpoints added to RiskTreatmentController and wired to existing storage module (ermorg.storage.*). These endpoints reuse the project's S3-backed DocumentStorageService and Document DTOs from the storage module.
   - Endpoints support upload (multipart/form-data), list, download, and delete operations for supporting evidence related to Risk Treatment records.

3. DTO mapping fixes for UI clarity
   - KPA/KPI and KRI/KPI response DTOs updated to expose human-friendly fields:
     - ownerName / riskOwnerName
     - evaluationByName / kriEvaluationByName
     - departmentName (prefer businessFunction, fallback to stakeholderDepartments)
     - unitOfMeasurement now prefers the enum label from RiskValueUnit when present (e.g., "Million")
   - These fixes address UI mapping issues where only IDs or raw internal values were shown.

4. Small repo/service changes
   - CompanyRepository: added findByNameAndOrganizationIdAndDeletedFalse(...) to assist import duplicate checks.

---

## New / Updated API endpoints (high-level)

Note: replace `{base}` with the backend base path (e.g., `/erm`).

1. Bulk import

- GET {base}/admin/bulk-import/template?tableName={table}
  - Purpose: Download CSV template for a named system table (metadata-driven headers when available).
  - Response: `text/csv` file download. Filename: `<table>-template.csv`.
  - UI: Trigger download when developer selects "Download template". No auth specifics in this doc — follow existing admin auth flows.

- POST {base}/admin/bulk-import/validate
  - Purpose: Validate a CSV file against template/metadata
  - Request: multipart/form-data with field `file` (CSV) and JSON param(s) in form field `request` or other as implemented (see controller for exact param names). Use the existing pattern used elsewhere in the app if needed.
  - Response: JSON `BulkImportUploadResponse` containing:
    - `valid` (boolean)
    - `errors` (list of row-level BulkImportValidationError entries with row numbers and messages)
    - `summary` (counts of rows, invalid rows)
  - UI: Show validation errors with row numbers, and allow user to fix CSV before processing.

- POST {base}/admin/bulk-import/process
  - Purpose: Process an already-validated CSV (or process directly after validation depending on UI flow)
  - Request: similar to validate endpoint; can accept CSV file and additional params (see controller)
  - Response: `BulkImportSummary` with counts of created/updated/skipped rows and any non-blocking messages.
  - UI: After successful validation, show Process button which calls this endpoint; present the summary on completion.

2. Supporting evidence (Risk Treatment)

- POST {base}/risk-treatment/{id}/evidence
  - Purpose: Upload a supporting evidence file for a Risk Response/Treatment id.
  - Request: multipart/form-data with `file` and optional `description`/`metadata`. Endpoint attaches file metadata and stores object in configured S3 bucket via DocumentStorageService.
  - Response: Document DTO (from storage module) or app-specific small wrapper containing `id`, `path`, `name`, `size`, `uploadedBy`, `uploadedAt`.
  - UI: Use file input; show progress upload indicator. When upload completes, refresh evidence list for the treatment record.

- GET {base}/risk-treatment/{id}/evidence
  - Purpose: List supporting evidence documents for a treatment record.
  - Response: JSON array of Document DTOs with `id`, `name`, `path`, `uploadedBy`, `uploadedAt`, `size`.
  - UI: Render as list with download and delete actions.

- GET {base}/risk-treatment/{id}/evidence/{docId}/download
  - Purpose: Download file content.
  - Response: file stream with proper content-type and content-disposition.
  - UI: Trigger file download in browser (link or programmatic download).

- DELETE {base}/risk-treatment/{id}/evidence/{docId}
  - Purpose: Remove the evidence pointer and delete from storage (if implemented).
  - Response: success status (204 or JSON confirmation).
  - UI: Confirm delete modal before calling.

---

## Expected request/response examples (simplified)

1. Validate CSV (example request)

- Request (multipart/form-data):
  - file: companies.csv (CSV file)
  - optionally: organizationId

- Response (application/json):
{
  "valid": false,
  "errors": [
    { "row": 3, "messages": ["companyName is required", "revenue must be numeric"] },
    { "row": 7, "messages": ["industry not in allowed options: [Banking, Insurance]"] }
  ],
  "summary": { "totalRows": 10, "validRows": 8, "invalidRows": 2 }
}

2. Process CSV (example response)

{
  "created": 8,
  "skipped": 1,
  "updated": 0,
  "errors": []
}

3. Evidence upload (example response)

{
  "id": 123,
  "name": "invoice.pdf",
  "path": "s3://bucket/path/2026/08/29/invoice.pdf",
  "size": 42581,
  "uploadedBy": 45,
  "uploadedAt": "2026-08-29T16:00:00Z"
}

---

## UI developer notes and recommendations

1. CSV template
   - Use the template endpoint to provide a pre-filled header that matches server-side expected columns (avoids header mismatch errors).
   - Ensure the CSV is UTF-8 encoded without BOM (server-side parsing is strict). The backend removes stray BOMs in implementation but avoid it.

2. Validation UI
   - Display row-level errors in a table with an option to download the error report (or re-download a template with invalid rows flagged).
   - For dropdown fields tied to SystemField metadata, consider fetching field options from the existing metadata endpoints the app already provides (IFieldService-backed endpoints). This allows client-side validation before upload.

3. File uploads
   - Use a resumable / chunked upload UI only if files are expected very large. The backend reuses S3 client and currently expects standard multipart uploads.
   - Show file size, upload progress, and a user-friendly name. After upload, call list endpoint to refresh attachments.

4. Error handling
   - For validation errors: show the row number, field(s), and message(s). Allow user to correct CSV and re-upload.
   - For processing errors: show summary and allow retry for failed rows.

5. Permissions & RBAC
   - Bulk import endpoints are intended for system-admin users. Ensure the frontend calls these only from admin screens and respects access tokens/headers in existing app flows.

6. Backward compatibility
   - The DTOs for KPA/KPI and KRI/KPI were extended with display-friendly fields (ownerName, evaluationByName, departmentName, unitOfMeasurement label). These are additive fields and should be consumed by the UI in place of raw id fields where present.
   - If the UI previously used `ownerId` or `kpiEvaluationBy`, prefer to show the new `ownerName` / `kriEvaluationByName` fields in lists and detail views.

---

## Files changed (for reviewer)
- ERM_backend/src/main/java/ermorg/erm/controller/BulkImportController.java (new)
- ERM_backend/src/main/java/ermorg/erm/service/IBulkImportService.java (new)
- ERM_backend/src/main/java/ermorg/erm/serviceimpl/BulkImportService.java (new)
- ERM_backend/src/main/java/ermorg/erm/dto/bulk/* (new DTOs)
- ERM_backend/src/main/java/ermorg/erm/serviceimpl/KpaKpiReviewService.java (modified)
- ERM_backend/src/main/java/ermorg/erm/dto/response/KpaKpiReviewResponseDTO.java (modified)
- ERM_backend/src/main/java/ermorg/erm/dto/response/KriKpiReviewResponseDTO.java (modified)
- ERM_backend/src/main/java/ermorg/erm/repository/CompanyRepository.java (modified)
- ERM_backend/src/main/java/ermorg/erm/controller/RiskTreatmentController.java (modified - evidence endpoints)
- ERM_backend/src/main/java/ermorg/erm/serviceimpl/RiskTreatmentService.java (modified - uses storage module APIs)

---

## Next actions & follow-ups
- Residual Risk Rating mapping: still pending — will add mapping fixes to ensure residual risk rating shows up in risk review/assessment APIs (can implement on request).
- Background processing: consider converting CSV processing to an asynchronous job with progress tracking for large datasets.
- Additional tests: add unit/integration tests for bulk import validation and evidence endpoints.

If you'd like, create a short API spec (OpenAPI snippet) for the new endpoints or a UI mock showing how the import flow and evidence UI should look. I can also open a pull request with this doc included.

---

(End of document)
