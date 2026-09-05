package ermorg.erm.model;

import java.math.BigDecimal;
import java.util.Date;

import ermorg.erm.constant.RiskAcceptanceLevel;
import ermorg.erm.constant.RiskValueUnit;
import jakarta.persistence.AttributeOverride;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Index;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.Temporal;
import jakarta.persistence.TemporalType;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "kpa_kpi_review", indexes = {
    @Index(name = "idx_kpa_kpi_review_org_deleted", columnList = "organization_id, is_deleted"),
    @Index(name = "idx_kpa_kpi_review_company_deleted", columnList = "company_id, is_deleted"),
    @Index(name = "idx_kpa_kpi_review_kpa", columnList = "kpa"),
    @Index(name = "idx_kpa_kpi_review_status", columnList = "status")
})
@AttributeOverride(name = "deleted", column = @Column(name = "is_deleted", nullable = false))
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
public class KpaKpiReview extends BaseModel {

    @Column(name = "kpa", nullable = false, length = 255)
    private String kpa;

    @Column(name = "business_objectives", length = 1000)
    private String businessObjectives;

    @Column(name = "business_function", length = 255)
    private String businessFunction;

    @ManyToOne
    @JoinColumn(name = "owner_id", nullable = false)
    private User owner;

    @ManyToOne
    @JoinColumn(name = "reporting_id")
    private User reporting;

    @ManyToOne
    @JoinColumn(name = "organization_id", nullable = false)
    private Organization organization;

    @ManyToOne
    @JoinColumn(name = "company_id", nullable = false)
    private Company company;

    @Column(name = "target", length = 1000)
    private String target;

    @Column(name = "key_performance_parameters", length = 1000)
    private String keyPerformanceParameters;

    @Column(name = "key_performance_indicator", nullable = false, length = 1000)
    private String keyPerformanceIndicator;

    @Column(name = "types_of_kpi", length = 100)
    private String typesOfKpi;

    @Column(name = "performance_indicators", length = 1000)
    private String performanceIndicators;

    @Column(name = "stakeholder_departments", length = 1000)
    private String stakeholderDepartments;

    @Column(name = "performance_tolerance_min_value", precision = 19, scale = 4)
    private BigDecimal performanceToleranceMinValue;

    @Column(name = "performance_tolerance_max_value", precision = 19, scale = 4)
    private BigDecimal performanceToleranceMaxValue;

    @Column(name = "targets", length = 1000)
    private String targets;

    @Column(name = "activities", length = 1000)
    private String activities;

    @Column(name = "thresholds", length = 1000)
    private String thresholds;

    @Column(name = "performance_appetite", length = 1000)
    private String performanceAppetite;

    @Column(name = "escalation_matrix", length = 1000)
    private String escalationMatrix;

    @Column(name = "level_of_measurement_level", length = 255)
    private String levelOfMeasurementLevel;

    @Column(name = "reporting_frequency", nullable = false, length = 30)
    private String reportingFrequency;

    @Column(name = "currency", length = 3)
    private String currency;

    @Enumerated(EnumType.STRING)
    @Column(name = "value_unit", length = 30)
    private RiskValueUnit valueUnit;

    @Column(name = "target_value", precision = 19, scale = 4)
    private BigDecimal targetValue;

    @Column(name = "actual_value", precision = 19, scale = 4)
    private BigDecimal actualValue;

    @Column(name = "january", precision = 19, scale = 4)
    private BigDecimal january;

    @Column(name = "february", precision = 19, scale = 4)
    private BigDecimal february;

    @Column(name = "march", precision = 19, scale = 4)
    private BigDecimal march;

    @Column(name = "april", precision = 19, scale = 4)
    private BigDecimal april;

    @Column(name = "may", precision = 19, scale = 4)
    private BigDecimal may;

    @Column(name = "june", precision = 19, scale = 4)
    private BigDecimal june;

    @Column(name = "july", precision = 19, scale = 4)
    private BigDecimal july;

    @Column(name = "august", precision = 19, scale = 4)
    private BigDecimal august;

    @Column(name = "september", precision = 19, scale = 4)
    private BigDecimal september;

    @Column(name = "october", precision = 19, scale = 4)
    private BigDecimal october;

    @Column(name = "november", precision = 19, scale = 4)
    private BigDecimal november;

    @Column(name = "december", precision = 19, scale = 4)
    private BigDecimal december;

    @Column(name = "q1", precision = 19, scale = 4)
    private BigDecimal q1;

    @Column(name = "q2", precision = 19, scale = 4)
    private BigDecimal q2;

    @Column(name = "q3", precision = 19, scale = 4)
    private BigDecimal q3;

    @Column(name = "q4", precision = 19, scale = 4)
    private BigDecimal q4;

    @Column(name = "kpi_type", length = 50)
    private String kpiType;

    @Column(name = "kra_rating", length = 30)
    private String kraRating;

    @Column(name = "risk_appetite_status")
    private String riskAppetiteStatus;

    @Enumerated(EnumType.STRING)
    @Column(name = "risk_acceptance_level", length = 100)
    private RiskAcceptanceLevel riskAcceptanceLevel;

    @ManyToOne
    @JoinColumn(name = "kpi_evaluation_by", nullable = false)
    private User kpiEvaluationBy;

    @Column(name = "kpi_evaluation_frequency", length = 30)
    private String kpiEvaluationFrequency;

    @Column(name = "potential_loss_percentage", precision = 19, scale = 4)
    private BigDecimal potentialLossPercentage;

    @Column(name = "yearly_frequency")
    private Integer yearlyFrequency;

    @Column(name = "annual_loss_expectancy", precision = 19, scale = 4)
    private BigDecimal annualLossExpectancy;

    @Column(name = "due_date")
    @Temporal(TemporalType.DATE)
    private Date dueDate;

    @Column(name = "actual_date")
    @Temporal(TemporalType.DATE)
    private Date actualDate;

    @Column(name = "last_kpi_evaluation_date")
    @Temporal(TemporalType.DATE)
    private Date lastKpiEvaluationDate;

    @Column(name = "next_evaluation_date")
    @Temporal(TemporalType.DATE)
    private Date nextEvaluationDate;

    @Column(name = "status", nullable = false, length = 30)
    private String status;
}
