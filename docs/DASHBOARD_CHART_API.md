# Dashboard chart API — backend handoff

The existing `/dashboard/erm-summary` contract is unchanged. New read-only endpoints use
the same organization/user context and role scope. Through the gateway, prefix paths below
with `/erm`. The UI still needs to replace its static chart data with these responses.

## Requests

- `GET /dashboard/charts/performance?year=2026&yearType=CALENDAR&indicator=KRI&frequency=MONTHLY`
- `GET /dashboard/charts/performance?year=2026&yearType=FINANCIAL&indicator=KPI&frequency=QUARTERLY&quarter=2`
- `GET /dashboard/charts/exposure?year=2026&yearType=FINANCIAL`
- `GET /dashboard/charts/trend?year=2026&yearType=CALENDAR`
- `GET /dashboard/charts/scatter?year=2026&yearType=CALENDAR&quarter=3`

`year` is required (1900–2200). `yearType` defaults to `CALENDAR`; `FINANCIAL` year 2026
means April 2026 through March 2027. Dates use a start-inclusive, end-exclusive interval.
Omit `quarter` for annual view; use 1–4 for a quarter relative to the chosen year type.
Financial Q4 2026 is January–March 2027. There is no separate `ANNUAL` entry frequency.

All endpoints accept optional positive `companyId`, `branchId` (Location), and
`departmentId` (Function). Omitting these means all records **within the user's scope**.
Company and advanced users cannot select another company. Advanced scope is assigned
branch OR assigned department, intersected with requested filters. Basic users see
creator-scoped records; org admins remain organization-scoped. Missing assignments
never grant unrestricted access. No caller-supplied SQL identifiers are accepted.

Performance and scatter accept `page` (default 0) and `size` (default 20, maximum 100).
Responses provide `hasNext`; fetch subsequent pages when offering indicator selection.
There is no expensive total-count query. Standard response wrapper: `{ "status": "SUCCESS", "data": ... }`.
Invalid enums, missing required parameters, invalid year/quarter/page/size return HTTP 400.

## Performance: KRI and KPI, no separate KRA chart

`indicator` is `KRI` or `KPI`; `frequency` is `MONTHLY` or `QUARTERLY`.
Each series represents one annual record. It contains `recordId`, indicator name, currency,
value unit, stored measurement unit/description, a single `target`, aligned `actuals`,
`invalidFields`, and `yearBasis`. Use the one target across all displayed periods; do not
sum it over months or quarters. KPI describes performance for its KRA.

Monthly labels are ISO year-month strings, e.g. `2026-04` through `2027-03` for financial
2026. Quarterly labels are Q1–Q4. The selected frequency determines which stored columns
are read. A monthly record is not silently converted to quarterly sums or averages.
Evaluation frequency is authoritative; reporting frequency is a legacy fallback only
when evaluation frequency is absent. The reporting person is unrelated to this filter.

Null/blank actuals remain null; zero remains zero. Non-numeric legacy KRI actuals become
null with their field names in `invalidFields`. Do not draw null as zero. Series are
returned separately: summing heterogeneous indicators, ratios, currencies, or units is
not a valid company-wide performance calculation. An overall normalized KPI score needs
an agreed weighting/normalization rule and is not invented here.

KRI Location comes from the linked risk's branch; KPI Location comes from the functional
owner's current branch because KPI entries have no branch snapshot. Historical owner
transfers can therefore affect a location-filtered KPI result. Department filters use
stored department identifiers in `business_function`; legacy free-text department values
must be corrected separately to support ID filtering reliably.

## Reporting-year metadata on existing create/update APIs

KRI and KPI request/response DTOs now accept optional fields together:

```json
{
  "reportingYear": 2026,
  "reportingYearType": "FINANCIAL",
  "reportingFrequency": "QUARTERLY"
}
```

Also send the matching `kriEvaluationFrequency` or `kpiEvaluationFrequency`.
Year-tagged entries require matching Monthly or Quarterly frequencies. One field without
the other is rejected. Updates omitting both preserve existing metadata. A tagged entry
cannot be moved into another year/year type; create a separate annual entry instead.
Targets may still be corrected through existing update flows; they are constant across
periods within the chart, not locked against business corrections.

Explicit metadata determines year selection even for late-created entries. Their
`yearBasis` is `EXPLICIT_REPORTING_YEAR`. Untagged legacy entries use creation date and
are marked `ENTRY_CREATED_AT_FALLBACK`. **That fallback is not verified historical data**:
show a warning and have the business confirm/tag the entry. Untagged quarterly records
also lack a calendar/financial convention; changing dashboard year type cannot establish
what Q1 originally meant. No automatic historical backfill is performed.

## Quantitative exposure

