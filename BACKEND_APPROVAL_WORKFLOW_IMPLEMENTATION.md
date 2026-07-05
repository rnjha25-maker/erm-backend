# Backend Approval, Escalation & Notification Workflow Implementation

## Executive Summary

This document provides a complete backend implementation for the Approval, Escalation, and Notification (ERAM) workflow in the ERM system. The changes ensure:

- ✅ Automatic notification to approvers upon assignment
- ✅ Proper notification to submitters and tagged users upon decision (approve/reject)
- ✅ Escalation logic that executes automatically based on due dates
- ✅ Strong entity relationships for traceability and data isolation
- ✅ Centralized notification service with event type tracking
- ✅ API backward compatibility with canonical `/escalations` endpoint
- ✅ Clean separation of concerns (Approval, Notification, Escalation services)

---

## Implementation Summary

### 1. Entity Model Changes

#### Approval Entity (`approval` table)

**New Fields:**
- `organization_id` – Link to owning organization (for data isolation)
- `company_id` – Link to owning company
- `source_module` – Source system/module of the approval (e.g., "RISK_CONTROL", "KPA_KPI")
- `source_record_id` – ID of the original record being approved
- `dueAt` – Due date for approval decision
- `escalationLevel` – Current escalation level (starts at 0)
- `escalatedAt` – Timestamp of last escalation
- `closedAt` – Timestamp when approval was finalized
- `assignedNotifiedAt` – Timestamp when approver was notified of assignment
- `decisionNotifiedAt` – Timestamp when submitter was notified of decision

**Database Migration:**
```sql
ALTER TABLE approval
ADD COLUMN organization_id BIGINT,
ADD COLUMN company_id BIGINT,
ADD COLUMN source_module VARCHAR(255),
ADD COLUMN source_record_id VARCHAR(255),
ADD COLUMN due_at DATETIME,
ADD COLUMN escalation_level INT DEFAULT 0,
ADD COLUMN escalated_at DATETIME,
ADD COLUMN closed_at DATETIME,
ADD COLUMN assigned_notified_at DATETIME,
ADD COLUMN decision_notified_at DATETIME,
ADD FOREIGN KEY (organization_id) REFERENCES organization(id),
ADD FOREIGN KEY (company_id) REFERENCES company(id);
```

#### Notification Entity (`notification` table)

**New Fields:**
- `event_type` – ENUM(ASSIGNED, APPROVED, REJECTED, ESCALATED, REMINDER_SENT, CLOSED)
- `source_module` – Source system/module
- `source_record_id` – Original record ID
- `subject` – Email subject
- `body` – Email body
- `failure_reason` – Error message if sending failed
- `retry_count` – Number of retry attempts
- `next_retry_at` – Next scheduled retry time

**Database Migration:**
```sql
ALTER TABLE notification
ADD COLUMN event_type VARCHAR(50),
ADD COLUMN source_module VARCHAR(255),
ADD COLUMN source_record_id VARCHAR(255),
ADD COLUMN subject VARCHAR(1000),
ADD COLUMN body TEXT,
ADD COLUMN failure_reason TEXT,
ADD COLUMN retry_count INT DEFAULT 0,
ADD COLUMN next_retry_at DATETIME,
ADD INDEX idx_event_type (event_type),
ADD INDEX idx_source (source_module, source_record_id);
```

#### Escalation Entity (`escalations` table)

**Relationship Changes:**
- Added `primary_responsible_id` (FK to User) – replaces `email_id_primary_responsible` for CRUD operations
- Added `escalation_user_id` (FK to User) – replaces `escalation_email_id` for CRUD operations
- Added `reporting_user_id` (FK to User) – replaces `reporting_level_email_id` for CRUD operations
- **Kept** email string fields for fallback/logging

**Database Migration:**
```sql
ALTER TABLE escalations
ADD COLUMN primary_responsible_id BIGINT,
ADD COLUMN escalation_user_id BIGINT,
ADD COLUMN reporting_user_id BIGINT,
ADD FOREIGN KEY (primary_responsible_id) REFERENCES user(id),
ADD FOREIGN KEY (escalation_user_id) REFERENCES user(id),
ADD FOREIGN KEY (reporting_user_id) REFERENCES user(id);
```

#### New WorkflowEventType Enum

Created `WorkflowEventType.java`:
```java
public enum WorkflowEventType {
    ASSIGNED,
    APPROVED,
    REJECTED,
    ESCALATED,
    REMINDER_SENT,
    CLOSED
}
```

---

### 2. Service Layer Implementation

#### NotificationService (New)

**Purpose:** Centralized notification handling for all workflow events.

