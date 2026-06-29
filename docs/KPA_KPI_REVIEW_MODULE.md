# Module Overview

The KPA & KPI Review & Evaluation module captures performance area, KPI, target, actuals, review cadence, owner, evaluator, status, and annual loss expectancy data for a company within an organization.

Records are company-scoped, organization-scoped, audited, and soft deleted with `is_deleted`.

# What Changed from KRI Module

- Risk terminology was replaced with performance terminology.
- Risk and sub-risk entity references were removed from the DTO contract.
- The API uses flat request and response DTOs.
- Organization and company are derived from server-side request context.
- Annual Loss Expectancy is calculated by the backend.
- List API is paginated and supports optional `status` and `search` filters.
- Approval task APIs were added so a KPA/KPI review page can be routed to an assigned approver.
- Approval decisions now notify the submitter, configured recipient users, and tagged members.

# Branch Commit Summary

Commit `24a0d53` on branch `kri-risk-improvment` documents and supports the KPA/KPI review module, plus the shared approval and notification flow used by this module.

Implemented backend changes in this branch:

- New KPA/KPI review API at `/kpa-kpi-review`.
- New approval API at `/approvals`.
- New approval statuses: `PENDING`, `APPROVED`, `REJECTED`.
- New notification statuses: `PENDING`, `SENT`, `FAILED`.
- New notification channels: `EMAIL`, `IN_APP`.
- Approval records store approver, submitter, review page link, record name, tagged members, recipient user IDs, status, comment, and notification timestamp.
- Notification records store recipient, approval reference, channel, sent timestamp, and delivery status.
- Approval decision saves the approver's decision/comment, sets `notifiedAt`, creates notification rows, and attempts email delivery.

# New Fields Added

- `kpa`
- `keyPerformanceParameters`
- `keyPerformanceIndicator`
- `typesOfKpi`
- `performanceToleranceMinValue`
- `performanceToleranceMaxValue`
- `performanceAppetite`
- `kpiType`
- `kraRating`
- `kpiEvaluationBy`
- `kpiEvaluationFrequency`
- `potentialLossPercentage`
- `yearlyFrequency`
- `annualLossExpectancy`

# API Endpoints

| Method | Endpoint | Description |
| --- | --- | --- |
| `POST` | `/kpa-kpi-review` | Create or update a review. Use `kpaKpiReviewId = 0` for create. |
| `GET` | `/kpa-kpi-review/{id}` | Get one active review scoped to current organization and company. |
| `GET` | `/kpa-kpi-review?page=0&size=20&sort=createdAt,desc&status=ACTIVE&search=revenue` | Get paginated active reviews. |
| `DELETE` | `/kpa-kpi-review/{id}` | Soft delete one active review. |

# Approval API Endpoints

| Method | Endpoint | Description |
| --- | --- | --- |
| `POST` | `/approvals` | Create an approval task for an approver. The task starts with `PENDING` status. |
| `PUT` | `/approvals/{id}/decision` | Save the approver decision. Only `APPROVED` or `REJECTED` is accepted. |
| `GET` | `/approvals/pending` | Get pending approval tasks assigned to the logged-in user. |
| `GET` | `/approvals/login-target` | Get login redirect target based on pending approvals. |

# Request/Response Example

Request:

```json
{
  "kpaKpiReviewId": 0,
  "kpa": "Revenue Growth",
  "businessObjectives": "Improve recurring revenue",
  "businessFunction": "Sales",
  "ownerId": 101,
  "target": "Increase ARR by 15%",
  "keyPerformanceParameters": "ARR, churn, expansion revenue",
  "keyPerformanceIndicator": "Monthly recurring revenue",
  "typesOfKpi": "LEADING",
  "performanceToleranceMinValue": 90.0000,
  "performanceToleranceMaxValue": 110.0000,
  "performanceAppetite": "Within +/- 10% of target",
  "reportingFrequency": "MONTHLY",
  "currency": "USD",
  "targetValue": 1000000.0000,
  "actualValue": 950000.0000,
  "kpiType": "QUANTITATIVE",
  "kraRating": "GOOD",
  "kpiEvaluationBy": 102,
  "kpiEvaluationFrequency": "QUARTERLY",
  "potentialLossPercentage": 5.0000,
  "yearlyFrequency": 4,
  "dueDate": "2026-07-31",
  "status": "ACTIVE"
}
```

Response:

