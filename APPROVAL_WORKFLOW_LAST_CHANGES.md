# Approval Workflow - Last Changes

## Summary

This update extends the approval workflow with approver dashboard APIs, upcoming/overdue/history views, reminder tracking, automatic reminder scheduling, stronger approval creation validation, and source record validation for workflow-linked ERM records.

## Backend API Changes

New approval dashboard endpoints were added under `ApprovalController`:

| Method | Endpoint | Purpose |
| --- | --- | --- |
| `GET` | `/approvals/dashboard` | Returns pending, upcoming due, overdue, and approval history in one response. |
| `GET` | `/approvals/dashboard/upcoming-due` | Returns pending approvals due within the next 7 days. |
| `GET` | `/approvals/dashboard/overdue` | Returns pending approvals with a due date before the current time. |
| `GET` | `/approvals/dashboard/history` | Returns non-pending approval decisions for the current approver. |

## DTO Changes

Added `ApprovalDashboardResponse` with:

- `pending`
- `upcomingDue`
- `overdue`
- `history`

Updated `ApprovalResponse` to expose:

- `reminderNotifiedAt`

## Approval Entity and Repository Changes

Updated `Approval` with:

- `reminderNotifiedAt`, mapped to `approval.reminder_notified_at`

Added repository queries for:

- Approver-specific upcoming due approvals
- Approver-specific overdue approvals
- Approver-specific approval history
- Pending approvals due for reminders
- Pending approvals due within a date range

## Approval Service Changes

Approval creation now validates:

- Request body is present
- Approver ID is present
- Submitter ID is present
- Approver and submitter are different users
- Current user is either the submitter or an admin
- `sourceModule` and `sourceRecordId` point to an existing active source record

Approval decisions now require:

- Final status must be `APPROVED` or `REJECTED`
- Reject decisions must include a non-blank comment

Reminder handling now:

- Sends a reminder notification
- Stores `reminderNotifiedAt`
- Saves the approval after reminder notification

Additional service methods were added for:

- `getMyDashboard`
- `getMyUpcomingDueApprovals`
- `getMyOverdueApprovals`
- `getMyApprovalHistory`

## Source Record Validation

Added `ApprovalSourceRecordValidator` to verify approval source records before creating approvals.

Supported normalized source modules include:

- `risk`
- `subrisk`
- `riskassessment`
- `riskcontrol`
- `risktreatment`
- `riskresponsetreatment`
- `riskreview`
- `krikpireview`
- `kpakpireview`
- `ermmaturity`

Validation checks:

- `sourceModule` and `sourceRecordId` are required
- `sourceRecordId` must be numeric
- Source record must exist
- Source record must not be marked deleted
- Source record must belong to the current organization when organization context is available

Added `RiskTreatmentRepository` to support validation for risk treatment approvals.

## Scheduler Changes

`ApprovalEscalationScheduler` now processes both reminders and escalations.

Reminder behavior:

- Controlled by `approval.reminder.interval-ms`
- Finds pending approvals that have not been reminded recently
- Skips manual approvals that have not been triggered
- Sends reminder notifications
- Updates `reminderNotifiedAt`

Escalation behavior:

- Controlled by `approval.escalation.interval-ms`
- Prevents repeated escalation within the configured interval
- Keeps automatic overdue approval escalation behavior

Default scheduler values:

- `approval.escalation.enabled=true` in scheduler property fallback
- `approval.reminder.interval-ms=86400000`
- `approval.escalation.interval-ms=86400000`

## Notification Changes

Notification creation now continues even when email sending is disabled.

Behavior changes:

- Notification rows are still created as `PENDING` when `approval.notifications.enabled=false`
- Email sending only runs when notifications are enabled
- Assignment, reminder, and escalation notifications target the primary recipient
- Approval/rejection decision notifications include configured recipient users
- Failed email attempts are captured as `FAILED` with a failure reason

## Database Script

Added manual schema script:

`ERM_backend/scripts/approval_workflow_dashboard_and_reminders.sql`

The script adds:

- `approval.reminder_notified_at`
- Index on approver/status/due/deleted
- Index on status/reminder/deleted
- Index on source module/source record

Use this script in environments where Hibernate DDL auto-update is disabled.

## Configuration Changes

Added default approval feature flags in:

- `ERM_backend/src/main/resources/application.yaml`

Defaults:

```yaml
approval:
  escalation:
    enabled: false
  notifications:
    enabled: false
```

Environment datasource configuration was also updated in staged files for:

- `ERM_backend`
- `command/erm-command-organization`
- `erm-api-gateway`

Security note: datasource credentials should be supplied through environment variables or a secret manager. Do not commit plaintext production credentials.

## Files Added

- `APPROVAL_WORKFLOW_LAST_CHANGES.md`
- `ERM_backend/scripts/approval_workflow_dashboard_and_reminders.sql`
- `ERM_backend/src/main/java/ermorg/erm/dto/response/ApprovalDashboardResponse.java`
- `ERM_backend/src/main/java/ermorg/erm/repository/RiskTreatmentRepository.java`
- `ERM_backend/src/main/java/ermorg/erm/serviceimpl/ApprovalSourceRecordValidator.java`

## Files Updated

- `ERM_backend/src/main/java/ermorg/erm/controller/ApprovalController.java`
- `ERM_backend/src/main/java/ermorg/erm/dto/response/ApprovalResponse.java`
- `ERM_backend/src/main/java/ermorg/erm/model/Approval.java`
- `ERM_backend/src/main/java/ermorg/erm/repository/ApprovalRepository.java`
- `ERM_backend/src/main/java/ermorg/erm/service/IApprovalService.java`
- `ERM_backend/src/main/java/ermorg/erm/serviceimpl/ApprovalEscalationScheduler.java`
- `ERM_backend/src/main/java/ermorg/erm/serviceimpl/ApprovalService.java`
- `ERM_backend/src/main/java/ermorg/erm/serviceimpl/NotificationService.java`
- `ERM_backend/src/main/resources/application-dev.yaml`
- `ERM_backend/src/main/resources/application-qa.yaml`
- `ERM_backend/src/main/resources/application.yaml`
- `command/erm-command-organization/src/main/resources/application-dev.yaml`
- `command/erm-command-organization/src/main/resources/application-qa.yaml`
- `erm-api-gateway/src/main/resources/application-dev.yaml`
- `erm-api-gateway/src/main/resources/application-qa.yaml`

## Deployment Notes

1. Apply `approval_workflow_dashboard_and_reminders.sql` where Hibernate does not manage schema updates.
2. Configure datasource credentials using environment variables or secret management.
3. Confirm `approval.escalation.enabled` and `approval.notifications.enabled` are intentionally set per environment.
4. Verify dashboard APIs with an authenticated user context.
5. Verify reminder behavior with pending approvals and due dates.
