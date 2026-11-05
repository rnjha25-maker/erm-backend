# Frontend implementation guide: period evaluations and dashboard

## Prerequisites and Swagger

Backend migrations must be applied before enabling this UI. Use the application's existing
API base URL and authentication client. Gateway requests use `/erm` as shown below.

- Gateway OpenAPI: `/erm/v3/api-docs/period-workflow`.
- Direct ERM OpenAPI: `/v3/api-docs/period-workflow` on the backend host/port (8086 by default).
- Full ERM OpenAPI remains available at `/erm/v3/api-docs` through the gateway.
- Gateway Swagger UI menu now includes **ERM Period Workflow & Dashboard**.
- Tags: **Period evaluations** and **Dashboard charts**. Select the `/erm` server when
  using the gateway, `/` only for direct trusted-backend verification.

Swagger generation was checked with a real local HTTP test, including all 11 operations
and the multipart request. This does not verify a currently deployed server. Backend
deployment and gateway menu deployment must both include these changes.

Do not fabricate `X-User-Id`, `X-Org-Id` or `X-Cmp-Id` from a user-editable form. These are
trusted backend context, not authorization controls in the UI. Continue the established
authenticated HTTP-client flow. The architectural review flags the existing gateway
header-trust configuration as a production security dependency.

## UI behaviour

1. Keep the existing **Edit** action for normal legacy record corrections.
2. Add **Period evaluations** to Control Assessment, Risk Response & Treatment,
   Risk Review & Evaluation, Risk & KRI and KRA/KPI. Map these to CONTROL, TREATMENT,
   REVIEW, KRI and KPI respectively. Do not add this action to Risk Identification or
   Risk Assessment; those remain the baseline.
3. Open period history for the selected original record. Keep its `sourceId` separate
   from each new evaluation's `id` in client state. They are different ID namespaces.
4. Offer year convention, starting year, frequency and period number. Monthly is 1–12;
   Quarterly is 1–4. Financial 2026 means April 2026–March 2027. Financial monthly period
   1 is April; period 12 is March. Financial Q4 is January–March 2027.
5. If a draft exists, offer **Continue draft** rather than trying to start another.
6. Show source values, previous-period values, subrisk/subcontrol links and prior approval
   information in a read-only context panel. Do not bind these directly into new actuals
   or silently carry forward a prior completed/approved status.
7. Edit only `evaluation_values`, using `editableFields` from the response. Keep the
   existing module-specific labels, configured dropdowns and calculation helpers. This
   endpoint uses snake_case keys, unlike the legacy camelCase category DTOs.
8. Save draft changes with the latest `revision`. Replace the local evaluation object
   with the returned data after each successful save or evidence upload.
9. Require a final review and effective date before **Complete evaluation**. Completion
   locks values permanently in this version. It is not approval. There is no cancel,
   delete, reopen, correction-revision or backfill action: do not display those buttons.
10. Offer the existing approval form separately after completion. Start/complete do not
    send notifications or inherit approval. Show the actual approval record's status.

Periods must be created in chronological order; skips are allowed but a skipped older
period cannot be filled after later history exists. Year convention cannot change within
a history; frequency cannot change within its reporting year. Warn before starting the
wrong period because there is currently no draft cancellation endpoint.

## Request sequence

Read summaries:

```http
GET /erm/period-evaluations?module=REVIEW&sourceId=123&page=0&size=20
```

The `data` value is an array, not a Spring Page. Newest first; no total/hasNext is returned.
Show Next when a full page is received; an empty next page means the end. Maximum size 100.
Read one evaluation with `GET /erm/period-evaluations/{id}`.

Start:

```http
POST /erm/period-evaluations/start
Content-Type: application/json

{
  "module": "REVIEW",
  "sourceId": 123,
  "year": 2026,
  "yearType": "FINANCIAL",
  "frequency": "QUARTERLY",
  "periodNumber": 2
}
```

Save a draft (illustrative evaluation ID 501):

```http
PATCH /erm/period-evaluations/501
Content-Type: application/json

{
  "revision": 0,
  "values": {
    "review_type": "Quantitative",
    "annual_loss_expectancy": 50,
    "currency": "INR",
    "value_unit": "CRORES"
  }
}
```

Only changed allowed keys need to be sent. Omitted keys remain unchanged. Send null to
clear a draft value. Numeric actuals/ALE inputs must be JSON numbers, not formatted text
such as `"1,000"`, currency symbols, NaN, or an empty string. Zero is valid.
Currencies use uppercase three-letter codes; value units use backend enum codes.

Complete using the revision returned by the most recent mutation:

```http
POST /erm/period-evaluations/501/complete
Content-Type: application/json

{"revision": 1, "effectiveDate": "2026-08-15"}
```

The effective date must be inside July–September 2026 for this example. Use a date-only
`YYYY-MM-DD` value, not a timezone-shifted JavaScript timestamp.

## Field mapping

`editableFields` is authoritative. These examples cover completion requirements:

- CONTROL: `control_title`; additional control type/mechanism, source, control details,
  appetite and acceptance fields are available. Subcontrol associations are read-only.
- TREATMENT: `risk_treatment_status`; control gaps, recommended control, management plan,
  contingency, effectiveness, evidence description, evaluation and status fields are available.
- REVIEW: `review_type` is `Quantitative` or `Qualitative`. Quantitative requires
  `annual_loss_expectancy`, `currency`, `value_unit`; qualitative requires
  `residual_risk_rating`. Revised likelihood/impacts and other review outputs are editable.
