-- Risk 1:N RiskAssessment and KriKpiReview M:1 RiskAssessment migration
-- Run manually on environments where Hibernate ddl-auto is not used.

-- Allow multiple risk_assessment rows per risk (drop unique constraint on risk_id if present).
-- Adjust index name to match your schema; inspect with: SHOW INDEX FROM risk_assessment;
-- ALTER TABLE risk_assessment DROP INDEX UK_risk_assessment_risk_id;

ALTER TABLE kri_kpi_reviews
    ADD COLUMN IF NOT EXISTS risk_assessment_id BIGINT NULL;

ALTER TABLE kri_kpi_reviews
    ADD CONSTRAINT fk_kri_kpi_reviews_risk_assessment
    FOREIGN KEY (risk_assessment_id) REFERENCES risk_assessment(id);
