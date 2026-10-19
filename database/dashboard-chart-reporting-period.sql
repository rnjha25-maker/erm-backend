-- MySQL 8. Run once through the normal migration process BEFORE deploying the new backend.
-- Nullable columns preserve existing clients and records. Do not infer/backfill historical
-- reporting years automatically: the business must confirm each legacy entry's year basis.
ALTER TABLE kri_kpi_reviews
    ADD COLUMN reporting_year INT NULL,
    ADD COLUMN reporting_year_type VARCHAR(20) NULL,
    ADD INDEX idx_kri_chart_reporting (organization_id, company_id, deleted, reporting_year, reporting_year_type),
    ADD INDEX idx_kri_chart_created (organization_id, company_id, deleted, created_at);

ALTER TABLE kpa_kpi_review
    ADD COLUMN reporting_year INT NULL,
    ADD COLUMN reporting_year_type VARCHAR(20) NULL,
    ADD INDEX idx_kpi_chart_reporting (organization_id, company_id, is_deleted, reporting_year, reporting_year_type),
    ADD INDEX idx_kpi_chart_created (organization_id, company_id, is_deleted, created_at);

-- Inspect existing indexes and EXPLAIN plans before adding these on a large database.
CREATE INDEX idx_assessment_chart_date ON risk_assessment
    (organization_id, company_id, deleted, last_evaluation_date, risk_id);
CREATE INDEX idx_review_chart_date ON risk_reviews
    (organization_id, company_id, deleted, last_evaluation_date, risk_id);
CREATE INDEX idx_risk_chart_scope ON risk
    (organization_id, company_id, deleted, branch_id, business_function);