- KRI: `actual_value`, with optional appetite/tolerance/status fields.
- KPI: `actual_value`, with optional appetite, KRA rating and status fields. KPI represents
  the KRA performance; no separate KRA chart is needed.

The backend enforces minimum completion rules and primitive types/ranges. It does not
currently reproduce every dynamic mandatory-field rule or recalculate every derived risk
score. Keep the existing configured module validation/calculations in the UI; additional
server-side business validation is a review item, not something the UI can guarantee alone.

KRI/KPI source annual entries must already have `reportingYear`, `reportingYearType`, and
matching evaluation/reporting frequency. Use the existing category save/get APIs for that
metadata. A new year requires a separate annual source entry. Do not pass another year's
source ID to start. The period API stores one `actual_value` for the selected period;
do not send `q2`, `january` or `target_value` to the new PATCH endpoint.

## Client contracts and Angular call pattern

```ts
type Module = 'CONTROL' | 'TREATMENT' | 'REVIEW' | 'KRI' | 'KPI';
type YearType = 'CALENDAR' | 'FINANCIAL';
type Frequency = 'MONTHLY' | 'QUARTERLY';
interface ApiResponse<T> { status: 'SUCCESS' | 'FAILED'; data: T; message?: string; }
interface PeriodEvaluation {
  id: number;
  module: Module;
  source_id: number;
  reporting_year: number;
  year_type: YearType;
  frequency: Frequency;
  period_number: number;
  period_start: string;
  period_end: string;
  state: 'DRAFT' | 'COMPLETED';
  revision: number;
  source_snapshot: Record<string, unknown>;
  evaluation_values: Record<string, string | number | null>;
  editableFields: string[];
  evidence: { document_id: string; inherited: boolean; description?: string }[];
}

// apiBase should already refer to the application's gateway.
saveDraft(e: PeriodEvaluation, changed: Record<string, string | number | null>) {
  return this.http.patch<ApiResponse<PeriodEvaluation>>(
    `${this.apiBase}/erm/period-evaluations/${e.id}`,
    { revision: e.revision, values: changed }
  );
}

uploadEvidence(e: PeriodEvaluation, file: File, description: string) {
  const body = new FormData();
  body.append('file', file);
  body.append('revision', String(e.revision));
  body.append('description', description);
  return this.http.post<ApiResponse<PeriodEvaluation>>(
    `${this.apiBase}/erm/period-evaluations/${e.id}/evidence`, body
  );
}
```

Do not manually set multipart Content-Type; the browser supplies its boundary. Uploads
append evidence, allow at most 10MB per file, and only work on drafts. Inherited evidence
must be clearly labeled as prior-period evidence. There is no removal endpoint in this
version. Fetch associated storage metadata at
`GET /erm/period-evaluations/{id}/evidence/{documentId}`. It is a JSON response, not a Blob
download; integrate the returned storage metadata with the existing document viewer.

## Approval integration

Use the existing approval endpoint after completion:

```json
{
  "sourceModule": "periodEvaluation",
  "sourceRecordId": "501",
  "submitterId": 30,
  "approverId": 31,
  "recordName": "Risk review - FY2026 Q2",
  "pageLink": "/webapp/erm?periodEvaluationId=501",
  "triggerType": "AUTOMATIC"
}
```

The shown `pageLink` is a proposed frontend deep link, not an existing implemented route.
Implement its handler to load the exact evaluation before using it. IDs must come from
the authenticated user and allowed company user directory; do not hardcode this example.
Submitter and approver must differ and belong to the selected company. Existing approval
notification/decision behaviour remains in that API. An assigned approver's read access
does not grant period-edit permission.

## Errors and recovery

- 400: show the validation message; preserve unsaved form data. Missing or invalid context
  can also appear as a 400 through existing backend error handling.
- 404: record/document is unavailable or outside caller scope; return to history.
- 409: reload the evaluation/history and show a conflict. Do not automatically replay
  an edit against a newer revision or turn a failed Start into an Edit.
- Upload/network timeout: fetch the evaluation and evidence manifest before retrying;
  the upload may have completed. Do not blindly retry mutations.
- Completion has no undo. Explain that before the user confirms it.

Use a request-in-progress guard to disable duplicate Start/Save/Complete/Upload clicks.
Keep draft saving and approval submission separate; a successful completion does not
mean approval creation succeeded.

## Dashboard wiring and acceptance checks

Use the new `/erm/dashboard/charts/*` APIs described in `DASHBOARD_CHART_API.md`. Only
completed period records contribute; draft changes must not appear in charts. Completion
does not imply approval. Refresh relevant charts after completion.

- Keep one target line for the year. Plot only the selected monthly or quarterly actuals.
- For `PERIOD_HISTORY` series, trust returned values; do not overlay legacy form values
  again. The API applies the frozen target/source values and completed period actuals.
- Do not render null as zero or combine different currency/unit groups.
- Show a warning for `ENTRY_CREATED_AT_FALLBACK`; it is not confirmed historical year data.
- Inherent exposure uses the retained baseline. Residual uses the latest review in the
  selected period; no review means missing, not a fabricated carried-forward value.

Before release test: continue an existing draft; reload saved values; cross-year financial
Q4; Monthly period 12; duplicate clicks; two-tab stale revision; immutable completion;
evidence viewing before/after the next period; assigned approver read-only access; wrong
company/source ID; source risk reassignment; zero actuals; missing values; and unchanged
legacy Edit, evidence, delete and approval screens. The legacy category grid does not
automatically display new period values—use an explicit period-history view.