**Methods:**
- `sendApprovalAssigned(Approval approval)` – Sends to approver when approval is created
- `sendApprovalDecision(Approval approval)` – Sends to submitter when decision is made
- `sendEscalation(Approval approval)` – Sends to relevant parties when escalated
- `sendReminder(Approval approval)` – Sends reminder to approver for pending approvals

**Features:**
- Resolves recipient list from submitter, tagged members, and explicit recipient IDs
- Normalizes mentions (@name format) to user email matching
- Gracefully handles missing email addresses
- Logs and records failed notification attempts
- Sets appropriate event type and source metadata

**Location:** `serviceimpl/NotificationService.java`

#### ApprovalService (Refactored)

**Changes:**
1. **Removed** inline email sending logic → delegated to NotificationService
2. **Removed** NotificationRepository and JavaMailSender dependencies
3. **Added** NotificationService dependency
4. **Enhanced `createApproval()`:**
   - Set organization and company context
   - Call `notificationService.sendApprovalAssigned()` after save
   - Set `assignedNotifiedAt` timestamp

5. **Enhanced `decide()`:**
   - Validate approver is the current user
   - Validate approval is in PENDING status (prevent duplicate decisions)
   - Set `closedAt` timestamp on decision
   - Call `notificationService.sendApprovalDecision()` after save
   - Set `decisionNotifiedAt` timestamp

**Location:** `serviceimpl/ApprovalService.java`

#### ApprovalEscalationScheduler (New)

**Purpose:** Scheduled job that automatically escalates approvals past their due date.

**Implementation:**
- Runs every 60 seconds (configurable via `approval.escalation.fixed-delay` property)
- Fetches all PENDING approvals where `dueAt` is before NOW
- Increments `escalationLevel` for each approval
- Updates `escalatedAt` timestamp
- Calls `notificationService.sendEscalation()` for each escalation
- Logs escalation events

**Location:** `serviceimpl/ApprovalEscalationScheduler.java`

**Configuration (application.yaml):**
```yaml
approval:
  escalation:
    fixed-delay: 60000  # milliseconds (1 minute)
```

---

### 3. API Changes

#### Approval Controller

**Endpoints (unchanged):**
- `POST /approvals` – Create approval (now sends assignment notification)
- `PUT /approvals/{id}/decision` – Record decision (now sends decision notification)
- `GET /approvals/pending` – List my pending approvals
- `GET /approvals/login-target` – Get redirect URL for login

#### Escalation Controller

**Canonical Endpoints (new):**
- `POST /escalations/save` (also at `/esclation/save`)
- `GET /escalations/{id}` (also at `/esclation/{id}`)
- `GET /escalations/get-view/{id}` (also at `/esclation/get-view/{id}`)
- `GET /escalations/get-all` (also at `/esclation/get-all`)
- `DELETE /escalations/{id}` (also at `/esclation/{id}`)

**Mapping:** Both `/escalations` and `/esclation` are now supported (legacy path marked for deprecation in future)

**Location:** `controller/EscalationController.java`

---

### 4. Application Bootstrap Changes

**Updated ErmApplication.java:**
- Added `@EnableScheduling` to enable scheduled task execution
- ApprovalEscalationScheduler now auto-wires and starts on application boot

---

### 5. Repository Enhancements

**ApprovalRepository.java:**
- Added `findByStatusAndDeletedFalse()` – Find approvals by status
- Added `findByStatusAndDueAtBeforeAndDeletedFalse()` – Find overdue pending approvals (for escalation scheduler)

---

## Approval Lifecycle (Complete)

### 1. **Approval Creation**
```
User A submits request → Approval created (status=PENDING)
  ├─ Approver B is assigned
  ├─ Notification sent to B (ASSIGNED event)
  ├─ assignedNotifiedAt = NOW
  └─ Approval visible in B's /approvals/pending list
```

### 2. **Approval Decision**
```
Approver B opens approval → Clicks Approve/Reject
  ├─ Check: B is the assigned approver (enforced)
  ├─ Check: Status is still PENDING (no duplicate decisions)
  ├─ Status updated (APPROVED / REJECTED)
  ├─ closedAt = NOW
  ├─ decisionNotifiedAt = NOW
  ├─ Notifications sent to:
  │  ├─ User A (submitter)
  │  ├─ All tagged members (@name)
  │  └─ All explicit recipient IDs
  └─ Approval no longer in PENDING list
```

### 3. **Escalation (Automatic)**
```
Scheduler runs every 60 seconds:
  ├─ Check: dueAt is before NOW
  ├─ Check: Status is still PENDING
  ├─ escalationLevel++
  ├─ escalatedAt = NOW
  ├─ Notification sent to Approver B (ESCALATED event)
  └─ Approver B sees escalation reminder in notifications
```

