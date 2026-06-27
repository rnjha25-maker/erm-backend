-- =============================================================================
-- Plain DDL: New maturity columns and department IDs table
-- Database: MySQL
--
-- How to run:
--   MySQL Workbench: open this file, select your database, execute all
--   CLI: mysql -h localhost -u root -p erm2 < add_erm_maturity_id_and_department_ids.sql
--
-- Select the correct database before running (dev=erm2, qa=erm):
-- =============================================================================

-- USE erm2;

-- 1) Add erm_maturity_id column (company/department group key)
ALTER TABLE erm_maturity_assessments
    ADD COLUMN erm_maturity_id VARCHAR(255) NULL
    COMMENT 'Group key: companyId or companyId_deptId...'
    AFTER next_assessment_date;

-- 2) Collection table for multiple department IDs per assessment row
CREATE TABLE erm_maturity_department_ids (
    maturity_assessment_id BIGINT NOT NULL,
    department_id          BIGINT NOT NULL,
    PRIMARY KEY (maturity_assessment_id, department_id),
    CONSTRAINT fk_erm_maturity_dept_assessment
        FOREIGN KEY (maturity_assessment_id)
        REFERENCES erm_maturity_assessments (id)
        ON DELETE CASCADE
);

-- 3) Index for count-by-group queries (9-record limit check)
CREATE INDEX idx_ermaturity_org_erm_id_deleted
    ON erm_maturity_assessments (organization_id, erm_maturity_id, deleted);
