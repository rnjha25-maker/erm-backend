# ERM dashboard overview — frontend integration

New additive API for the supplied ERM_DASHBOARD_UI_DATA_CONTRACT. Existing summary,
register, charts, CRUD and period-evaluation APIs retain their routes and response shapes.
No schema migration is required beyond the dashboard/period migrations already deployed.

## Request

`GET /erm/dashboard/overview?year=2026&yearType=CALENDAR&periodType=YEAR&indicator=KRI&frequency=MONTHLY`

Send the normal `Authorization: Bearer …` header to the gateway. Organization/user
scope is derived from the authenticated context, not from query parameters.

- `year`: required, 1900–2200. For a financial year this is the starting year.
- `yearType`: `CALENDAR` (default) or `FINANCIAL` (April–March).
- `periodType`: `YEAR` (default), `H1`, `H2`, `Q1`, `Q2`, `Q3`, `Q4`.
  Financial Q1 means April–June; financial H2 means October–March.
- `companyId`, `branchId`, `functionId`: optional positive IDs. Omit for all records
  permitted by the user's role. Do not send zero or the string `null`.
- `indicator`: `KRI` (default) or `KPI`.
- `frequency`: `MONTHLY` (default) or `QUARTERLY`. Half-year/year are reporting
  windows, not additional evaluation frequencies. The document's All Charts,
  Half-Yearly and Yearly evaluation choices are not supported enum values.
- `page=0`, `size=20`: risk-register pagination; size 1–100.
- `performancePage=0`, `performanceSize=20`: independent indicator pagination;
  size 1–100.
- `performanceRecordId`: optional ID from the requested performance page, to populate
  the single-line-chart `performance` array. An ID absent from that page returns 400.

The overview is read-only. Pagination never alters the summary/chart counts. The
performance tab selects KRI or KPI; call the endpoint again when that tab changes.

## Response

The envelope is `{ "status": "SUCCESS", "message": null, "data": { ... } }`.
The `data` keys follow the reference document:

- `totalRisks`, `withinRiskAppetite`, `riskToleranceBreached`: unique risk counts.
- `riskLevelSummary`, `riskSummaryPriorityBased`, `riskByCategory`, `riskBySource`,
  `riskByImpact`, `treatmentStrategy`, `assessmentType`, `riskAppetiteStatus`:
  `{ "key": "HIGH", "displayLabel": "High", "count": 2 }` items.
- `ratingWiseStatus`: `{ "label": "High", "Open": 0, "In Progress": 1,
  "Closed": 0, "Unknown": 1 }` rows.
- `riskHeatmap`: existing axes/cells structure with 25 cells, counts, risk IDs and
  plotted/unplotted totals. Severity keys use `MEDIUM`; impact-axis keys still use
  `MODERATE`. Extra heatmap metadata remains available.
- `riskRatingByLocation`, `riskRatingByCategory`, `functionWiseRiskRating`,
  `riskRatingByOwner`: `{ "key": "50", "displayLabel": "ERM", "total": 2,
  "breakdown": [ ...count items... ] }`.
- `financialExposureByRisk`: `{ "riskId": 1, "riskTitle": "Example",
  "annualLossExpectancy": 120, "currency": "INR", "valueUnit": "CRORES",
  "basis": "RESIDUAL" }`. Currency and value unit must be displayed together.
  No currency/unit conversion is performed. ALE may be null when the latest value
  is missing; it must not be rendered as zero.
- `ermMaturityFunctionWise`, `ermMaturityCompanyWise`: existing maturity summary
  shapes with ID, displayLabel, totalWeightageScore and overallMaturityLevel.
- `riskRegister`: flat rows with riskId, riskTitle, category, owner, likelihood,
  impact, inherentScore, residualScore, rating and status.
- `riskRegisterPage`: `{ "page": 0, "size": 20, "totalElements": 76,
  "hasNext": true }`.

### Performance: preserve separate indicators

`performanceSeries` contains one item per indicator record:

