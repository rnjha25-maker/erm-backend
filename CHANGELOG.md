# ERM Backend — Change Log

> **Branch:** `kri-risk-improvment` → merged to `main`
> **DB verified:** 2026-08-01 (read-only audit, MySQL 8.0.44, AWS RDS `erm`)
> **Service:** ERM_backend (port 8086) + erm-command-organization (port 8081)

---

## [kri-risk-improvment] — 2026-08-01

### Summary of all changes in this release cycle

| # | Area | Change | DB Verified |
|---|------|--------|:-----------:|
| 1 | Role Management | Added `role_type` master table; `role.role_type_id` FK | ✅ |
| 2 | KPA/KPI Review | New module — table + full CRUD API | ✅ |
| 3 | Risk fields | `risk_appetite_status` + `risk_acceptance_level` on 12 tables | ✅ |
| 4 | Approval workflow | `root_cause` + `action_taken` on `approval` | ✅ |
| 5 | Field config | Unique constraint fixed; per-category conditional unique index | ✅ |
| 6 | Currency / Value | `currency`, `value_unit` on 4 risk/review tables | ✅ |
| 7 | Risk Assessment | `last_evaluation_date`, `next_evaluation_date` added | ✅ |
| 8 | Metadata fields | `system_fields` metadata columns; `field_options` table | ✅ |
| 9 | ERM Maturity | `erm_maturity_id` grouping key + `erm_maturity_department_ids` table | ✅ |
| 10 | KRI/KPI ↔ Risk Assessment | `kri_kpi_reviews.risk_assessment_id` FK | ✅ |
| 11 | Approval dashboard | `reminder_notified_at` + 3 performance indexes | ✅ |
| 12 | Approval dashboard | 4 new dashboard endpoints + login-target | ✅ |
| 13 | Approval workflow | `trigger`, `escalate`, `reminder` action endpoints | ✅ |
| 14 | Field options API | `GET /field/options/{tableName}/{fieldName}` — metadata-driven dropdowns | ✅ |
| 15 | Field duplicate fix | `POST /field/save` — pre-save duplicate validation | ✅ |

---

## Database Changes

### V001 — Add RoleType Master Entity
**Status:** ✅ Applied | **Migration:** `database/migration/V001_Add_RoleType_Master_Entity.sql`

- New table `role_type` (`id`, `code` UNIQUE, `name`, `description`, `created_at`)
- Pre-seeded 6 role types: `SUPER_ADMIN`, `ORG_ADMIN`, `COMPANY_ADMIN`, `BASIC_USER`, `ADVANCED_USER`, `REPORTING_USER`
- New column `role.role_type_id INT NULL` with FK to `role_type(id)`

> **⚠ Known issue in script:** Contains Oracle/PostgreSQL `CREATE SEQUENCE` / `ALTER SEQUENCE` syntax (no-ops on MySQL 8). Dual `PRIMARY KEY` declaration. NOT NULL constraint was never enforced — `role_type_id` remains nullable in live DB. See inline script notes before using in a fresh-deploy pipeline.

---

### V002 — Create KPA/KPI Review Table
**Status:** ✅ Applied | **Migration:** `database/migration/V002_Create_Kpa_Kpi_Review.sql`

- New table `kpa_kpi_review` with 60+ columns
- Indexes: org+deleted, company+deleted, org+company+deleted, kpa name, status, due_date
- CHECK constraints on reporting_frequency, kpi_evaluation_frequency, status, loss percentage, tolerance range

---

### V003 — Risk Appetite & Acceptance Fields
**Status:** ✅ Applied (via Hibernate) | **Migration:** `database/migration/V003_Add_Risk_Appetite_And_Acceptance_Fields.sql`

Added to 12 tables:
- `risk_appetite_status VARCHAR(255)`
- `risk_acceptance_level VARCHAR(100)`

Tables: `erm_maturity_assessments`, `escalations`, `kri_kpi_reviews`, `kpa_kpi_review`, `risk`, `risk_assessment`, `risk_controls`, `risk_reviews`, `risk_sub_control`, `sub_risk`, `risk_treatment`, `risk_risponse_treatment`

