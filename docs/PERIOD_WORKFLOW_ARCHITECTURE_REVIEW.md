# Period workflow review — 8 October 2026

## Assessment

The separate history store is a sound compatibility boundary: category records and
their existing IDs remain unchanged; period values are separate and completed versions
are immutable. Tenant predicates, source-row locks, revision checks, a duplicate-period
constraint and scoped evidence reads provide useful safeguards. Database tests cover
source/link preservation and dashboard integration.

This is suitable for further staging validation, **not an unconditional production
sign-off**. The following remaining conditions matter even when functional tests pass.

## Remaining findings

1. **P1 — Existing gateway authentication boundary is unsafe.** `SecurityConfig` includes
   `/erm/**` among public paths, and the backend resolves user/organization/company from
   headers. Role and tenant checks are only as trustworthy as those headers. Restrict
   backend reachability and enforce authenticated, server-derived identity at the gateway
   before exposing the new workflow to untrusted callers. This inherited issue was not
   globally changed during this compatibility-focused review.
2. **P1 — Operational recovery is not yet supported.** A wrong-period draft cannot be
   cancelled; completed values cannot be reopened/corrected, including after rejection.
   There is no immutable correction-revision path. An approved operating procedure or
   a versioned correction/cancellation feature is needed before broad adoption. The UI
   must not promise those actions. Do not silently overwrite completed history to recover.
3. **P2 — Storage and DB are not one transaction.** A storage upload can succeed before
   a DB rollback. Timeouts now bound the call, but orphan cleanup and request-idempotent
   upload recovery remain necessary for production resilience. Physical deletion after
   a remote timeout also has an ambiguous outcome; retain documents conservatively.
4. **P2 — Business validation is minimal.** The new workflow validates basic types,
   ranges and required completion outputs, not all configured mandatory fields or all
   domain calculation relationships. It accepts evaluated scores/ALE rather than
   universally recomputing them. Define/reuse authoritative validation rules before
   treating completion as a fully validated financial-control event.
5. **P2 — Audit actor detail is limited.** Creation actor and timestamps/revisions are
   retained, but there is no per-edit audit event stream or separate completion actor.
   Add those if required by the audit/approval operating model; do not infer who completed
   the evaluation from its original creator.
6. **P2 — Performance is functionally checked, not load-certified.** Source/draft row locks
   serialize writes; storage uploads hold a draft lock during a bounded network call.
   History reads now filter permissions in SQL before pagination. Chart history reads
   are batched, but JSON snapshots can be large. Measure query plans, lock waits, payload
   sizes and latency with representative tenants before rollout. Snapshot projections
   and dedicated numeric period columns may be warranted at scale.

## Issues corrected during this review

- **History authorization/pagination:** moved accessible-row filtering into SQL before
  LIMIT/OFFSET. Previously one inaccessible newer row could make a page fail even when
  an older row was assigned to the caller, with repeated approval-count queries.
- **Source reassignment integrity:** starting another period now rejects a legacy source
  moved to a different risk, preventing one history chain from mixing two risk identities.
- **Evidence retention race:** the legacy deletion path and first snapshot use the same
  treatment-row lock. Deletion checks tenant/company and document association before
  checking retained references. DB work rolls back on checked failures.
- **Unbounded storage waiting:** added a dedicated period storage client with configurable
  5-second connect and 15-second read timeouts. The legacy storage client is unchanged.
- **Swagger discoverability:** added operation descriptions/examples, a `period-workflow`
  OpenAPI group, and an ERM entry in the gateway Swagger menu. A real HTTP test generates
  the spec and checks all new operations, request schemas and multipart upload.

## Compatibility and rollout

### Verification

The local regression run completed with **101 tests: 100 passed, 1 skipped, 0 failures
or errors**. The default application-context test was excluded because its default
configuration targets QA. The skipped test is a separate opt-in persistence test.
The disposable MySQL period/dashboard integration tests were enabled and passed.
The HTTP Swagger test verified all 11 operations across 10 paths, including multipart
upload and request schemas. Its generated spec is available locally at
`ERM_backend/target/period-workflow-openapi.json`; live deployment was not verified.

### Deployment boundary

Existing category save/edit routes are preserved. Evidence deletion is intentionally
stricter where a file is retained by history or does not belong to the supplied treatment.
No frontend application code, deployed database or live infrastructure was changed.

Apply both additive migration scripts through the normal migration process, deploy
backend and gateway documentation, then enable the new UI only in staging first. Verify
legacy create/edit/delete, evidence, approval and dashboard paths with realistic data.
Unit/integration tests cannot establish that an unknown production dataset or deployment
configuration will have no regressions.

Frontend handoff: `FRONTEND_PERIOD_WORKFLOW_IMPLEMENTATION.md`.
Backend contract: `PERIOD_EVALUATION_API.md` and `DASHBOARD_CHART_API.md`.
