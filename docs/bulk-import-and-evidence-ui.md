# Bulk Import & Evidence Upload - Backend Changes and UI Integration Guide

This document summarizes the backend changes for CSV bulk import and Risk Response & Treatment supporting evidence upload, and describes how the UI should call the new/updated endpoints.

---

## Summary of backend changes

1. CSV bulk-import scaffold (CSV-only)
   - Controller: `ermorg.erm.controller.BulkImportController`
   - Service: `ermorg.erm.service.IBulkImportService` and `ermorg.erm.serviceimpl.BulkImportService`
   - DTOs: `ermorg.erm.dto.bulk.BulkImportRequest`, `BulkImportUploadResponse`, `BulkImportSummary`, `BulkImportValidationError`
   - Template generation uses `SystemTable` / `SystemField` metadata through `IFieldService` when available.
   - Fallback template supports company imports with required headers such as `companyName` and `organizationName`.
   - Validation rules cover CSV-only uploads, required fields, numeric parsing, and dropdown option checks from metadata.
   - Processing is synchronous and currently creates new `Company` rows, skipping existing company names in the current organization.

2. Supporting evidence (Risk Response & Treatment)
   - Evidence endpoints were added to `RiskTreatmentController`.
   - Upload/download/delete are routed through the storage service using the existing storage `DocumentDto` contract.
   - Each risk treatment currently stores one supporting evidence document UUID in `supportingEvidenceDocument`.

3. DTO mapping fixes for UI clarity
   - KPA/KPI and KRI/KPI response DTOs expose display-friendly fields:
     - `ownerName` / `riskOwnerName`
     - `evaluationByName` / `kriEvaluationByName`
     - `departmentName`
     - `unitOfMeasurement`
   - These fields are additive and should be preferred for list/detail display where present.

---

## API endpoints

Examples below use the `ERM_backend` route paths. If the UI calls through API Gateway, prepend the gateway prefix used by the environment.

### Bulk import

#### GET {base}/bulk-import/template/{moduleId}?templateType={table}

Downloads a generated CSV template.

Path/query parameters:

| Name | Required | Notes |
|------|----------|-------|
| `moduleId` | yes | Used to resolve module metadata |
| `templateType` | no | Overrides the resolved table name, for example `company` |

Response: `GeneralResponse<byte[]>`

The `data` field contains generated CSV bytes. The controller does not currently set a file download header, so the UI may need to convert the wrapped bytes into a CSV download.

#### POST {base}/bulk-import/validate

Validates a CSV file before processing.

Consumes: `multipart/form-data`

Form fields:

| Name | Required | Notes |
|------|----------|-------|
| `file` | yes | Must be a `.csv` file |
| `moduleId` | yes | Used to resolve validation metadata |
| `templateType` | no | Overrides the resolved table name |

Response: `GeneralResponse<BulkImportSummary>`

```json
{
  "status": "SUCCESS",
  "message": "Validation complete.",
  "data": {
    "totalRows": 10,
    "validRows": 8,
    "invalidRows": 2,
    "duplicateRows": 0,
    "warnings": [
      "Row 7: company 'Acme Ltd' already exists in the current organization and will be skipped."
    ],
    "errors": [
      {
        "rowNumber": 3,
        "columnName": "companyName",
        "message": "Company name is required."
      }
    ]
  }
}
```

#### POST {base}/bulk-import/process

Processes the uploaded CSV. The backend calls validation first.

Consumes: `multipart/form-data`

Form fields: same as `POST /bulk-import/validate`.

Response: `GeneralResponse<BulkImportUploadResponse>`

```json
{
  "status": "SUCCESS",
  "message": "Import processed.",
  "data": {
    "importId": 1788026400000,
    "status": "COMPLETED",
    "summary": {
      "totalRows": 10,
      "validRows": 10,
      "invalidRows": 0,
      "duplicateRows": 0,
      "warnings": [],
      "errors": []
    }
  }
}
```

`data.status` is `COMPLETED` when validation passes and `FAILED` when validation errors exist.

#### GET {base}/bulk-import/{importId}/status

Returns the in-memory status for a processed import.

Response: `GeneralResponse<BulkImportUploadResponse>`