```json
{
  "recordId": 123,
  "indicator": "Customer incidents",
  "currency": null,
  "valueUnit": null,
  "measurementUnit": "Count",
  "points": [
    { "period": "Jan", "actual": 4, "target": 5 },
    { "period": "Feb", "actual": null, "target": 5 }
  ],
  "invalidFields": []
}
```

`performancePage` supplies page, size and hasNext. `performance` uses exactly the
reference document's array of period/actual/target rows, but is populated only
when a record is explicitly selected, or when the entire result contains exactly
one record. Multiple records are never averaged or summed into a fabricated line.
An absent actual is null; an actual zero stays zero. The annual target repeats
across all displayed periods. H1/H2/Q windows slice the annual series.

### Unresolved business definitions

`overdueEvaluations`, `upcomingReview`, `totalKris` and `highRiskKris` currently
return **null**, with a per-field explanation in `unavailableMetrics`. Show
"Not configured" or hide these cards. Do not coerce them to zero. Their legacy
names previously exposed unrelated risk/acceptance counts; those values are not
copied into the new response. Defining these metrics remains a business follow-up.

Lifecycle aggregation recognizes only explicit Open / In Progress / Closed
(case/spacing normalized). Approval statuses such as APPROVED are not treated as
Closed. Other statuses are counted as Unknown, while the register retains the raw
risk status. Any broader lifecycle mapping requires an agreed rule.

## Selection and counting rules

1. Retain nondeleted risks created before the exclusive period end, including
   risks identified in previous years. This represents current surviving baseline
   data, not a complete historical audit of deleted/edited risk records.
2. Select one latest nondeleted assessment per risk as of period end, ordered by
   evaluation date (creation date fallback), creation time, then ID.
3. Select one latest review per risk within the reporting window. Completed REVIEW
   period evaluations participate; drafts do not. At equal evaluation dates a
   completed period snapshot takes precedence over the legacy review row.
4. Use residual rating when available, otherwise the inherent rating. Unknown
   ratings remain Unknown; fixed bands still return zero counts. The heatmap may
   use its existing geometric fallback colour when a stored rating is unavailable.
5. Category/source summaries count unique risks. Priority, treatment and assessment
   type use the selected baseline assessment, never every assessment row.
6. Appetite statuses are normalized into Within, Appetite Breached but within
   Tolerance, Tolerance Breached, or Unknown. The middle band is not a tolerance breach.
7. Financial exposure uses the selected quantitative review; without a review,
   a quantitative baseline is returned with `basis=INHERENT`. A missing latest
   residual ALE is not replaced with an older baseline amount. Annual values are
   snapshots, not the sum of four quarters.
8. Maturity uses in-period maturity entries and existing score/level rules. Scope
   applies through departments/branches. Company-wide entries cannot satisfy a
   specific branch/function filter and are excluded in that case.

Keys are normalized uppercase with underscores, including source and treatment
keys. Always use `displayLabel` for text. Empty sections are arrays; numeric metrics
are numbers. `notes` explains selection/availability rules in the response.

## Implementation and performance

The new overview uses the existing ChartScope resolver, chart performance service,
heatmap builder, rating mapper and maturity rules. A scoped scalar SQL batch ranks
assessments/reviews before aggregation; there are no per-risk database lookups.
Maturity uses a second batch and deduplicates score IDs across department joins.
Performance remains paginated. Heatmap drill-down IDs and summary processing scale
with the number of scoped risks; large organizations should be load-tested before
choosing caching or response-size limits. No performance benchmark is implied.

Two fixes in the reused performance query also apply to the existing chart endpoint:
organization-wide requests now include completed history for the authorized source
IDs when companyId is omitted; KPI branch filtering recognizes the existing legacy
profile organization link when the owner's direct organization is null. Explicit
conflicting organization assignments are not accepted by that fallback.

Swagger: `/erm/v3/api-docs/period-workflow` includes `/dashboard/overview` and its
typed response. The API gateway prefix is `/erm`; direct service tests use
`/dashboard/overview` after the gateway's prefix stripping.

Verification covers real local HTTP, OpenAPI, isolated MySQL selection/scoping,
null/zero behavior, period windows, mixed series and regression tests. These checks
do not constitute verification against a newly deployed production instance.
