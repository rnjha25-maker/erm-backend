API Field Mapping & Integration Guide for UI

This document lists the added/updated API fields that UI developers should map to in the frontend. It provides the field name, type, server-side source, fallback rules, and example JSON for the most relevant endpoints.

Goal: No backend code changes required for UI — the UI only needs to read these fields from responses and use the existing endpoints.

---

1) KPA/KPI response DTO (KpaKpiReviewResponseDTO)

New/important fields to map in UI:

- ownerId (long)
  - Type: number
  - Source: RiskResponse/KPA owner user id (existing field)
  - UI use: keep for linking to user profile or edit actions.

- ownerName (String)
  - Type: string
  - Source: user.userDetail.firstName + " " + user.userDetail.lastName; fallback to user.email when name not available
  - UI use: display as the owner in lists and detail views (preferred over ownerId for user-friendly display)

- evaluationBy / kpiEvaluationBy / evaluationBy (long)
  - Type: number / Long
  - Source: user id who evaluates the KPI (existing field)
  - UI use: keep for linking to evaluator profile or editing

- evaluationByName (String)
  - Type: string
  - Source: evaluator.userDetail first+last name, fallback to evaluator.email
  - UI use: display as evaluator; use this value for labels and lists

- departmentName (String)
  - Type: string
  - Source: prefer businessFunction field (if present), otherwise stakeholderDepartments
  - UI use: show as the human-friendly department/functional area for KPA/KPI

- unitOfMeasurement (String)
  - Type: string
  - Source: prefers RiskValueUnit enum label (e.g., "Million") when present, otherwise legacy unitOfMeasurement string stored in DB
  - UI use: show next to numeric values (target / actual) for clarity

Notes & sample KPA/KPI response (trimmed):

{
  "kpaKpiReviewId": 123,
  "kpa": "Operational Efficiency",
  "businessObjectives": "Reduce processing time",
  "departmentName": "Operations",
  "ownerId": 45,
  "ownerName": "Ajeet Kumar",
  "evaluationBy": 67,
  "evaluationByName": "Priya Singh",
  "unitOfMeasurement": "Million",
  "valueUnit": "MILLION",
  "targetValue": 10.0,
  "actualValue": 8.7
}

Map UI columns/fields to ownerName and evaluationByName for display. Keep ownerId/evaluationBy for action links.

---

2) KRI/KPI response DTO (KriKpiReviewResponseDTO)

New/important fields to map in UI:

- riskOwner (long)
  - Type: number
  - Source: user id of the risk owner
  - UI use: linking and permissions logic

- riskOwnerName (String)
  - Type: string
  - Source: riskOwner.userDetail first+last name, fallback to email
  - UI use: display owner in lists and detail panels

- kriEvaluationBy (long)
  - Type: number
  - Source: user id who evaluates the KRI

- kriEvaluationByName (String)
  - Type: string
  - Source: kriEvaluationBy.userDetail name or email fallback
  - UI use: display evaluator name

- departmentName (String)
  - Type: string
  - Source: businessFunction or stakeholderDepartments
  - UI use: display department/functional area

- unitOfMeasurement (String)
  - Type: string
  - Source: prefer valueUnit.enum label when present; fallback to unitOfMeasurement string
  - UI use: display with numeric KPI values

Notes & sample KRI/KPI response (trimmed):

{
  "kriId": 321,
  "riskId": 88,
  "riskTitle": "Data Security",
  "riskOwner": 45,
  "riskOwnerName": "Ajeet Kumar",
  "kriEvaluationBy": 67,
  "kriEvaluationByName": "Priya Singh",
  "departmentName": "IT",
  "unitOfMeasurement": "Lakh",
  "valueUnit": "LAKH",
  "targetValue": "100",
  "actualValue": "120"
}

---

3) Bulk import endpoints (template / validate / process)

Controller endpoints and request param names (as implemented):

- GET /bulk-import/template/{moduleId}?templateType={optional}
  - Response: GeneralResponse<byte[]> where data is CSV bytes. The UI should trigger a download using the returned bytes.

- POST /bulk-import/validate (consumes multipart/form-data)
  - Form params:
    - file (MultipartFile) — the CSV file
    - moduleId (Long) — id of the module/table the template targets (server-side metadata)
    - templateType (String) optional
  - Response: GeneralResponse<BulkImportSummary>
    - BulkImportSummary fields:
      - totalRows (int)
      - validRows (int)
      - invalidRows (int)
      - duplicateRows (int)
      - warnings (List<String>)
      - errors (List<BulkImportValidationError>)
    - BulkImportValidationError:
      - rowNumber (int) — 1-based row number in CSV
      - columnName (String)
      - message (String)
  - UI use: display row-level errors with column names and messages; disable Process until no blocking errors.

