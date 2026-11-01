# ERM dashboard backend handoff for UI

## Current behavior

- The React dashboard has a live mode that calls one combined endpoint: `GET /erm/dashboard/overview`.
- That endpoint returns one response containing the supported risk summaries, heat map, breakdowns, maturity data, risk register page, and KRI/KPI performance series.
- Live mode renders `DashboardOverview` and does not render the seed dashboard panels. The static mode renders the existing local data dashboard instead.
- The React API master switch is `VITE_USE_API=true`; with the default `false`, the dashboard remains seed-backed and makes no dashboard overview request.
- Legacy endpoints remain available: `/erm/dashboard/erm-summary`, `/erm/dashboard/erm-summary-v2`, `/erm/dashboard/risk-register`, CSV export, and `/erm/basic/{period}`, `/erm/advanced/{period}`, `/erm/admin/{period}`.

## Coverage against the 48-panel inventory

The combined overview payload has these relevant fields today:

| UI panel group | Overview field(s) | Current state |
| --- | --- | --- |
| Risk level summary | `riskLevelSummary` | API-backed |
| Priority / source / category / impact / treatment / assessment | `riskSummaryPriorityBased`, `riskBySource`, `riskByCategory`, `riskByImpact`, `treatmentStrategy`, `assessmentType` | API-backed |
| Rating by status | `ratingWiseStatus` | API-backed |
| Risk heat map | `riskHeatmap` | API-backed |
| Location / category / function / owner breakdowns | `riskRatingByLocation`, `riskRatingByCategory`, `functionWiseRiskRating`, `riskRatingByOwner` | API-backed; not all contract panels are represented separately |
| Appetite and tolerance counts | `withinRiskAppetite`, `riskToleranceBreached` | API-backed |
| Financial risk exposure | `financialExposureByRisk` | API-backed |
| ERM maturity | `ermMaturityFunctionWise`, `ermMaturityCompanyWise` | API-backed |
| Risk register | `riskRegister`, `riskRegisterPage` | API-backed, paginated |
| KRI/KPI performance | `performance`, `performanceSeries`, `performancePage` | API-backed, independently paginated |
| Overdue evaluations, upcoming review, KRI totals/high-risk KRI totals | matching nullable fields | Not configured; backend supplies a reason in `unavailableMetrics` |

The combined overview does not currently expose every item in the 48-panel inventory as an independent dataset. Examples include quick stats; group-company / organization rating variants; risk-by-department and risk-by-status charts; top 10 risks; KRI status overview; treatment plan status; approval queue; control effectiveness; overdue/action items; escalation matrix and velocity heat map; reporting-frequency distribution; and the nine all-fields dynamic-record tables. In the existing React dashboard, these panels are computed from local seed/dynamic-category state and are not calls to dashboard backend endpoints.

Use the attached UI contract and panel inventory to identify the exact panel-to-field mappings still required. For each missing panel, please confirm the dataset, filters, role visibility, and empty-state behavior with the product owner before wiring it. The existing backend response includes `unavailableMetrics` for metrics whose business definitions have not been agreed; the UI should retain that explicit unavailable state instead of showing a misleading zero.

## Role and scope behavior

Dashboard API data scope uses the existing role names and precedence: `ORG_ADMIN` > `COMPANY_ADMIN` > `ADVANCED_USER` > `BASIC_USER`. The resulting data scopes are organization-wide, assigned-company, assigned-company plus assigned branch/department, and creator-only, respectively. The server applies the scope even when a UI sends filter IDs; filters narrow the authorized scope and cannot widen it. The overview endpoint rejects branch/department filters for creator-only users because those filters could otherwise return records they did not create.

The legacy tiered routes are also role-checked server-side. `/admin` is restricted to organization-wide scope; `/advanced` applies company scope and narrows risk/branch/department aggregates to an Advanced User's assigned branch and department; `/basic` returns creator-only records for Basic Users and the role-authorized company or organization population for higher roles. The combined overview and legacy summary/register endpoints apply the shared branch/department scope rules. Existing role precedence is unchanged.

## Suggested UI integration checks

1. Keep the existing seed/static dashboard usable while the live API switch is off.
2. When live mode is enabled, bind only panels with defined fields and preserve the server-provided unavailable reasons for undefined metrics.
3. Validate the combined endpoint with an organization admin, company admin, advanced user with branch and department assignments, advanced user with only one assignment, and basic user.
4. For roles whose panels differ, hide/show panels in UI using the existing role rules; data returned by the backend remains bounded by the caller's data scope.
5. For the remaining static-only panels, share the requested field definitions and filters so backend endpoints can be designed without changing the existing role model.

## Local verification

Backend source changes were compiled with `mvn -q -DskipTests compile`. This confirms compilation only; authenticated integration behavior still needs verification against a configured database and representative users.
