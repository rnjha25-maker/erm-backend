package ermorg.erm.evaluation;

import java.util.*;

/** Identifiers are server-owned constants, never interpolated from request text. */
public enum EvaluationModule {
    CONTROL("risk_controls", "risk_control_id", "riskcontrol",
        "control_source control_title control_type control_mechanism access_controls process_controls physical_controls technical_control risk_appetite_status risk_acceptance_level"),
    TREATMENT("risk_risponse_treatment", "risk_response_treatment_id", "riskresponsetreatment",
        "control_presence control_description control_gaps_identified recommended_control management_action_plan contingency_plans control_effectiveness_percentage control_effectiveness_weightage control_evaluation_status risk_treatment_status risk_appetite_status risk_acceptance_level evidence_require supporting_evidence_artifacts_description control_evaluation_by control_status"),
    REVIEW("risk_reviews", "risk_review_id", "riskreview",
        "revised_likelihood revised_velocity likelihood_probability revised_financial_impact revised_operational_impact revised_customer_impact revised_reputational_impact revised_legal_compliance_impact revise_impact_score residual_risk_score_range residual_risk_rating risk_treatment_status risk_tolerance_status risk_appetite_status risk_acceptance_level value_unit currency risk_evaluation_by status assets_value per_of_potential_loss yearly_frequency annual_loss_expectancy review_type"),
    KRI("kri_kpi_reviews", "kri_kpi_review_id", "krikpireview",
        "actual_value risk_appetite_status kri_appetite_status risk_tolerance_status status"),
    KPI("kpa_kpi_review", null, "kpakpireview",
        "actual_value risk_appetite_status kra_rating status");

    public final String table, subRiskColumn, approvalModule;
    public final Set<String> fields;
    EvaluationModule(String table,String subRiskColumn,String approvalModule,String fields) {
        this.table=table;this.subRiskColumn=subRiskColumn;this.approvalModule=approvalModule;
        this.fields=Collections.unmodifiableSet(new LinkedHashSet<>(List.of(fields.split(" "))));
    }
    public String deletedColumn() { return this==KPI?"is_deleted":"deleted"; }
}
