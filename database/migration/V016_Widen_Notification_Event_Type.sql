-- Notification.event_type stores WorkflowEventType enum names as strings.
-- Keep the column wide enough for current and future enum values.
-- Verify the existing type/default in the target database before applying.
ALTER TABLE notification
    MODIFY COLUMN event_type VARCHAR(32) NOT NULL;
