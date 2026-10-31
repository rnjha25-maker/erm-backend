# Dashboard cleanup compatibility

This refactor changes Java organization and shared transport, not evaluation rules.

- `DashboardChartService` and `PeriodEvaluationService` now live in `serviceimpl`.
- `DashboardChartRepository` now lives in `repository`.
- Period start/update/complete requests have explicit DTO names in `dto.request`.
- Legacy treatment evidence and period evidence use the same `StorageDocumentClient`
  for HTTP transport, URL normalization and response-envelope extraction.
- Existing filename policies, error messages, validation, timeouts, evidence associations,
  transaction boundaries and business calculations remain at their existing call sites.

Endpoint URLs, JSON names, Swagger schema names and database tables are unchanged.
Period JSON retains the previously documented snake_case values and snapshot fields;
renaming these to camelCase would require a separately coordinated API migration.
Frontend developers can continue using `FRONTEND_PERIOD_WORKFLOW_IMPLEMENTATION.md`.

The existing save methods are intentionally not invoked by period writes: they update
the baseline category records. Chart SQL remains batched and does not call per-record
category services. No blanket interface was added solely for naming symmetry.

This cleanup does not expand period completion validation, recalculate submitted values,
or add correction/cancellation. Those are business changes requiring separate acceptance
criteria; the findings in `PERIOD_WORKFLOW_ARCHITECTURE_REVIEW.md` remain applicable.

Deployment setup and manual branch selection are documented in
`FEATURE_BRANCH_DEPLOYMENT.md`.

Verification on 10 October 2026: clean backend regression run completed with 103 tests,
102 passed, 1 optional persistence test skipped, no failures/errors. Disposable local
MySQL integration tests were enabled. Swagger generation, legacy evidence round-trip,
period history and branch deployment guard tests passed. The default QA-connected
application-context test was excluded. No GitHub workflow or EC2 deployment was triggered.
