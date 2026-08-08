-- =============================================================================
-- Plain DDL: Move maturity area / weightage / marks into child table (Approach 1)
-- Database: MySQL
--
-- How to run:
--   MySQL Workbench: open this file, select your database, execute all
--   CLI: mysql -h localhost -u root -p erm2 < add_erm_maturity_scores_one_to_many.sql
--
-- Select the correct database before running (dev=erm2, qa=erm):
-- =============================================================================

-- USE erm2;

-- 1) Child table for assessment-area scores (1 parent : N children)
CREATE TABLE IF NOT EXISTS erm_maturity_scores (
    id                        BIGINT       NOT NULL,
    created_at                DATETIME(6)  NULL,
    updated_at                DATETIME(6)  NULL,
    client_ip                 VARCHAR(255) NULL,
    deleted                   BIT(1)       NULL,
    created_by_id             BIGINT       NULL,
    updated_by_id             BIGINT       NULL,
    assessment_area_name      VARCHAR(255) NULL,
    assessment_area_id        BIGINT       NULL,
    key_assessment_parameters VARCHAR(255) NULL,
    weightage_score           DECIMAL(19, 2) NULL,
    marks_achieved            VARCHAR(255) NULL,
    maturity_assessment_id    BIGINT       NOT NULL,
    PRIMARY KEY (id),
    CONSTRAINT fk_erm_maturity_score_assessment
        FOREIGN KEY (maturity_assessment_id)
        REFERENCES erm_maturity_assessments (id)
        ON DELETE CASCADE
);

CREATE INDEX idx_ermaturity_score_parent_deleted
    ON erm_maturity_scores (maturity_assessment_id, deleted);

-- 2) Backfill: one child per existing assessment row that still has area/score columns.
--    Uses a session variable for ids; afterward bump BASE_MODEL_SEQ past MAX(id) if needed.
SET @erm_score_id := (
    SELECT IFNULL(MAX(id), 0) FROM (
        SELECT id FROM erm_maturity_scores
        UNION ALL
        SELECT id FROM erm_maturity_assessments
    ) existing_ids
);

INSERT INTO erm_maturity_scores (
    id,
    created_at,
    updated_at,
    client_ip,
    deleted,
    created_by_id,
    updated_by_id,
    assessment_area_name,
    assessment_area_id,
    key_assessment_parameters,
    weightage_score,
    marks_achieved,
    maturity_assessment_id
)
SELECT
    (@erm_score_id := @erm_score_id + 1),
    a.created_at,
    a.updated_at,
    a.client_ip,
    COALESCE(a.deleted, 0),
    a.created_by_id,
    a.updated_by_id,
    a.assessment_area_name,
    a.assessment_area_id,
    a.key_assessment_parameters,
    a.weightage_score,
    a.marks_achieved,
    a.id
FROM erm_maturity_assessments a
WHERE COALESCE(a.deleted, 0) = 0
  AND NOT EXISTS (
      SELECT 1 FROM erm_maturity_scores s WHERE s.maturity_assessment_id = a.id AND COALESCE(s.deleted, 0) = 0
  )
  AND (
      a.assessment_area_name IS NOT NULL
      OR a.assessment_area_id IS NOT NULL
      OR a.key_assessment_parameters IS NOT NULL
      OR a.weightage_score IS NOT NULL
      OR (a.marks_achieved IS NOT NULL AND a.marks_achieved <> '')
  );

-- 3) Collapse sibling parents that share the same org + erm_maturity_id into one header.
--    Re-point children to the keeper (MIN id), soft-delete duplicates, merge department ids.

-- 3a) Re-point scores from duplicate parents to the keeper parent
UPDATE erm_maturity_scores s
INNER JOIN erm_maturity_assessments dup ON dup.id = s.maturity_assessment_id
INNER JOIN (
    SELECT organization_id, erm_maturity_id, MIN(id) AS keeper_id
    FROM erm_maturity_assessments
    WHERE COALESCE(deleted, 0) = 0
      AND erm_maturity_id IS NOT NULL
      AND erm_maturity_id <> ''
    GROUP BY organization_id, erm_maturity_id
    HAVING COUNT(*) > 1
) k ON k.organization_id = dup.organization_id AND k.erm_maturity_id = dup.erm_maturity_id
SET s.maturity_assessment_id = k.keeper_id
WHERE dup.id <> k.keeper_id
  AND COALESCE(dup.deleted, 0) = 0;

-- 3b) Copy department ids from duplicates onto keeper (ignore duplicates via INSERT IGNORE)
INSERT IGNORE INTO erm_maturity_department_ids (maturity_assessment_id, department_id)
SELECT k.keeper_id, d.department_id
FROM erm_maturity_department_ids d
INNER JOIN erm_maturity_assessments dup ON dup.id = d.maturity_assessment_id
INNER JOIN (
    SELECT organization_id, erm_maturity_id, MIN(id) AS keeper_id
    FROM erm_maturity_assessments
    WHERE COALESCE(deleted, 0) = 0
      AND erm_maturity_id IS NOT NULL
      AND erm_maturity_id <> ''
    GROUP BY organization_id, erm_maturity_id
    HAVING COUNT(*) > 1
) k ON k.organization_id = dup.organization_id AND k.erm_maturity_id = dup.erm_maturity_id
WHERE dup.id <> k.keeper_id
  AND COALESCE(dup.deleted, 0) = 0;

-- 3c) Soft-delete duplicate parents
UPDATE erm_maturity_assessments dup
INNER JOIN (
    SELECT organization_id, erm_maturity_id, MIN(id) AS keeper_id
    FROM erm_maturity_assessments
    WHERE COALESCE(deleted, 0) = 0
      AND erm_maturity_id IS NOT NULL
      AND erm_maturity_id <> ''
    GROUP BY organization_id, erm_maturity_id
    HAVING COUNT(*) > 1
) k ON k.organization_id = dup.organization_id AND k.erm_maturity_id = dup.erm_maturity_id
SET dup.deleted = 1
WHERE dup.id <> k.keeper_id
  AND COALESCE(dup.deleted, 0) = 0;

-- 4) Clear migrated columns on parent (entity no longer maps them; safe to NULL)
UPDATE erm_maturity_assessments
SET assessment_area_name = NULL,
    assessment_area_id = NULL,
    key_assessment_parameters = NULL,
    weightage_score = NULL,
    marks_achieved = NULL
WHERE COALESCE(deleted, 0) = 0;

-- Optional: drop unused parent columns after verifying application:
-- ALTER TABLE erm_maturity_assessments
--     DROP COLUMN assessment_area_name,
--     DROP COLUMN assessment_area_id,
--     DROP COLUMN key_assessment_parameters,
--     DROP COLUMN weightage_score,
--     DROP COLUMN marks_achieved;
