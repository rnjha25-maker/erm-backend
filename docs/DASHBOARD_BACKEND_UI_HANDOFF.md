# ERM dashboard backend handoff for UI

This document describes the dashboard response currently implemented in the backend for the UI integration. It covers the combined overview endpoint and the response fields added for the dashboard panels. Existing dashboard endpoints remain available; no frontend files were changed.

## Endpoint

`GET /dashboard/overview`

Example:

```http
GET /erm/dashboard/overview?year=2026&yearType=CALENDAR&periodType=YEAR&indicator=KRI&frequency=MONTHLY&page=0&size=20&performancePage=0&performanceSize=20
```

The endpoint returns the usual `GeneralResponse` envelope. Dashboard data is under `data`.

### Query parameters

| Parameter | Required | Default | Notes |
|---|---:|---|---|
| `year` | Yes | — | Reporting year. |
| `yearType` | No | `CALENDAR` | `CALENDAR` or `FINANCIAL`; financial year starts in April. |
| `periodType` | No | `YEAR` | `YEAR`, `H1`, `H2`, or `Q1`–`Q4`. |
| `companyId` | No | — | Optional narrowing filter. |
| `branchId` | No | — | Optional narrowing filter. |
| `functionId` | No | — | Optional narrowing filter. |
| `indicator` | No | `KRI` | `KRI` or `KPI`; used for performance series. |
| `frequency` | No | `MONTHLY` | `MONTHLY` or `QUARTERLY`. |
| `page`, `size` | No | `0`, `20` | Pagination for the risk register. |
| `performancePage`, `performanceSize` | No | `0`, `20` | Pagination for performance series. |
| `performanceRecordId` | No | — | Selects one series from the requested performance page for the legacy `performance` array. |

Omit optional scope IDs to request all records permitted by the caller's existing backend role scope. Do not send `0`. Filters can narrow that scope; they cannot widen it. UI role checks can still control panel visibility, but must not be treated as data authorization.

## Panel fields

The following fields are available in `data` in addition to the existing live overview fields (risk summary, heatmap, maturity summary, risk register, and performance series):

| Panel / contract area | Response field(s) |
|---|---|
| §17 Rating cuts | `ratingsByGroupCompany`, `ratingsByCategory`, `ratingsByDepartment`; existing location/function/owner cuts remain `riskRatingByLocation`, `functionWiseRiskRating`, `riskRatingByOwner`. Each rating cut has `key`, `displayLabel`, `total`, and `breakdown`. |
| §18 Risk trend | `riskTrend`: `key` (`YYYY-MM`), `displayLabel`, `cumulativeValue`. Counts are cumulative risk creations within the selected period; empty months carry forward the prior total. |
| §19 Risk by status | `riskByStatus`: key/count/displayLabel for `DRAFT`, `SUBMITTED`, `UNDER_REVIEW`, and `APPROVED`. Draft risks are visible only to their creator or an approver assigned to that risk. |
| §20 Department and reporting frequency | `riskByDepartment`, `reportingFrequencyDistribution`: key/count/displayLabel. |
| §21 Top risks | `topRisks`: `riskId`, `code`, `riskTitle`, `owner`, `score`, `band`, `treatmentStatus`; at most 10, sorted by descending available residual/risk score. |
| §22, §30–§31 KRI overview/evaluation | `kriStatusOverview`, `kriEvaluation`, `kriAppetiteEvaluation`: indicator ID, linked risk ID, indicator/source/UOM, threshold, current, status, tolerance fields, and available readings. |
| §23 Treatment and approvals | `treatmentPlanStatus`, `approvalQueue`. Approval counts are read-only and restricted to pending approvals assigned to the current user. |
| §24 Control effectiveness | `controlEffectiveness`: `averageScore`, `assessmentCount`, `byRating`. |
| §26 Overdue items and actions | `overdueItems`, `myActionItems`. Action entries include `id`, `type`, `displayLabel`, `due`, `priority`, `progress`, and `recordId`. My actions are assigned pending approvals. Overdue items require a persisted due date and incomplete status. |
| §32–§33 KRA/KPI evaluation | `kraKpiEvaluation`, `kpiPeriodical`: indicator evaluation fields and Q1–Q4 readings for the selected reporting year. |
| §34 Rating and appetite | `riskRatingAppetite`: risk ID, inherent/residual fields, appetite/tolerance and acceptance status. |
| §36 Function compliance | `functionWiseCompliance`: function key/label, total, timely, pending, and late counts. |
| §37 Dynamic categories | `dynamicCategoryRecords`: category ID and display label. `count` is currently `null`; see limitations below. |
| §39 Quick stats | `quickStats`: key, displayLabel, value. |

Fixed severity breakdowns use `CRITICAL`, `HIGH`, `MEDIUM`, `LOW`, and `VERY_LOW`; absent levels are returned with count zero. Other categories/statuses may include `UNKNOWN` where source data cannot be normalized.

## UI integration notes and current limits

- The UI should render each field as returned and should not recalculate risk/KRI/KPI status or tolerance. KRI status/tolerance values come from persisted backend data; the backend does not assume every higher value is worse.
- KRI threshold properties are currently populated from stored tolerance range values. KPI thresholds likewise use stored performance tolerance values. Missing source values remain `null`.
- `dynamicCategoryRecords` currently exposes category definitions only. Record counts are withheld until counts can be computed with the applicable table-level role scope and field permissions. Check `unavailableMetrics["dynamicCategoryRecords.count"]` for the reason.
- Overdue KRI/KPI/treatment entries require a source due date. The backend does not invent due dates. A missing due date means the item cannot be classified as overdue by this response.
- `overdueEvaluations`, `upcomingReview`, `totalKris`, and `highRiskKris` remain `null`/unavailable. Their metric definitions or thresholds have not been agreed; use the corresponding entries in `unavailableMetrics`.
- `performance` is a compatibility field. With one series on the first page it is populated automatically; with multiple series, use `performanceSeries` and pass a selected `performanceRecordId` to retrieve the corresponding `performance` points.
- `riskRegister` pagination (`page`, `size`) and performance pagination are independent. Pagination does not change chart totals.
- `approvalQueue` is a summary and deep-link source only. It does not perform approve/reject actions.
- The response includes `notes` and `unavailableMetrics`; use these to present known limitations rather than treating missing values as zero.

## Smoke check

The backend overview service was smoke-checked against the configured QA database with Hibernate DDL disabled and service discovery disabled for the test run. The user scope resolved successfully and the new query paths executed. That account had no in-scope risk, KRI, or approval rows at the time, so aggregation behavior for populated datasets still needs UI/QA validation with representative records.