> **⚠ Known bug:** Line targeting `risk_assessments` (plural) was a no-op. Live table is `risk_assessment` (singular). Columns exist because Hibernate applied them independently.

---

### V004 — Approval Decision Details
**Status:** ✅ Applied | **Migration:** `database/migration/V004_Add_Approval_Decision_Details.sql`

- `approval.root_cause TEXT NULL`
- `approval.action_taken TEXT NULL`

---

### V005 — Fix Custom Field Uniqueness (superseded)
**Status:** ⚠ Superseded by V007 | **Migration:** `database/migration/V005_Fix_Custom_Field_System_Field_Uniqueness.sql`

Partial attempt to drop global unique key. Does not handle the FK dependency. Retained for history only — **use V007** in any fresh-deploy pipeline.

---

### V006 — Currency, Value Unit, Risk Appetite Status Field Mappings
**Status:** ✅ Applied (partial) | **Migration:** `database/migration/V006_Add_Currency_Value_And_Risk_Appetite_Status_Fields.sql`

- Added `value_unit VARCHAR(30)` to `risk_reviews`, `kri_kpi_reviews`, `kpa_kpi_review`
- Inserted `system_fields` rows for `currency`, `valueUnit`, `riskAppetiteStatus`
- Inserted `fields` (category mappings) for Currency, Value, Risk Appetite Status

> **⚠ Known bug:** `ALTER TABLE risk_assessments` (line ~17) targets wrong plural table — was a no-op. `risk_assessment.value_unit` was added by V008.

---

### V007 — Drop Global SystemField Unique Constraint
**Status:** ✅ Applied | **Migration:** `database/migration/V007_Drop_Global_SystemField_Unique_Constraint_OrgCommand.sql`

Authoritative fix for field uniqueness:
1. Drops FK `FKetn47tjyqy7hocw7rmnuv13n7`
2. Drops global unique key `UKhlq90hlc2henmua6g7ytpo6vs`
3. Adds regular index `idx_fields_system_field_id`
4. Re-adds FK
5. Adds conditional unique index `idx_fields_active_category_system_field` (active rows only)

---

### V008 — Currency & Evaluation Dates on Risk Assessment
**Status:** ✅ Applied | **Migration:** `database/migration/V008_Add_Currency_And_Dates_To_RiskAssessment.sql`

- `risk_assessment.currency VARCHAR(10) NULL`
- `risk_assessment.last_evaluation_date DATE NULL`
- `risk_assessment.next_evaluation_date DATE NULL`
- `risk_reviews.currency VARCHAR(10) NULL`
- `system_fields` rows for `currency`, `lastEvaluationDate`, `nextEvaluationDate` (riskAssessment)
- `fields` (category mappings): Currency (Input Field), Last Evaluation Date (Date Picker), Next Evaluation Date (Date Picker)

---

### V009 — Metadata Field Options
**Status:** ✅ Applied (structure) | **Migration:** `database/migration/V009_Metadata_Field_Options.sql`

- 8 new metadata columns on `system_fields`: `display_label`, `field_type`, `default_value`, `validation_rules`, `is_required`, `is_read_only`, `is_hidden`, `display_order`
- New table `field_options` — stores selectable options per system field
- Seeds `valueUnit` dropdown: `RS/Rs.`, `THOUSANDS/Thousands`, `LAKH/Lakh`, `CRORES/Crores`, `MILLION/Million`, `BILLION/Billion`, `TRILLION/Trillion`
- Backfills `display_label` + `field_type` on existing `system_fields` rows

> **⚠ Gap:** `field_options` table is empty in live DB — seeding queries ran but row count is 0. The `GET /field/options` API will return empty arrays until this data is inserted. Run V009 data section manually or verify the seed executed correctly.

---

### V010 — ERM Maturity Grouping Key + Department IDs
**Status:** ✅ Applied | **Migration:** `database/migration/V010_Add_ErmMaturity_Id_And_Department_Ids.sql`

- `erm_maturity_assessments.erm_maturity_id VARCHAR(255) NULL` — group key for company/department clusters
- New table `erm_maturity_department_ids` (assessment ↔ departments M:N, cascade delete)
- Index `idx_ermaturity_org_erm_id_deleted` for 9-record-limit enforcement queries

---

