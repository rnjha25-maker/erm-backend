# Period evaluation workflow

Swagger: `/erm/v3/api-docs/period-workflow` through the gateway, or
`/v3/api-docs/period-workflow` directly on the backend. The gateway Swagger menu
includes **ERM Period Workflow & Dashboard**. These routes were verified locally;
deployed availability requires deploying the backend and gateway documentation.

Frontend handoff: [implementation guide](FRONTEND_PERIOD_WORKFLOW_IMPLEMENTATION.md).
Review findings: [architecture review](PERIOD_WORKFLOW_ARCHITECTURE_REVIEW.md).

## Scope and compatibility

This is an additive workflow for CONTROL, TREATMENT, REVIEW, KRI and KPI. Risk Identification
and Risk Assessment are not supported as period modules: they remain the baseline.
Existing category save/edit APIs keep their existing behaviour. New period APIs never
write to category rows or reassign subrisk, subcontrol or approval foreign keys.

Deploy `dashboard-chart-reporting-period.sql` and `period-evaluation.sql` before deploying
the backend. The scripts are one-time MySQL 8 migrations; review existing columns/indexes
before applying. No production/shared database migration has been run by this work.
Frontend changes are still required to offer period selection and call these endpoints.

One intentional protection affects existing evidence deletion: a file referenced by
period history cannot be physically deleted through the treatment endpoint. Unreferenced
files retain the existing behaviour after checking that the document belongs to the
specified treatment and tenant. Direct storage-administrator deletion is outside
this API's control and should follow the same retention policy.

## Start a period

`POST /erm/period-evaluations/start`

```json
{
  "module": "REVIEW",
  "sourceId": 123,
  "year": 2026,
  "yearType": "FINANCIAL",
  "frequency": "QUARTERLY",
  "periodNumber": 2
}
```

`sourceId` always refers to the original category record, not the last evaluation ID.
Year means starting year; financial 2026 runs April 2026–March 2027. Period numbers are
1–12 for MONTHLY or 1–4 for QUARTERLY, relative to that year convention. Selecting a
period is explicit: no rollover occurs merely because the date changes.

The first call snapshots source scalar values, selected subrisk identities/names,
subcontrols/tags for controls, and prior approval details. Treatment evidence references
are retained. A subsequent call for the same source links the previous completed period
and preserves its original source snapshot and values as context. New evaluation values
start empty: a prior completion/approval is never treated as the new period's result.

There can be only one draft at a time per source. Subsequent periods must be chronological
and non-overlapping. Skipped periods are allowed, but older gaps cannot be backfilled once
later history exists. The year convention cannot switch within a history; frequency is
fixed within a reporting year. The source row is locked for concurrent starts; a database
unique key also rejects duplicate period identities. Duplicates/conflicts return 409.
Starting another period also rejects a source reassigned to a different risk after
history began; use a separate source record to preserve the original history identity.

For KRI/KPI, tag the existing annual entry with `reportingYear` and `reportingYearType`
first; the requested year and evaluation frequency must match that entry. Create a
separate annual category entry for a new year. The annual target is read-only through
period APIs, and remains frozen in the period source snapshot.

## Edit and complete

`GET /erm/period-evaluations/{id}` returns the existing SUCCESS/data response wrapper.
The data includes `revision`, `state`, `source_snapshot`, `evaluation_values`,
`editableFields` and an evidence manifest. These snapshot/field names use database
snake_case; they are a new API contract, not the legacy DTO field names.

`PATCH /erm/period-evaluations/{id}` merges only explicitly supplied allowed fields:

```json
{
  "revision": 0,
  "values": {
    "review_type": "Quantitative",
    "currency": "INR",
    "value_unit": "CRORES",
    "annual_loss_expectancy": 50
  }
}
```

Use `editableFields` to map the appropriate form. Identity, tenant, baseline relationships,
targets, snapshot content and approval decisions cannot be edited. Existing values not
included in the patch are preserved; an explicit null clears a draft value. Numeric
fields require JSON numbers. Payloads cannot contain arbitrary nested objects. Subrisk
and subcontrol associations are retained context, not editable through this workflow.

`POST /erm/period-evaluations/{id}/complete`

```json
{"revision": 1, "effectiveDate": "2026-08-15"}
```

Completion requires an effective date inside the selected period, including for late
data entry. Completion has minimum module validation: control title, treatment status,
review type and residual result, or KRI/KPI actual value. Quantitative review requires
ALE, currency and value unit; qualitative review requires a residual rating. Existing
dynamic UI form rules should still be applied by the category UI. New APIs do not
recompute all business-derived scores; supply the evaluated values as in existing forms.

Every mutation increments revision. Sending an old revision returns 409; reload rather
than overwriting another user's work. Completed evaluations are immutable. There is no
reopen, correction-revision or deletion endpoint in this initial workflow. Confirm the
values before completing; production adoption must account for this restriction.

## Evidence and approvals

- Upload to `POST /erm/period-evaluations/{id}/evidence` as multipart `file`, `revision`
  and optional `description`. Maximum 10MB. It is a draft-only action and increments revision.
- Read/download via `GET /erm/period-evaluations/{id}/evidence/{documentId}`. Access and
  document association are checked before contacting storage. Never attach arbitrary
  document IDs from the client. Inherited evidence is explicitly marked `inherited`.
- Uploads append documents; there is no evidence removal endpoint in this initial
  workflow. A storage upload that succeeds before a database failure may leave an
  unattached storage object; storage cleanup should handle these without deleting retained files.
- Approval is a separate existing approval operation after completion, using
  `sourceModule: "periodEvaluation"` and `sourceRecordId: "<evaluation id>"`. Existing
  approval notifications run only when that operation is requested. Start/complete
  themselves do not send notifications or copy an approval decision.
- Approver and submitter must belong to the selected company. Assigned approvers can
  read that evaluation but assignment does not grant editing rights. Existing source
  approvals remain unchanged; prior decisions are historical context only.

## History, access and charts

`GET /erm/period-evaluations?module=REVIEW&sourceId=123&page=0&size=20`
returns chronological summaries, newest first. Size is capped at 100. Fetch individual
records for values and evidence. Company context is required for all period APIs, along
with authenticated organization/user context. Organization/company scope and existing
dashboard role restrictions apply. Source creator/period creator is used for basic users;
advanced users use assigned branch OR department. Snapshot history remains readable after
an ordinary category edit; it is not reconstructed from the current record.

Only COMPLETED evaluations feed charts. Completion is distinct from approval, consistent
with existing chart reads; charts do not claim that every included evaluation is approved.
Residual exposure combines legacy reviews and new review evaluations and selects one
latest result per risk. On an equal effective date, period history takes precedence over
legacy rows. Drafts and repeated quarters do not inflate exposure. Inherent exposure
still comes from the unchanged assessment baseline.

KRI/KPI charts overlay completed period actuals on the frozen annual source values and
target, preserving untouched month/quarter values from that snapshot. Existing annual
category rows are not updated by these APIs. The UI must read the period API after opting
into this workflow rather than expecting the legacy category grid to reflect new values.
There is no new standalone control/treatment chart.

## Verification

Unit/HTTP tests cover date boundaries, stale/completed edits, numeric validation and errors.
Opt-in MySQL tests exercise transactional start/edit/complete, source/subrisk/approval
preservation, cross-tenant and role restrictions, retained evidence, and chart integration.
Run with `ERM_CHART_DB_TEST=true` only against the disposable loopback database on port
3308. Tests never use the application's configured QA/production datasource. Performance
requires representative staging load validation; local functional checks are not a benchmark.