Inherent uses assessment `yearlyLossExpectancy`; residual uses review
`annualLossExpectancy`. Only quantitative rows count. Risk Identification and Risk
Assessment are retained baselines, as clarified on 7 October. Inherent exposure uses the
latest stored quantitative baseline before the selected period's end, even if originally
assessed in a previous quarter/year. It does not require another assessment each quarter.
Residual exposure uses the latest quantitative review **within** the selected period.
Both are selected per distinct risk using last evaluation date, then creation timestamp,
then ID. If evaluation date is absent, creation date is the fallback. Future-dated rows
are excluded. Inherent and residual are selected independently; one side can be missing.
For annual residual exposure, select the latest available review per risk, never sum
the quarterly snapshots. An unreviewed quarter returns no residual value; carry-forward
of prior-period reviews has not been confirmed.

Example: risk A's baseline ALE is 100 and risk B's baseline is 50. Inherent exposure
remains 150 across quarters unless the baseline is explicitly corrected or a new risk
is identified. If A has review ALE values 80, 70, 60, 40 and B's latest review is 20,
annual residual exposure is 60, not the sum of the quarterly review values.

`totals` groups by currency and value unit; never add these groups together without
approved conversion rules. Unknown units/currencies remain explicitly null. Each group
includes inherent/residual values, risk counts and missing-value counts. A missing latest
ALE is not replaced with an older value or zero. A partial sum must be shown with its
missing count. Review and assessment values have no fabricated relationship or percentage.

The earlier assumption of recreating Risk Assessment up to four times a year is superseded
by the clarification: periodic changes belong to Control Assessment, Risk Response,
Risk Review, KRI and KPI. These read APIs do not introduce a four-assessments write limit.
Existing explicit baseline corrections remain possible. Without version history, a
correction can affect a historical chart; these APIs cannot reconstruct overwritten values.

## Trend and scatter

Trend counts newly created risks and assessments by month in the selected period,
including assessments on older risks. Empty buckets are zero. It is explicitly labeled
`CREATED_RECORDS`, not a historical outstanding-risk balance.

Scatter provides one latest baseline assessment per risk as of period end, its category and raw
likelihood/gross impact/rating. It does not invent coordinates or map qualitative text
to numeric levels. The UI must use the configured scale consistently; agree a normalized
scale before combining different assessment types on the same axes.

## Next-period evaluation workflow — remaining integration work

Mapping a date can derive a period only when the evaluation's effective date and company's
calendar/financial convention are known. Creation/update timestamps cannot reliably
identify late or backdated evaluations. Dashboard filters alone do not select a period
for category-page data entry.

- Retain the risk and baseline assessment IDs across periods, for quantitative and
  qualitative risks. Do not automatically clone or reset their statuses.
- Category pages need a visible reporting-year and month/quarter context. The backend
  may propose the current period, but users must be able to identify the period they are
  completing. The UI must submit that context, including for late evaluations.
- KRI/KPI already store monthly/quarterly actual columns. Preserve the annual target
  and other periods' actuals when editing one period; a new year gets a separate annual
  entry and explicit reporting metadata. Existing saves accept a whole annual record,
  so the UI must send the other populated periods too. They are not partial period updates.
- Controls, treatment and reviews need separately retained evaluation snapshots and
  period-aware reads/writes. Existing save calls with the same ID update the same row.
  Some subrisk associations point to a single current record, so simply creating another
  row can also move the subrisk link away from the earlier record. Snapshot relationships
  and evidence/approval associations need explicit preservation before a next-period
  action can safely be offered.

The additive next-period workflow is now implemented separately; see
`PERIOD_EVALUATION_API.md` for start/edit/complete/history/evidence and approval integration.
The category UI still needs to call those APIs. Existing category edits retain their
previous behaviour; they do not automatically become period evaluations. Completed period
reviews feed residual exposure; KRI/KPI period values overlay frozen annual source values.
The additional `period-evaluation.sql` migration is required before deployment.

## Deployment and performance

1. Review/apply `database/dashboard-chart-reporting-period.sql` through normal database
   migration procedures before deploying this backend. It is a one-time script, not
   idempotent. The added nullable columns preserve old rows. Inspect existing indexes
   before adding the proposed chart indexes. No shared database was modified by this work.
2. Deploy the backend, then wire the UI to these APIs and send reporting metadata on new
   annual entries. Frontend code has not been modified in this change.
3. Confirm legacy year assignments and historical snapshot requirements before treating
   older-year charts as complete financial reporting.

Queries aggregate/rank in MySQL 8 and return scalar results. Performance/scatter use one
bounded result query; KRI/KPI also use one batched period-history query for that page.
Exposure/trend each use two aggregate queries. There are no
per-record lookups or loaded entity collections. User-role resolution is a fixed-cost
lookup, separate from chart queries. Validate EXPLAIN plans and latency on representative
staging volumes before rollout; local functional tests are not a load benchmark.

Tests use unit/MockMvc coverage plus opt-in MySQL tests against loopback port 3308, with
session temporary fixture tables cloned from a local Hibernate-created schema. Enable
only with `ERM_CHART_DB_TEST=true`; they never connect to configured QA/production URLs.