### V011 — KRI/KPI Review → Risk Assessment Relationship
**Status:** ✅ Applied | **Migration:** `database/migration/V011_Alter_Risk_Assessment_Relationships.sql`

- `kri_kpi_reviews.risk_assessment_id BIGINT NULL` with FK to `risk_assessment(id)`
- One risk assessment can now link to multiple KRI/KPI reviews (1:N)

---

### V012 — Approval Workflow Dashboard & Reminders
**Status:** ✅ Applied | **Migration:** `database/migration/V012_Approval_Workflow_Dashboard_And_Reminders.sql`

- `approval.reminder_notified_at DATETIME NULL`
- Index `idx_approval_approver_status_due_deleted` — dashboard queries
- Index `idx_approval_status_reminder_deleted` — reminder scheduler queries
- Index `idx_approval_source` — source module/record lookup

---

## Backend Code Changes

### Role Management (`erm-command-organization`, port 8081)

**New files:**
- `model/RoleType.java` — entity for `role_type` table
- `repository/RoleTypeRepository.java` — `findByCode(String code)`
- `service/RoleTypeValidator.java` — validates role type codes; used in save flow
- `constant/RoleTypeCode.java` — enum of predefined codes

**Modified files:**
- `model/Role.java` — added `@ManyToOne` to `RoleType`
- `dto/requestDTO/RoleRequest.java` — added `String roleTypeCode`
- `dto/responseDTO/RoleResponse.java` — added `String roleTypeCode`
- `serviceimpl/RoleService.java` — enforces `roleTypeCode` on create; optional on update

**New endpoint:**
- `GET /role/roletype/all` — returns all `RoleType` rows for dropdown population

---

### KPA/KPI Review (`ERM_backend`, port 8086)

**New files:**
- `model/KpaKpiReview.java`
- `controller/KpaKpiReviewController.java`
- `serviceimpl/KpaKpiReviewService.java`
- `dto/riskDTO/KpaKpiReviewRequestDTO.java`
- `dto/response/KpaKpiReviewResponseDTO.java`

**New endpoints:**
- `POST /kpa-kpi-review` — create (fully validated with `@Valid`)
- `GET /kpa-kpi-review/{id}` — single record
- `GET /kpa-kpi-review?status=&search=` — paginated list (default page size 20, sort by id DESC)
- `DELETE /kpa-kpi-review/{id}` — soft delete

---

### Approval Workflow (`ERM_backend`, port 8086)

**Modified files:**
- `model/Approval.java` — added `rootCause TEXT`, `actionTaken TEXT`, `reminderNotifiedAt`, `triggerType`, `escalationSource`, `triggeredBy`, `triggeredAt`, workflow fields
- `dto/riskDTO/ApprovalDecisionRequest.java` — added `rootCause`, `actionTaken`
- `dto/response/ApprovalResponse.java` — added all new workflow fields
- `dto/response/ApprovalDashboardResponse.java` — new DTO: `pending`, `upcomingDue`, `overdue`, `history`

**New endpoints:**
- `POST /approvals/{id}/trigger` — manually trigger workflow
- `POST /approvals/{id}/escalate` — escalate approval
- `POST /approvals/{id}/reminder` — send reminder to approver
- `GET /approvals/dashboard` — combined dashboard response
- `GET /approvals/dashboard/upcoming-due` — due within 7 days
- `GET /approvals/dashboard/overdue` — past due date
- `GET /approvals/dashboard/history` — completed decisions
- `GET /approvals/login-target` — login redirect target for approver

---

### Field Configuration (`ERM_backend`, port 8086)

**New files:**
- `model/SystemField.java` — updated with 8 metadata fields
- `model/FieldOption.java` — new entity for `field_options` table
- `repository/FieldOptionRepository.java`

**Modified files:**
- `serviceimpl/FieldService.java`:
  - `saveField()` — pre-save duplicate validation (checks DB for field name and system-field-id duplication within same category)
  - `getFieldOptions()` — new method, returns active options for a named field in a named system table
  - `enrichWithOptions()` — batch-loads options to avoid N+1

**New endpoint:**
- `GET /field/options/{tableName}/{fieldName}` — returns dropdown options for metadata-driven UI rendering

---