Unknown import ids return `data.status: "NOT_FOUND"`.

#### GET {base}/bulk-import/{importId}/errors

Returns stored validation errors for a processed import.

Response: `GeneralResponse<List<Object>>`

Unknown import ids return an empty list.

### Supporting evidence (Risk Treatment)

#### POST {base}/risk-treatment/{id}/evidence

Uploads or replaces supporting evidence for a Risk Response & Treatment record.

Consumes: `multipart/form-data`

Form fields:

| Name | Required | Default | Notes |
|------|----------|---------|-------|
| `file` | yes | - | File to upload |
| `description` | no | - | Stored as `supportingEvidence` when present |
| `purpose` | no | `risk-response-treatment` | Forwarded to storage metadata |

Response: `GeneralResponse<RiskResponseTreatmentResponse>`

The updated treatment response includes `supportingEvidence` and `supportingEvidenceDocument`.

#### GET {base}/risk-treatment/{id}/evidence

Fetches attached evidence metadata.

Response data shape:

```json
{
  "documentId": "8fa85f64-5717-4562-b3fc-2c963f66afa6",
  "description": "Board minutes and action plan",
  "fileName": "evidence.pdf",
  "contentType": "application/pdf",
  "purpose": "risk-response-treatment",
  "path": "s3://bucket/path/evidence.pdf"
}
```

If no document is attached, `documentId` is `null` and only `description` is returned.

#### GET {base}/risk-treatment/evidence/{documentId}

Fetches evidence document content and metadata from storage.

Response: `GeneralResponse<DocumentDto>`

`data.fileContent` contains Base64 file content. Use `data.fileName + data.fileExtension` for the downloaded filename and `data.contentType` for the Blob MIME type.

#### DELETE {base}/risk-treatment/{id}/evidence/{documentId}

Deletes the storage document and clears the risk treatment evidence fields when the document id matches the attached document.

Response: `GeneralResponse<Void>` with message `Supporting evidence deleted.`

---

## UI developer notes

1. CSV template
   - Use `GET /bulk-import/template/{moduleId}` to avoid header mismatches.
   - Pass `templateType=company` when the UI needs the company fallback import.
   - Keep the CSV UTF-8 encoded.

2. Validation UI
   - Display row-level errors using `rowNumber`, `columnName`, and `message`.
   - Display `warnings` separately. Duplicate company names are warnings and will be skipped during processing.
   - Only enable processing when `errors.length === 0`.

3. File uploads
   - Use standard multipart uploads.
   - After upload or delete, call `GET /risk-treatment/{id}/evidence` to refresh the visible attachment.
   - Current backend supports one supporting evidence document per treatment, so uploads replace the stored document reference.

4. Error handling
   - Missing/empty bulk import file: `Please upload a CSV file.`
   - Non-CSV bulk import file: `Unsupported file type. Please upload CSV.`
   - Missing evidence file: `Please select a file to upload.`
   - Missing treatment record: `Risk response treatment not found.`

5. Permissions and RBAC
   - Bulk import endpoints are intended for admin screens. Keep route visibility and API calls aligned with the app's existing auth and tenant headers.

---

## Files changed for reviewer

- `ERM_backend/src/main/java/ermorg/erm/controller/BulkImportController.java`
- `ERM_backend/src/main/java/ermorg/erm/service/IBulkImportService.java`
- `ERM_backend/src/main/java/ermorg/erm/serviceimpl/BulkImportService.java`
- `ERM_backend/src/main/java/ermorg/erm/dto/bulk/*`
- `ERM_backend/src/main/java/ermorg/erm/controller/RiskTreatmentController.java`
- `ERM_backend/src/main/java/ermorg/erm/serviceimpl/RiskTreatmentService.java`
- `command/storage/src/main/java/ermorg/storage/dto/request/DocumentUploadDto.java`
- `command/storage/src/main/java/ermorg/storage/dto/response/DocumentDto.java`

---

## Follow-ups

- Consider returning the template as `text/csv` with `Content-Disposition` instead of wrapped bytes.
- Consider asynchronous import jobs with persistent status tracking for large CSV files.
- Add unit/integration tests for validation failures, duplicate handling, evidence upload, and evidence deletion.