```json
{
  "status": "SUCCESS",
  "message": "Saved.",
  "data": {
    "kpaKpiReviewId": 501,
    "kpa": "Revenue Growth",
    "keyPerformanceIndicator": "Monthly recurring revenue",
    "performanceToleranceMinValue": 90.0000,
    "performanceToleranceMaxValue": 110.0000,
    "potentialLossPercentage": 5.0000,
    "yearlyFrequency": 4,
    "annualLossExpectancy": 20.0000,
    "status": "ACTIVE"
  }
}
```

# Approval Request/Response Examples

Create approval task:

```json
{
  "approverId": 102,
  "submitterId": 101,
  "pageLink": "/kpa-kpi-review/501",
  "recordName": "Revenue Growth - Monthly recurring revenue",
  "taggedMembers": "@Jane Manager @audit@example.com",
  "recipientUserIds": [101, 205]
}
```

Create approval response:

```json
{
  "status": "SUCCESS",
  "message": "Approval task created.",
  "data": {
    "id": 9001,
    "approverId": 102,
    "submitterId": 101,
    "pageLink": "/kpa-kpi-review/501",
    "status": "PENDING",
    "comment": null,
    "notifiedAt": null,
    "recordName": "Revenue Growth - Monthly recurring revenue",
    "taggedMembers": "@Jane Manager @audit@example.com",
    "recipientUserIds": "101,205"
  }
}
```

Decision request:

```json
{
  "status": "APPROVED",
  "comment": "Approved for quarterly review cycle."
}
```

Decision response:

```json
{
  "status": "SUCCESS",
  "message": "Approval decision saved.",
  "data": {
    "id": 9001,
    "status": "APPROVED",
    "comment": "Approved for quarterly review cycle.",
    "notifiedAt": "2026-06-26T10:15:00.000+00:00"
  }
}
```

# Business Logic (Calculation)

`annualLossExpectancy = potentialLossPercentage * yearlyFrequency`

Rules:

- If either input is missing, `annualLossExpectancy` is `null`.
- Result is stored at scale `4`.
- `potentialLossPercentage` must be between `0` and `100`.
- `yearlyFrequency` must be `0` or greater.

# Approval and Notification Logic

Approval creation:

- Approval creation is a separate API call from KPA/KPI review save.
- The UI should create or update the KPA/KPI review first, then create an approval task with `pageLink` pointing to that review page.
- `approverId` and `submitterId` must reference active, non-deleted users.
- New approval tasks are saved with `PENDING` status.
- `recipientUserIds` is accepted as a JSON array in the request and returned as a comma-separated string in the response.

Decision flow:

- Only the assigned approver can decide the approval when a logged-in user context is present.
- Decision status must be `APPROVED` or `REJECTED`; `PENDING` is not allowed as a decision.
- Decision saves the comment and sets `notifiedAt`.
- After a decision is saved, the backend resolves notification recipients and creates one notification row per recipient.

Notification recipients:

- The submitter is always included when available.
- Any active users listed in `recipientUserIds` are included.
- Tagged members are resolved from `taggedMembers` by matching `@mention` text against active users in the current organization.
- Mention matching supports display name, email, or phone after normalization.
- Duplicate recipients are de-duplicated before notifications are created.

Notification delivery:

- Current implementation creates `EMAIL` notification records.
- If a mail sender is configured and the recipient has an email address, the backend sends a simple email with approver, decision, record name, comment, and page link.
- A notification is saved as `SENT` when email delivery completes.
- A notification is saved as `FAILED` if email delivery throws an exception.
- If no mail sender is configured or the recipient has no email address, the send method returns without error and the notification is currently marked `SENT`.
- `IN_APP` exists as a channel enum but is not yet used by the service.

# Login Redirect Behavior

`GET /approvals/login-target` returns:

- `redirectUrl = null` when the user has no pending approvals.
- `redirectUrl = <approval.pageLink>` when the user has exactly one pending approval.
- `redirectUrl = "/approvals/pending"` when the user has more than one pending approval.
- `pendingApprovals` always contains the pending approval list for the logged-in user.

# UI Changes Required