- POST /bulk-import/process (consumes multipart/form-data)
  - Form params same as validate
  - Response: GeneralResponse<BulkImportUploadResponse>
    - BulkImportUploadResponse fields:
      - importId (Long) — server import job/record id
      - status (String) — e.g., "COMPLETED", "PARTIAL", "FAILED"
      - summary (BulkImportSummary) — same shape as above with counts and any errors/warnings
  - UI use: show a summary screen with counts and any non-blocking warnings. If the import is long-running, poll /bulk-import/{importId}/status and /bulk-import/{importId}/errors (both implemented).

Important: The CSV must be UTF-8 encoded. The server will parse and validate headers according to metadata for moduleId.

---

4) Evidence endpoints (Risk Treatment attachments)

Controller endpoints and params (as implemented):

- POST /risk-treatment/{id}/evidence (consumes multipart/form-data)
  - Path param: id (treatment record id)
  - Form params:
    - file (MultipartFile) — required
    - description (String) — optional
    - purpose (String) — optional, default: "risk-response-treatment"
  - Response: GeneralResponse<RiskResponseTreatmentResponse>
    - The returned RiskResponseTreatmentResponse should include updated evidence listing. For UI, prefer the list returned by GET /risk-treatment/{id}/evidence.

- GET /risk-treatment/{id}/evidence
  - Response: GeneralResponse<Object> where data should be an array/list of document-like objects. Each document object includes at least:
    - id (String) — document identifier
    - name (String) — file name
    - path (String) — storage path or URL
    - size (number) — file size in bytes
    - uploadedBy (number) — user id of uploader (may be included)
    - uploadedAt (String) — ISO timestamp
  - UI use: render list with Download and Delete buttons for each item.

- GET /risk-treatment/evidence/{documentId}
  - Path param: documentId (string)
  - Response: file stream (download)

- DELETE /risk-treatment/{id}/evidence/{documentId}
  - Path params: id (treatment id), documentId
  - Response: GeneralResponse<Void> (status & message)
  - UI use: confirm delete then call; refresh list on success.

Sample evidence list item (example):

{
  "id": "c3f4b8f2-...",
  "name": "evidence.pdf",
  "path": "s3://bucket/path/evidence.pdf",
  "size": 42581,
  "uploadedBy": 45,
  "uploadedAt": "2026-08-29T16:00:00Z"
}

---

5) Recommended UI integration process (step-by-step)

1. Template download
   - Call GET /bulk-import/template/{moduleId} when user clicks "Download template".
   - Save response bytes as a .csv file and prompt user to open/edit locally.

2. Client-side metadata (optional)
   - If the frontend already has system field metadata endpoints, fetch dropdown options for form-side validation.

3. Validation
   - After user selects a CSV file, POST to /bulk-import/validate (multipart form) with moduleId.
   - If response.summary.invalidRows > 0 or response.summary.errors not empty, present errors with rowNumber, columnName, and message. Block processing until resolved.

4. Processing
   - When validation passes, call /bulk-import/process with same params.
   - Show progress spinner. If the response contains importId and status, poll /bulk-import/{importId}/status until final. Use /bulk-import/{importId}/errors to fetch full error lists if needed.

5. Evidence upload
   - Use multipart POST /risk-treatment/{id}/evidence with form field `file` and optional `description`.
   - On success, call GET /risk-treatment/{id}/evidence to refresh display.

6. Display fields
   - For owner/evaluator display: use ownerName / evaluationByName / kriEvaluationByName (fallback shown already in backend). These always prefer human-friendly text and may be empty only if user record lacks email/name.
   - For unit display: show unitOfMeasurement in UI next to numbers.

---

6) Quick checklist for UI developer

- [ ] Use ownerName / riskOwnerName / evaluationByName / kriEvaluationByName for display instead of raw IDs.
- [ ] Use departmentName for department labels.
- [ ] Use unitOfMeasurement from response (backend prefers enum label when available).
- [ ] For bulk-import, call validate first, show row-level errors, then process.
- [ ] For evidence, use upload endpoint then refresh via GET /risk-treatment/{id}/evidence.

---

If you'd like, I can:
- Add a short OpenAPI snippet for each endpoint (YAML) so your UI team can import into Postman or generate client stubs.
- Create a small example React/TypeScript interface file that maps the DTOs exactly for the frontend.

Tell me which (OpenAPI / TS interfaces) you'd prefer next.