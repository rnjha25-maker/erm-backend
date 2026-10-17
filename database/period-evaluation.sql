-- Additive MySQL 8 migration. Apply before deploying period-evaluation APIs.
-- Existing category rows, IDs, subrisk links and approvals are not moved or backfilled.
CREATE TABLE period_evaluation (
    id BIGINT NOT NULL AUTO_INCREMENT PRIMARY KEY,
    organization_id BIGINT NOT NULL,
    company_id BIGINT NOT NULL,
    module VARCHAR(20) NOT NULL,
    source_id BIGINT NOT NULL,
    risk_id BIGINT NULL,
    branch_id BIGINT NULL,
    department_id VARCHAR(255) NULL,
    source_creator_id BIGINT NULL,
    previous_evaluation_id BIGINT NULL,
    reporting_year INT NOT NULL,
    year_type VARCHAR(20) NOT NULL,
    frequency VARCHAR(20) NOT NULL,
    period_number INT NOT NULL,
    period_start DATE NOT NULL,
    period_end DATE NOT NULL,
    state VARCHAR(20) NOT NULL DEFAULT 'DRAFT',
    revision BIGINT NOT NULL DEFAULT 0,
    source_snapshot JSON NOT NULL,
    evaluation_values JSON NOT NULL,
    created_by_id BIGINT NOT NULL,
    created_at DATETIME(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6),
    updated_at DATETIME(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6),
    completed_at DATETIME(6) NULL,
    effective_date DATE NULL,
    UNIQUE KEY uq_period_evaluation (organization_id,company_id,module,source_id,year_type,reporting_year,frequency,period_number),
    KEY idx_period_history (organization_id,company_id,module,source_id,period_start),
    KEY idx_period_chart (organization_id,company_id,module,state,effective_date,risk_id),
    CONSTRAINT fk_period_previous FOREIGN KEY(previous_evaluation_id) REFERENCES period_evaluation(id),
    CONSTRAINT ck_period_frequency CHECK(frequency IN ('MONTHLY','QUARTERLY')),
    CONSTRAINT ck_period_state CHECK(state IN ('DRAFT','COMPLETED')),
    CONSTRAINT ck_period_dates CHECK(period_start<period_end)
);

CREATE TABLE period_evaluation_evidence (
    evaluation_id BIGINT NOT NULL,
    document_id CHAR(36) NOT NULL,
    inherited BOOLEAN NOT NULL DEFAULT FALSE,
    description VARCHAR(2000) NULL,
    PRIMARY KEY(evaluation_id,document_id),
    KEY idx_period_document(document_id),
    CONSTRAINT fk_period_evidence FOREIGN KEY(evaluation_id) REFERENCES period_evaluation(id)
);