- Replace remaining risk labels with performance labels.
- Send numeric fields as numbers, not strings.
- Use server-calculated `annualLossExpectancy`; do not allow manual editing.
- Use dropdowns for `reportingFrequency`, `kpiEvaluationFrequency`, and `status`.
- Update list screens to consume Spring `Page` response fields: `content`, `totalElements`, `totalPages`, `number`, and `size`.
- Add search box wired to `search`, matching KPA and KPI text.
- Add optional status filter wired to `status`.
- After saving a KPA/KPI review that requires approval, call `POST /approvals` with the review page link and selected approver.
- Show pending approval tasks from `GET /approvals/pending` to approvers.
- On login, call `GET /approvals/login-target` and redirect users with pending approvals according to `redirectUrl`.
- Decision UI must send only `APPROVED` or `REJECTED` to `PUT /approvals/{id}/decision`.
- Allow approvers to enter an optional decision comment.
- Allow submitter to add explicit notification recipients through `recipientUserIds` and tagged members through `taggedMembers`.

# Validation Rules

- `kpa` is required, max `255`.
- `keyPerformanceIndicator` is required, max `1000`.
- `ownerId` and `kpiEvaluationBy` must be positive IDs.
- `reportingFrequency` is required and must be one of `DAILY`, `WEEKLY`, `MONTHLY`, `QUARTERLY`, `HALF_YEARLY`, `ANNUALLY`, `AD_HOC`.
- `kpiEvaluationFrequency` uses the same allowed values when provided.
- `status` is required and must be one of `DRAFT`, `ACTIVE`, `IN_REVIEW`, `COMPLETED`, `OVERDUE`, `INACTIVE`.
- `currency` must be a 3-letter uppercase ISO code when provided.
- Numeric values must be non-negative.
- `actualDate` and `lastKpiEvaluationDate` cannot be future dates.
- `dueDate` and `nextEvaluationDate` cannot be past dates.
- Approval `approverId` and `submitterId` must be active, non-deleted users.
- Approval decision `status` must be `APPROVED` or `REJECTED`.
- Decision by a logged-in user other than the assigned approver is rejected.
- Approval `pageLink` should point to the KPA/KPI review page so login redirect and notification email can route users correctly.

# QA Test Scenarios

- Create a review with all mandatory fields.
- Update an existing review using `kpaKpiReviewId`.
- Verify `annualLossExpectancy` is calculated and rounded to 4 decimals.
- Verify missing ALE inputs return `annualLossExpectancy = null`.
- Verify validation fails for invalid status or frequency.
- Verify validation fails for negative numeric values.
- Verify `GET /kpa-kpi-review` returns paginated data only for the current company.
- Verify `status` filter returns only matching rows.
- Verify `search` matches both `kpa` and `keyPerformanceIndicator`.
- Verify `DELETE` sets soft delete and deleted rows are not returned.
- Verify users from deleted accounts cannot be selected as owner or evaluator.
- Create an approval task for a saved KPA/KPI review and verify status is `PENDING`.
- Verify `GET /approvals/pending` returns only approvals assigned to the logged-in approver.
- Verify `GET /approvals/login-target` returns `null`, the review `pageLink`, or `/approvals/pending` for zero, one, or many pending tasks.
- Approve an approval and verify status, comment, and `notifiedAt` are updated.
- Reject an approval and verify status, comment, and `notifiedAt` are updated.
- Verify a decision using `PENDING` fails validation.
- Verify a non-assigned logged-in user cannot decide another user's approval.
- Verify notification rows are created for submitter, explicit recipient users, and tagged members.
- Verify duplicate recipients do not create duplicate notification rows.
- Verify email delivery success marks notification status `SENT`.
- Verify email delivery failure marks notification status `FAILED`.

# Edge Cases

- `yearlyFrequency = 0` should produce `annualLossExpectancy = 0.0000`.
- Missing organization or company context should fail instead of saving orphan records.
- Deleted records must not be readable, listed, updated, or deleted again.
- Very long text values should be rejected before persistence.
- Deprecated risk-named aliases may be present during UI transition but should not be used for new screens.
- Approval notification still saves the decision even if email sending fails for one or more recipients.
- Tagged members that cannot be matched to an active user are ignored.
- Invalid IDs in stored recipient strings are ignored during notification resolution.

# Known Limitations (if any)

- Frequency, status, KPI type, and rating are still strings in Java. They should become enums in a follow-up migration.
- The current API uses `POST` for both create and update. A future REST cleanup should split create and update into `POST` and `PUT/PATCH`.
- Database check constraints require MySQL 8.0.16+ to enforce them.
- Approval tasks are not automatically created by `POST /kpa-kpi-review`; the UI or caller must create approval tasks through `/approvals`.
- `IN_APP` is defined as a notification channel but no in-app notification delivery path is implemented yet.
- There is no public notification listing endpoint in this branch.
- Notification email has no template system; it uses a plain text body.