### Risk Assessment / Review / KRI entities (`ERM_backend`)

**Modified entities** — added fields confirmed in live DB:

| Entity | New Fields |
|--------|-----------|
| `RiskAssessment` | `riskAppetiteStatus`, `riskAcceptanceLevel`, `valueUnit`, `currency`, `lastEvaluationDate`, `nextEvaluationDate` |
| `RiskReview` | `riskAppetiteStatus`, `riskAcceptanceLevel`, `valueUnit`, `currency`, `lastEvaluationDate`, `nextEvaluationDate` |
| `KriKpiReview` | `riskAppetiteStatus`, `riskAcceptanceLevel`, `valueUnit`, `currency`, `riskAssessmentId` |
| `Risk` | `riskAppetiteStatus`, `riskAcceptanceLevel` |
| `RiskControls` | `riskAppetiteStatus`, `riskAcceptanceLevel` |
| `RiskSubControl` | `riskAppetiteStatus`, `riskAcceptanceLevel` |
| `RiskTreatment` | `riskAppetiteStatus`, `riskAcceptanceLevel` |
| `RiskResponseTreatment` | `riskAppetiteStatus`, `riskAcceptanceLevel` |
| `SubRisk` | `riskAppetiteStatus`, `riskAcceptanceLevel` |
| `Escalation` | `riskAppetiteStatus`, `riskAcceptanceLevel` |
| `ErmMaturityAssessment` | `riskAppetiteStatus`, `riskAcceptanceLevel`, `ermMaturityId`, `departmentIds` |

All corresponding request DTOs and response DTOs updated with matching fields.

---

## Open Issues / Gaps

| # | Issue | Severity | Action Required |
|---|-------|----------|----------------|
| 1 | `field_options` table is empty | High | Run V009 seed section, or seed manually. `GET /field/options` returns `[]` until fixed |
| 2 | `role.role_type_id` is nullable | Medium | Decide: enforce NOT NULL? Create a corrective V013 migration |
| 3 | `GET /role/roletype/all` returns empty list | High | Bug in `RoleService.getRoleType()` — calls `findAll()` but returns `List.of()`. Fix needed before UI can use this endpoint |
| 4 | V001 script invalid for fresh deploy | Low | Fix dual PK and SEQUENCE syntax before using in new environments |
| 5 | V003 wrong table name (`risk_assessments`) | Low | Fix for fresh deploy pipeline — no live impact |

---

## Migration Execution Order (Fresh Environment)

```
V001 → V002 → V003 → V004 → V005* → V006 → V007 → V008 → V009 → V010 → V011 → V012
```

> \* V005 may be skipped — V007 supersedes it completely. If included, V005 must run before V007.

---

## Files Removed in This Cleanup (2026-08-01)

The following stale documentation files were deleted from the project root:

- `APPROVAL_WORKFLOW_LAST_CHANGES.md`
- `BACKEND_APPROVAL_WORKFLOW_IMPLEMENTATION.md`
- `CHANGE_LOG_AND_TEST_GUIDE.md`
- `COMPLETE_RESOLUTION.md`
- `DATABASE_AND_UI_CHANGE_IMPLEMENTATION.md`
- `ERM_DEVELOPER_REFERENCE.md`
- `FIELD_CONFIGURATION_INSERTS.md`
- `FIX_IMPLEMENTATION_SUMMARY.md`
- `FIX_SUMMARY.md`
- `README_ROLETYPE_IMPLEMENTATION.md`
- `ROLETYPE_CHANGES_SUMMARY.md`
- `ROLETYPE_IMPLEMENTATION.md`
- `ROLETYPE_QUICK_REFERENCE.md`
- `ROOT_CAUSE_ANALYSIS.md`
- `dataformapping.txt`

The following SQL scripts were moved from `ERM_backend/scripts/` to `database/migration/`:

- `add_erm_maturity_id_and_department_ids.sql` → `V010_Add_ErmMaturity_Id_And_Department_Ids.sql`
- `alter_risk_assessment_relationships.sql` → `V011_Alter_Risk_Assessment_Relationships.sql`
- `approval_workflow_dashboard_and_reminders.sql` → `V012_Approval_Workflow_Dashboard_And_Reminders.sql`
