# ERM Backend — API Reference

> **Last verified:** 2026-08-30 against repository code and live DB notes (MySQL 8.0.44, AWS RDS `erm`)
> **ERM_backend:** port 8086
> **erm-command-organization:** port 8081
> **Base path (via gateway):** All requests route through API Gateway (port 8080)

All responses are wrapped in `GeneralResponse`:
```json
{
  "status": "SUCCESS",
  "message": "...",
  "data": { ... }
}
```

---

## Table of Contents

1. [Role Management](#1-role-management)
2. [KPA / KPI Review](#2-kpa--kpi-review)
3. [Approval Workflow](#3-approval-workflow)
4. [Bulk Import](#4-bulk-import)
5. [Risk Treatment Evidence](#5-risk-treatment-evidence)
6. [Field Configuration](#6-field-configuration)
7. [Enums Reference](#7-enums-reference)
8. [Database Schema Reference](#8-database-schema-reference)
9. [UI Integration Guide](#9-ui-integration-guide)

---

## 1. Role Management

**Service:** `erm-command-organization` (port 8081)

### POST /role/save
Create or update a role.

**Request body:**
```json
{
  "roleId": 0,
  "roleName": "Risk Manager",
  "priority": 2,
  "description": "Manages risk assessments",
  "roleTypeCode": "BUSINESS"
}
```

| Field | Type | Required | Notes |
|-------|------|----------|-------|
| `roleId` | long | no | `0` = create; positive = update |
| `roleName` | String | yes | Must be unique across active roles |
| `priority` | long | yes | Sort weight |
| `description` | String | no | |
| `roleTypeCode` | String | **yes on create** | Must match a code in `role_type` table. Optional on update (preserves existing if omitted) |

**Response:** `RoleResponse`
```json
{
  "roleId": 42,
  "roleName": "Risk Manager",
  "description": "...",
  "priority": 2,
  "roleTypeCode": "BUSINESS",
  "rights": []
}
```

**Errors:**
- `"Role type code is required for new roles."` — create with no `roleTypeCode`
- `"Invalid role type code."` — code not found in `role_type` table
- `"Role with this name already exists."` — duplicate name

---

### GET /role/{roleId}
Get a single role by ID.

**Response:** `RoleResponse` (same shape as above)

---

### GET /role/all
Get all active roles (excludes `orgAdmin` system role).

**Response:** `List<RoleResponse>`

---

### DELETE /role/delete/{roleId}
Soft-delete a role.

---

### GET /role/roletype/all
Get all role types for populating the Role Type dropdown.

**Response:** `List<RoleType>`
```json
[
  { "id": 1, "code": "SUPER_ADMIN",    "name": "System / Super Admin",  "description": "...", "createdAt": "..." },
  { "id": 2, "code": "ORG_ADMIN",      "name": "Organisation Admin",    "description": "...", "createdAt": "..." },
  { "id": 3, "code": "COMPANY_ADMIN",  "name": "Company Admin",         "description": "...", "createdAt": "..." },
  { "id": 4, "code": "BASIC_USER",     "name": "Basic User",            "description": "...", "createdAt": "..." },
  { "id": 5, "code": "ADVANCED_USER",  "name": "Advanced User",         "description": "...", "createdAt": "..." },
  { "id": 6, "code": "REPORTING_USER", "name": "Reporting User",        "description": "...", "createdAt": "..." }
]
```

> **⚠ Bug:** `RoleService.getRoleType()` currently returns `List.of()`. This is a known open issue — fix required before this endpoint is usable by the UI.

---

## 2. KPA / KPI Review

**Service:** `ERM_backend` (port 8086)

### POST /kpa-kpi-review
Create a KPA/KPI review record.

**Request body (`KpaKpiReviewRequestDTO`):**

| Field | Type | Required | Validation |
|-------|------|----------|-----------|
| `kpa` | String | ✅ | max 255 |
| `keyPerformanceIndicator` | String | ✅ | max 1000 |
| `reportingFrequency` | String | ✅ | `DAILY\|WEEKLY\|MONTHLY\|QUARTERLY\|HALF_YEARLY\|ANNUALLY\|AD_HOC` |
| `ownerId` | long | ✅ | positive |
| `kpiEvaluationBy` | long | ✅ | positive |
| `status` | String | ✅ | `DRAFT\|ACTIVE\|IN_REVIEW\|COMPLETED\|OVERDUE\|INACTIVE` |
| `businessObjectives` | String | no | max 1000 |
| `businessFunction` | String | no | max 255 |
| `target` | String | no | max 1000 |
| `keyPerformanceParameters` | String | no | max 1000 |
| `typesOfKpi` | String | no | max 100 |
| `performanceIndicators` | String | no | max 1000 |
| `stakeholderDepartments` | String | no | max 1000 |
| `performanceToleranceMinValue` | BigDecimal | no | ≥ 0 |
| `performanceToleranceMaxValue` | BigDecimal | no | ≥ 0 |
| `targets` | String | no | max 1000 |
| `activities` | String | no | max 1000 |
| `thresholds` | String | no | max 1000 |
| `performanceAppetite` | String | no | max 1000 |
| `escalationMatrix` | String | no | max 1000 |
| `measurableParameters` | String | no | max 255 |
| `currency` | String | no | 3-letter ISO code (e.g. `INR`, `USD`) |
| `valueUnit` | RiskValueUnit | no | see enum |
| `targetValue` | BigDecimal | no | ≥ 0 |
| `actualValue` | BigDecimal | no | ≥ 0 |
| `january` … `december` | BigDecimal | no | ≥ 0 each |
| `q1` … `q4` | BigDecimal | no | ≥ 0 each |
| `kpiType` | String | no | max 50 |
| `kraRating` | String | no | max 30 |
| `riskAppetiteStatus` | String | no | max 255 |
| `riskAcceptanceLevel` | RiskAcceptanceLevel | no | see enum |
| `kpiEvaluationFrequency` | String | no | same values as `reportingFrequency` |
| `potentialLossPercentage` | BigDecimal | no | 0–100 |
| `yearlyFrequency` | Integer | no | ≥ 0 |
| `dueDate` | Date | no | future or present |
| `actualDate` | Date | no | past or present |
| `lastKpiEvaluationDate` | Date | no | past or present |
| `nextEvaluationDate` | Date | no | future or present |
| `kpaKpiReviewId` | long | no | `0` = create |

> **Deprecated fields** (still accepted but ignored): `riskToleranceRangeMinValue`, `riskToleranceRangeMaxValue`, `riskAppetite`

**Response:** `KpaKpiReviewResponseDTO` — all request fields plus server-computed `annualLossExpectancy`.

---

### GET /kpa-kpi-review/{id}
Get a single KPA/KPI review.

**Response:** `KpaKpiReviewResponseDTO`

---

### GET /kpa-kpi-review
Paginated list with optional filters.

**Query params:**

| Param | Type | Default | Notes |
|-------|------|---------|-------|
| `status` | String | — | Filter by status value |
| `search` | String | — | Text search on kpa/kpi fields |
| `page` | int | 0 | Spring Pageable |
| `size` | int | 20 | Spring Pageable |
| `sort` | String | `id,DESC` | Spring Pageable |

**Response:** `Page<KpaKpiReviewResponseDTO>`
```json
{
  "data": {
    "content": [ ... ],
    "totalElements": 42,
    "totalPages": 3,
    "size": 20,
    "number": 0
  }
}
```

---

### DELETE /kpa-kpi-review/{id}
Soft-delete a KPA/KPI review.

---

## 3. Approval Workflow

**Service:** `ERM_backend` (port 8086)
**Base path:** `/approvals`

### POST /approvals
Create a new approval task.

**Request body (`ApprovalRequest`):**
```json
{
  "approverId": 10,
  "submitterId": 5,
  "pageLink": "/risk/assessment/123",
  "recordName": "Risk Assessment Q3",
  "taggedMembers": "12,15,18",
  "recipientUserIds": "10,12",
  "sourceModule": "RISK_ASSESSMENT",
  "sourceRecordId": "123",
  "dueAt": "2026-08-15T00:00:00",
  "organizationId": 1,
  "companyId": 2
}
```

---

### PUT /approvals/{id}/decision
Submit an approval decision (approve or reject).

**Request body (`ApprovalDecisionRequest`):**
```json
{
  "status": "REJECTED",
  "comment": "Insufficient data",
  "rootCause": "Missing quantitative risk data for Q3",
  "actionTaken": "Returned to risk owner for revision"
}
```

| Field | Type | Required | Notes |
|-------|------|----------|-------|
| `status` | ApprovalStatus | ✅ | `PENDING\|APPROVED\|REJECTED` |
| `comment` | String | no | General comment |
| `rootCause` | String | no | **Recommended on REJECTED** — stored in `approval.root_cause` |
| `actionTaken` | String | no | **Recommended on REJECTED** — stored in `approval.action_taken` |

**Response:** `ApprovalResponse` (full workflow state object — see [ApprovalResponse shape](#approvalresponse-shape))

---

### POST /approvals/{id}/trigger
Manually trigger the approval workflow for a record.

**Response:** `ApprovalResponse`

---

### POST /approvals/{id}/escalate
Escalate an overdue or stuck approval. Increments `escalation_level`.

**Response:** `ApprovalResponse`

---

### POST /approvals/{id}/reminder
Send a reminder notification to the approver.
Updates `approval.reminder_notified_at`.

**Response:** `ApprovalResponse`

---

### GET /approvals/pending
Get all pending approvals for the **currently authenticated approver**.

**Response:** `List<ApprovalResponse>`

---

### GET /approvals/dashboard
Combined dashboard view for the current approver.

**Response:** `ApprovalDashboardResponse`
```json
{
  "data": {
    "pending":     [ ...ApprovalResponse ],
    "upcomingDue": [ ...ApprovalResponse ],
    "overdue":     [ ...ApprovalResponse ],
    "history":     [ ...ApprovalResponse ]
  }
}
```

---

### GET /approvals/dashboard/upcoming-due
Pending approvals with `due_at` within the next 7 days.

**Response:** `List<ApprovalResponse>`

---

### GET /approvals/dashboard/overdue
Pending approvals where `due_at` is before now.

**Response:** `List<ApprovalResponse>`

---

### GET /approvals/dashboard/history
Completed approval decisions (non-PENDING) for the current approver.

**Response:** `List<ApprovalResponse>`

---

### GET /approvals/login-target
Returns login redirect information for approvers.

**Response:** `ApprovalLoginTargetResponse`

---

### ApprovalResponse Shape

```json
{
  "id": 1,
  "approverId": 10,
  "submitterId": 5,
  "requestedBy": 5,
  "approvedBy": 10,
  "pageLink": "/risk/assessment/123",
  "status": "REJECTED",
  "comment": "Insufficient data",
  "rootCause": "Missing quantitative risk data for Q3",
  "actionTaken": "Returned to risk owner for revision",
  "notifiedAt": "...",
  "createdAt": "...",
  "requestedAt": "...",
  "recordName": "Risk Assessment Q3",
  "taggedMembers": "12,15,18",
  "recipientUserIds": "10,12",
  "sourceModule": "RISK_ASSESSMENT",
  "sourceRecordId": "123",
  "organizationId": 1,
  "companyId": 2,
  "assignedNotifiedAt": "...",
  "decisionNotifiedAt": "...",
  "reminderNotifiedAt": "...",
  "dueAt": "...",
  "escalationLevel": 0,
  "escalatedAt": null,
  "closedAt": "...",
  "triggerType": "AUTOMATIC",
  "triggeredById": null,
  "triggeredAt": null,
  "escalationSource": null
}
```

---

## 4. Bulk Import

**Service:** `ERM_backend` (port 8086)
**Base path:** `/bulk-import`

Bulk import currently supports CSV files and processes synchronously. Template headers are generated from `SystemTable` / `SystemField` metadata when available; otherwise the service falls back to a company import template.

### GET /bulk-import/template/{moduleId}
Download a CSV template for the supplied module.

**Path params:**

| Param | Type | Required | Notes |
|-------|------|----------|-------|
| `moduleId` | long | yes | Used to resolve the first system table for the module |

**Query params:**

| Param | Type | Required | Notes |
|-------|------|----------|-------|
| `templateType` | String | no | Overrides metadata lookup table name. Example: `company` |

**Response:** `GeneralResponse<byte[]>`

The response `data` contains the generated CSV bytes. The controller wraps the bytes in `GeneralResponse`; it does not currently set `Content-Disposition`.

---

### POST /bulk-import/validate
Validate an uploaded CSV file.

**Consumes:** `multipart/form-data`

**Form fields:**

| Field | Type | Required | Notes |
|-------|------|----------|-------|
| `file` | MultipartFile | yes | Must be a `.csv` file |
| `moduleId` | long | yes | Used to resolve import metadata |
| `templateType` | String | no | Overrides the resolved table name |

**Response:** `BulkImportSummary`
```json
{
  "status": "SUCCESS",
  "message": "Validation complete.",
  "data": {
    "totalRows": 2,
    "validRows": 2,
    "invalidRows": 0,
    "duplicateRows": 0,
    "warnings": [],
    "errors": []
  }
}
```

`errors[]` entries use this shape:
```json
{
  "rowNumber": 3,
  "columnName": "companyName",
  "message": "Company name is required."
}
```

**Validation behavior:**
- Rejects missing/empty files with `"Please upload a CSV file."`
- Rejects non-CSV filenames with `"Unsupported file type. Please upload CSV."`
- Requires at least one data row; otherwise returns a warning and marks one invalid row.
- Checks required metadata fields, numeric metadata fields, and dropdown option values when metadata exists.
- For company imports, warns when a company already exists in the current organization.

---

### POST /bulk-import/process
Process an uploaded CSV file.

**Consumes:** `multipart/form-data`

**Form fields:** same as `POST /bulk-import/validate`.

**Response:** `BulkImportUploadResponse`
```json
{
  "status": "SUCCESS",
  "message": "Import processed.",
  "data": {
    "importId": 1788026400000,
    "status": "COMPLETED",
    "summary": {
      "totalRows": 2,
      "validRows": 2,
      "invalidRows": 0,
      "duplicateRows": 0,
      "warnings": [],
      "errors": []
    }
  }
}
```

If validation errors are present, `data.status` is `FAILED`; otherwise it is `COMPLETED`. For the fallback/current `company` import, processing creates new `Company` rows and skips existing company names within the current organization.

---

### GET /bulk-import/{importId}/status
Fetch the in-memory status for a processed import.

**Response:** `BulkImportUploadResponse`

If the import id is unknown, `data.status` is `NOT_FOUND`.

---

### GET /bulk-import/{importId}/errors
Fetch row-level validation errors for a processed import.

**Response:** `List<Object>` containing the stored `BulkImportValidationError` entries. Unknown import ids return an empty list.

---

## 5. Risk Treatment Evidence

**Service:** `ERM_backend` (port 8086)
**Base path:** `/risk-treatment`

Evidence endpoints attach a single supporting-evidence document to a Risk Response & Treatment record. Files are passed through to the storage service as Base64 document payloads and stored using the configured storage backend.

### POST /risk-treatment/{id}/evidence
Upload or replace supporting evidence for a risk treatment.

**Consumes:** `multipart/form-data`

**Path params:**

| Param | Type | Required | Notes |
|-------|------|----------|-------|
| `id` | long | yes | Risk response treatment id |

**Form fields:**

| Field | Type | Required | Default | Notes |
|-------|------|----------|---------|-------|
| `file` | MultipartFile | yes | - | File to upload |
| `description` | String | no | - | Stored in `supportingEvidence` when present |
| `purpose` | String | no | `risk-response-treatment` | Forwarded to storage metadata |

**Response:** `RiskResponseTreatmentResponse`
```json
{
  "status": "SUCCESS",
  "message": "Supporting evidence uploaded successfully.",
  "data": {
    "riskResponseTreatmentId": 12,
    "supportingEvidence": "Board minutes and action plan",
    "supportingEvidenceDocument": "8fa85f64-5717-4562-b3fc-2c963f66afa6"
  }
}
```

---

### GET /risk-treatment/{id}/evidence
Fetch supporting evidence metadata for a risk treatment.

**Response data shape:**
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

---

### GET /risk-treatment/evidence/{documentId}
Download/fetch evidence document data from the storage service.

**Response:** storage `DocumentDto` wrapped in `GeneralResponse`. The `data.fileContent` field contains Base64 file content.

---

### DELETE /risk-treatment/{id}/evidence/{documentId}
Delete the evidence file and clear the risk treatment evidence fields when the document id matches the attached document.

**Response:** `GeneralResponse<Void>` with message `"Supporting evidence deleted."`

---

## 6. Field Configuration

**Service:** `ERM_backend` (port 8086)

### GET /field/options/{tableName}/{fieldName}
Returns active dropdown options for a system field.
Powers metadata-driven UI rendering — no hardcoding required.

**Path params:**

| Param | Description | Example values |
|-------|-------------|---------------|
| `tableName` | System table name | `riskAssessment`, `riskReview`, `kpaKpiReview`, `kriKpiReview` |
| `fieldName` | Field name on the system table | `valueUnit`, `currency`, `riskAppetiteStatus` |

**Examples:**
```
GET /field/options/riskAssessment/valueUnit
GET /field/options/riskReview/valueUnit
GET /field/options/kpaKpiReview/valueUnit
GET /field/options/kriKpiReview/valueUnit
```

**Response:** `List<FieldOptionResponse>`
```json
{
  "data": [
    { "id": 1, "value": "RS",        "label": "Rs.",       "displayOrder": 1 },
    { "id": 2, "value": "THOUSANDS", "label": "Thousands", "displayOrder": 2 },
    { "id": 3, "value": "LAKH",      "label": "Lakh",      "displayOrder": 3 },
    { "id": 4, "value": "CRORES",    "label": "Crores",    "displayOrder": 4 },
    { "id": 5, "value": "MILLION",   "label": "Million",   "displayOrder": 5 },
    { "id": 6, "value": "BILLION",   "label": "Billion",   "displayOrder": 6 },
    { "id": 7, "value": "TRILLION",  "label": "Trillion",  "displayOrder": 7 }
  ]
}
```

> **⚠ Gap:** `field_options` table is currently empty. This endpoint returns `[]` until V009 data seed is applied.

---

### GET /field/get-system-tables/{moduleId}
Get all system tables for a module (used in field configuration UI).

**Response:** `List<SystemTableResponse>`

---

### GET /field/get-system-table-by-name/{tableName}
Get a single system table by its name.

**Response:** `SystemTableResponse`

---

### GET /field/get-all-category-fields/{moduleId}
Get all categories with their fields for a module.

**Response:** `List<CategoryListResponse>`

---

### GET /field/get-category/{categoryId}
Get a single category with its fields.

**Response:** `CategoryResponse`

---

### SystemFieldResponse Shape (for reference)

```json
{
  "id": 123,
  "field": "valueUnit",
  "displayLabel": "Value",
  "fieldType": "DROPDOWN",
  "defaultValue": null,
  "validationRules": null,
  "isRequired": false,
  "isReadOnly": false,
  "isHidden": false,
  "displayOrder": 5,
  "options": [
    { "id": 1, "value": "RS", "label": "Rs.", "displayOrder": 1 }
  ]
}
```

---

## 7. Enums Reference

### RiskValueUnit
Serializes to its label string (e.g. `"RS"` in JSON becomes `"Rs."`).
Deserializes from both the enum name (`RS`) and the label (`Rs.`).

| Enum Name | JSON Value / Label |
|-----------|-------------------|
| `RS` | `Rs.` |
| `THOUSANDS` | `Thousands` |
| `LAKH` | `Lakh` |
| `CRORES` | `Crores` |
| `MILLION` | `Million` |
| `BILLION` | `Billion` |
| `TRILLION` | `Trillion` |

---

### RiskAcceptanceLevel
Serializes as the enum name string.

| Value | Suggested Display Label |
|-------|------------------------|
| `ACCEPTABLE_RISK` | Acceptable Risk |
| `ACCEPTABLE_WITH_MITIGATION` | Acceptable with Mitigation |
| `ACCEPTABLE_WITH_MONITORING` | Acceptable with Monitoring |
| `ACCEPTABLE_WITHOUT_MITIGATION_MONITORING` | Acceptable without Mitigation / Monitoring |
| `UNACCEPTABLE_RISK` | Unacceptable Risk |

---

### ApprovalStatus

| Value | Meaning |
|-------|---------|
| `PENDING` | Awaiting decision |
| `APPROVED` | Decision made — approved |
| `REJECTED` | Decision made — rejected |

---

### KPA/KPI Status

| Value | Meaning |
|-------|---------|
| `DRAFT` | Not yet submitted |
| `ACTIVE` | Live and being monitored |
| `IN_REVIEW` | Under approval review |
| `COMPLETED` | Evaluation complete |
| `OVERDUE` | Past due date |
| `INACTIVE` | Deactivated |

---

### Reporting / Evaluation Frequency

`DAILY` · `WEEKLY` · `MONTHLY` · `QUARTERLY` · `HALF_YEARLY` · `ANNUALLY` · `AD_HOC`

---

### WorkflowTriggerType

| Value | Meaning |
|-------|---------|
| `AUTOMATIC` | Triggered by the system |
| `MANUAL` | Triggered by a user action |

---

## 8. Database Schema Reference

> All columns verified 2026-08-01 (read-only queries against live DB).

### Table: `role_type`
| Column | Type | Notes |
|--------|------|-------|
| `id` | INT PK | Auto-increment |
| `code` | VARCHAR(100) UNIQUE NOT NULL | Identifier used in API |
| `name` | VARCHAR(255) | Display name |
| `description` | TEXT | |
| `created_at` | TIMESTAMP | |

**Pre-seeded rows:** 6 (see Section 1)

---

### Table: `role` — new column
| Column | Type | Notes |
|--------|------|-------|
| `role_type_id` | INT NULL | FK → `role_type(id)` |

---

### Table: `kpa_kpi_review` (new)
Full schema in `database/migration/V002_Create_Kpa_Kpi_Review.sql`.
Key columns: `kpa`, `key_performance_indicator`, `reporting_frequency`, `currency`, `value_unit`, `status`, `risk_appetite_status`, `risk_acceptance_level`, monthly/quarterly values, evaluation dates.

---

### Table: `approval` — new columns
| Column | Type | Notes |
|--------|------|-------|
| `root_cause` | TEXT NULL | Populated on rejection |
| `action_taken` | TEXT NULL | Populated on rejection |
| `reminder_notified_at` | DATETIME(6) NULL | Updated when reminder sent |
| `trigger_type` | ENUM('AUTOMATIC','MANUAL') NULL | |
| `escalation_source` | ENUM('AUTOMATIC','MANUAL') NULL | |
| `triggered_by_id` | BIGINT NULL | FK → `user(id)` |
| `triggered_at` | DATETIME(6) NULL | |
| `organization_id` | BIGINT NULL | FK → `organization(id)` |
| `company_id` | BIGINT NULL | FK → `company(id)` |
| `source_module` | VARCHAR(255) NULL | e.g. `RISK_ASSESSMENT` |
| `source_record_id` | VARCHAR(255) NULL | |

**Indexes on `approval`:**
- `idx_approval_approver_status_due_deleted` — dashboard queries
- `idx_approval_status_reminder_deleted` — reminder scheduler
- `idx_approval_source` — source lookup

---

### Tables: `risk_assessment`, `risk_reviews`, `kri_kpi_reviews`, `kpa_kpi_review` — new columns

| Column | Type | Tables |
|--------|------|--------|
| `currency` | VARCHAR(10) | `risk_assessment`, `risk_reviews`, `kri_kpi_reviews` |
| `value_unit` | ENUM(7) | all 4 |
| `risk_appetite_status` | VARCHAR(255) | all 4 |
| `risk_acceptance_level` | ENUM(5) | all 4 |
| `last_evaluation_date` | DATE | `risk_assessment`, `risk_reviews` |
| `next_evaluation_date` | DATE | `risk_assessment`, `risk_reviews` |

---

### Table: `system_fields` — new metadata columns
| Column | Type | Notes |
|--------|------|-------|
| `display_label` | VARCHAR(255) NULL | Human-readable label for UI |
| `field_type` | VARCHAR(50) NULL | `INPUT_FIELD`, `DROPDOWN`, `DATE_PICKER`, etc. |
| `default_value` | VARCHAR(255) NULL | |
| `validation_rules` | TEXT NULL | JSON validation rules |
| `is_required` | BIT(1) | Default `0` |
| `is_read_only` | BIT(1) | Default `0` |
| `is_hidden` | BIT(1) | Default `0` |
| `display_order` | INT NULL | Sort order within table |

---

### Table: `field_options` (new)
| Column | Type | Notes |
|--------|------|-------|
| `id` | BIGINT PK | |
| `system_field_id` | BIGINT NOT NULL | FK → `system_fields(id)` |
| `option_value` | VARCHAR(100) NOT NULL | Stored value (e.g. `RS`) |
| `option_label` | VARCHAR(255) NOT NULL | Display value (e.g. `Rs.`) |
| `display_order` | INT NULL | Sort order |
| `is_active` | BIT(1) | Default `1` |
| `deleted` | BIT(1) | Soft-delete flag |

---

### Table: `erm_maturity_assessments` — new column
| Column | Type | Notes |
|--------|------|-------|
| `erm_maturity_id` | VARCHAR(255) NULL | Group key (`companyId` or `companyId_deptId`) |

---

### Table: `erm_maturity_department_ids` (new)
| Column | Type | Notes |
|--------|------|-------|
| `maturity_assessment_id` | BIGINT PK, FK | → `erm_maturity_assessments(id)` CASCADE DELETE |
| `department_id` | BIGINT PK | |

---

### Table: `kri_kpi_reviews` — new column
| Column | Type | Notes |
|--------|------|-------|
| `risk_assessment_id` | BIGINT NULL | FK → `risk_assessment(id)` |

---

## 9. UI Integration Guide

This section is addressed directly to frontend developers.

---

### 9.1 Role Type Dropdown

When rendering the **Role Create / Edit** form:

```
1. Call:  GET /role/roletype/all
2. Map response to dropdown options: { label: item.name, value: item.code }
3. Send selected code as "roleTypeCode" in POST /role/save body
4. ⚠ roleTypeCode is required on create — validate before submit
```

> **Known issue:** This endpoint currently returns an empty array due to a bug in `RoleService.getRoleType()`. Wait for a backend hotfix before wiring this dropdown.

---

### 9.2 Value Unit Dropdown (Metadata-Driven)

**Do NOT hardcode** the `valueUnit` options list. Fetch dynamically:

```
GET /field/options/{tableName}/valueUnit

Table name mapping:
  Risk Assessment form  → riskAssessment
  Risk Review form      → riskReview
  KPA/KPI Review form   → kpaKpiReview
  KRI/KPI Review form   → kriKpiReview
```

Use `value` as the form field value and `label` as the display text.

> **⚠ Gap:** `field_options` table is currently empty. This endpoint returns `[]`. Until seeded, use the hardcoded enum fallback:

| Send as `value` | Display as `label` |
|-----------------|-------------------|
| `RS` | Rs. |
| `THOUSANDS` | Thousands |
| `LAKH` | Lakh |
| `CRORES` | Crores |
| `MILLION` | Million |
| `BILLION` | Billion |
| `TRILLION` | Trillion |

---

### 9.3 Risk Acceptance Level Dropdown

Static — sourced from the `RiskAcceptanceLevel` enum. Safe to hardcode.

| Send | Display |
|------|---------|
| `ACCEPTABLE_RISK` | Acceptable Risk |
| `ACCEPTABLE_WITH_MITIGATION` | Acceptable with Mitigation |
| `ACCEPTABLE_WITH_MONITORING` | Acceptable with Monitoring |
| `ACCEPTABLE_WITHOUT_MITIGATION_MONITORING` | Acceptable without Mitigation / Monitoring |
| `UNACCEPTABLE_RISK` | Unacceptable Risk |

---

### 9.4 Approval Decision Dialog

The rejection/decision modal must include two new optional text fields:

```
Root Cause     → maps to "rootCause"  in PUT /approvals/{id}/decision body
Action Taken   → maps to "actionTaken" in PUT /approvals/{id}/decision body
```

- Recommend making both **required when status = REJECTED**
- Both appear in `ApprovalResponse` for display in approval history views

---

### 9.5 Approval Dashboard

Use the new endpoints to build the approver's dashboard:

```
GET /approvals/dashboard          → single call, all 4 buckets
GET /approvals/dashboard/upcoming-due  → due within 7 days
GET /approvals/dashboard/overdue  → past due date
GET /approvals/dashboard/history  → completed decisions
```

Recommended dashboard layout:
- Pending count badge in navbar
- 4-tab or 4-card layout: Pending / Upcoming / Overdue / History

---

### 9.6 KPA/KPI Review Form

New module — full form build required. Key fields:

| Field | UI Widget | Notes |
|-------|-----------|-------|
| `kpa` | Text input | required |
| `keyPerformanceIndicator` | Text area | required |
| `reportingFrequency` | Dropdown | required; 7 values |
| `ownerId` | User picker | required |
| `kpiEvaluationBy` | User picker | required |
| `status` | Dropdown | required; 6 values |
| `currency` | Text input | ISO 3-letter code |
| `valueUnit` | Dropdown | fetch from `/field/options/kpaKpiReview/valueUnit` |
| `riskAcceptanceLevel` | Dropdown | 5 enum values |
| Monthly values (Jan–Dec) | 12 number inputs | grouped in a grid |
| Quarterly values (Q1–Q4) | 4 number inputs | |
| `dueDate` | Date picker | future/present |
| `nextEvaluationDate` | Date picker | future/present |

---

### 9.7 Risk Assessment / Risk Review New Fields

Add to existing forms:

| Field | Widget | Endpoint context |
|-------|--------|-----------------|
| `currency` | Text input (ISO 3-char) | risk_assessment, risk_reviews |
| `valueUnit` | Dropdown | `GET /field/options/riskAssessment/valueUnit` |
| `riskAppetiteStatus` | Text input or dropdown | free text |
| `riskAcceptanceLevel` | Dropdown | 5 enum values |
| `lastEvaluationDate` | Date picker | risk_assessment only |
| `nextEvaluationDate` | Date picker | risk_assessment, risk_reviews |

---

### 9.8 Bulk Import UI Flow

Use the `BulkImportSummary` response from `POST /bulk-import/validate` to render row-level errors before enabling processing.

```
1. Download template: GET /bulk-import/template/{moduleId}?templateType=company
2. Validate upload:   POST /bulk-import/validate     multipart fields: file, moduleId, templateType
3. Process upload:    POST /bulk-import/process      multipart fields: file, moduleId, templateType
4. Poll status:       GET /bulk-import/{importId}/status
5. Read errors:       GET /bulk-import/{importId}/errors
```

Display `errors[].rowNumber`, `errors[].columnName`, and `errors[].message`. Display `warnings[]` separately because duplicate company names are warnings and will be skipped during processing.

---

### 9.9 Risk Treatment Evidence UI Flow

Evidence is attached to one Risk Response & Treatment record.

```
Upload:   POST   /risk-treatment/{id}/evidence
Fetch:    GET    /risk-treatment/{id}/evidence
Download: GET    /risk-treatment/evidence/{documentId}
Delete:   DELETE /risk-treatment/{id}/evidence/{documentId}
```

Upload uses multipart fields `file`, optional `description`, and optional `purpose`. Download returns storage document metadata plus Base64 content in `data.fileContent`.

---

### 9.10 Backend Deployment Checklist (for DevOps)

Before deploying this release:

- [ ] Run all migrations V001 → V012 on the target environment
- [ ] Verify `role_type` table has 6 rows
- [ ] Verify `field_options` seeding ran (table should have 28 rows: 7 options × 4 tables)
- [ ] Fix `RoleService.getRoleType()` bug before UI wires the role type dropdown
- [ ] Confirm `approval.reminder_notified_at` column exists
- [ ] Confirm `erm_maturity_department_ids` table exists
- [ ] Confirm `kri_kpi_reviews.risk_assessment_id` column exists