### 4. **Escalation (Manual)**
```
Management Portal:
  ├─ View escalation rules by organization
  ├─ Configure escalation users (with User entity relationships)
  ├─ Manually trigger escalation if needed
  └─ Audit trail tracked in notification records
```

---

## Backward Compatibility

### Data Layer
- No breaking schema changes; all new columns are nullable/optional
- Existing approvals will migrate smoothly
- Legacy email-based escalation configurations continue to work

### API Layer
- All existing approval endpoints return same response structure (enhanced with new fields)
- Escalation controller supports both `/esclation` and `/escalations`
- Clients can ignore new fields if not used

### Notification Behavior
- **Previous:** Notifications sent only on decision (APPROVED/REJECTED)
- **Now:** 
  - Assignment notification sent immediately when approval is created
  - Decision notification sent on approval decision
  - Escalation notification sent when escalation is triggered
  - All notifications logged with event type for audit trail

---

## Configuration & Deployment

### Required Steps

1. **Database Migration**
   - Apply migration scripts (provided above) in order
   - Verify all new columns and indexes are created
   - Backup production database before migration

2. **Application Configuration** (application-prod.yaml)
   ```yaml
   approval:
     escalation:
       fixed-delay: 60000  # Check for escalations every minute
       # Adjust based on SLA requirements
   
   spring:
     mail:
       enabled: true       # Ensure mail is configured if not already
       host: ${MAIL_HOST}
       port: ${MAIL_PORT}
       username: ${MAIL_USERNAME}
       password: ${MAIL_PASSWORD}
   ```

3. **Build & Deploy**
   - Build: `mvn clean package -pl ERM_backend`
   - Deploy to target environment
   - Verify `@EnableScheduling` is active in logs

4. **Verification Checklist**
   - [ ] Application starts without errors
   - [ ] Scheduler log shows: `Scheduling enabled` or similar
   - [ ] Create test approval, verify assignment notification sent
   - [ ] Approve/reject approval, verify decision notification sent
   - [ ] Set `dueAt` to past date, wait 60 seconds, verify escalation triggered
   - [ ] Check notification table for event type values (ASSIGNED, APPROVED, etc.)

---

## Testing Strategy

### Unit Tests
- `ApprovalServiceTest.java` – Tests assignment and decision notifications
- Mock NotificationService to verify calls
- Test duplicate decision prevention
- Test approver authorization validation

### Integration Tests
1. **Approval Lifecycle Test:**
   - Create approval → Verify assignment notification
   - Decide approval → Verify decision notification
   - Verify notification records created in DB

2. **Escalation Test:**
   - Create approval with dueAt = past
   - Run scheduler manually or wait
   - Verify escalation level incremented
   - Verify escalation notification sent

3. **Recipient Resolution Test:**
   - Create approval with tagged members
   - Verify all mentioned users receive notifications
   - Verify explicit recipient IDs receive notifications

### Regression Tests
- Existing approval API contracts still work
- No null pointer exceptions on optional fields
- Organization/company context preserved

---

## Known Limitations & Future Enhancements

### Current Scope
- ✅ Single-level escalation support
- ✅ Email-only notifications
- ✅ No re-escalation rules (escalates once per due date)

### Future Enhancements (Post-MVP)
- [ ] Multi-level escalation chains
- [ ] In-app notification support (beyond email)
- [ ] Notification retry with exponential backoff
- [ ] Bulk approval operations
- [ ] Approval templates with preconfigured escalation rules
- [ ] Dashboard widgets for pending approvals and escalations
- [ ] Approval audit trail UI

---

## Troubleshooting

### Issue: No notifications being sent

**Check:**
1. Mail service is enabled and configured correctly
2. NotificationService constructor is properly wired (check application logs for autowiring errors)
3. Recipient email addresses are not null/blank
4. Check `notification` table for records with status=FAILED and `failure_reason` field

### Issue: Escalation not triggering

**Check:**
1. Scheduler is enabled (`@EnableScheduling` present in `ErmApplication.java`)
2. Approvals have `dueAt` set to past date
3. Approvals are still in `PENDING` status
4. Check application logs for `Escalated approval` messages
5. Manually query: `SELECT * FROM approval WHERE status='PENDING' AND due_at < NOW()`

### Issue: Duplicate notifications

**Check:**
1. Escalation scheduler not running twice (check thread pool configuration)
2. Notification retry logic not misconfigured

---

## Support & Questions

For issues or clarifications, refer to:
- [WorkflowEventType](constant/WorkflowEventType.java)
- [NotificationService](serviceimpl/NotificationService.java)
- [ApprovalService](serviceimpl/ApprovalService.java)
- [ApprovalEscalationScheduler](serviceimpl/ApprovalEscalationScheduler.java)

---

**Implementation Date:** 2026-07-04  
**Version:** 1.0.0  
**Status:** Ready for Integration Testing
