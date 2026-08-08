package ermorg.erm.dto.response;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import ermorg.erm.constant.RiskAcceptanceLevel;
import ermorg.erm.constant.RiskValueUnit;
import ermorg.erm.model.KriKpiReview;
import ermorg.erm.model.Risk;
import ermorg.erm.model.RiskAssessment;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class KriKpiReviewResponseDTO {

    private long kriId;
    private String businessObjectives;
    private String businessFunction;
    private long riskOwner;
    private long riskId;
    private long riskAssessmentId;
    private String riskTitle;

    // ✅ ONLY DTO (NO ENTITY)
    private List<SubRiskResponse> subRiskIds = new ArrayList<>();

    private String target;
    private String keyRiskParameters;
    private String keyRiskIndicatorKri;
    private String typesOfKeyRiskIndicatorKri;
    private String typeOfRiskIndicator;

    private String performanceIndicators;
    private String stakeholderDepartments;
    private String riskToleranceRangeMinValue;
    private String riskToleranceRangeMaxValue;
    private String targets;
    private String activities;
    private String thresholds;
    private String riskToleranceStatus;
    private String riskAppetite;
    private String escalationMatrix;
    private String measurableParameters;
    private long reporting;
    private String unitOfMeasurement;
    private String reportingFrequency;
    private String currency;
    private RiskValueUnit valueUnit;
    private String targetValue;
    private String actualValue;
    private String actuals;

    private String january;
    private String february;
    private String march;
    private String april;
    private String may;
    private String june;
    private String july;
    private String august;
    private String september;
    private String october;
    private String november;
    private String december;

    private String q1;
    private String q2;
    private String q3;
    private String q4;

    private String kriType;
    private String kriAppetiteStatus;
    private String riskAppetiteStatus;
    private RiskAcceptanceLevel riskAcceptanceLevel;

    private long kriEvaluationBy;
    private String kriEvaluationFrequency;

    private String dueDate;
    private String actualDate;
    private long overDue;
    private String lastKriEvaluationDate;
    private String nextEvaluationDate;

    private String status;

    // ❌ REMOVE THIS (IMPORTANT)
    // private List<SubRisk> subRisks;

    public KriKpiReviewResponseDTO(KriKpiReview kriKpiReview) {

        this.kriId = kriKpiReview.getId();

        if (kriKpiReview.getRisk() != null) {
            this.riskId = kriKpiReview.getRisk().getId();
            this.riskTitle = kriKpiReview.getRisk().getRisktitle();
        }

        if (kriKpiReview.getRiskAssessment() != null) {
            this.riskAssessmentId = kriKpiReview.getRiskAssessment().getId();
        }

        // ✅ SAFE mapping (DTO only)
        if (kriKpiReview.getSubRisks() != null) {
            this.subRiskIds = kriKpiReview.getSubRisks()
                    .stream()
                    .map(SubRiskResponse::new)
                    .toList();
        }

        this.businessObjectives = kriKpiReview.getBusinessObjectives();
        this.businessFunction = kriKpiReview.getBusinessFunction();

        if (kriKpiReview.getRiskOwner() != null) {
            this.riskOwner = kriKpiReview.getRiskOwner().getId();
        }

        this.target = kriKpiReview.getTarget();
        this.keyRiskParameters = kriKpiReview.getKeyRiskParameters();
        this.keyRiskIndicatorKri = kriKpiReview.getKeyRiskIndicatorKri();
        this.typesOfKeyRiskIndicatorKri = kriKpiReview.getTypesOfKeyRiskIndicatorKri();
        this.typeOfRiskIndicator = kriKpiReview.getTypeOfRiskIndicator();

        this.performanceIndicators = kriKpiReview.getPerformanceIndicators();
        this.stakeholderDepartments = kriKpiReview.getStakeholderDepartments();
        this.riskToleranceRangeMinValue = kriKpiReview.getRiskToleranceRangeMinValue();
        this.riskToleranceRangeMaxValue = kriKpiReview.getRiskToleranceRangeMaxValue();
        this.targets = kriKpiReview.getTargets();
        this.activities = kriKpiReview.getActivities();
        this.thresholds = kriKpiReview.getThresholds();
        this.riskToleranceStatus = kriKpiReview.getRiskToleranceStatus();
        this.riskAppetite = kriKpiReview.getRiskAppetite();
        this.escalationMatrix = kriKpiReview.getEscalationMatrix();
        this.measurableParameters = kriKpiReview.getLevelOfMeasurementLevel();
        if (kriKpiReview.getReporting() != null) {
            this.reporting = kriKpiReview.getReporting().getId();
        }
        this.unitOfMeasurement = kriKpiReview.getUnitOfMeasurement();

        this.reportingFrequency = kriKpiReview.getReportingFrequency();
        this.currency = kriKpiReview.getCurrency();
        this.valueUnit = kriKpiReview.getValueUnit();
        this.targetValue = kriKpiReview.getTargetValue();
        this.actualValue = kriKpiReview.getActualValue();
        this.actuals = kriKpiReview.getActuals();

        this.january = kriKpiReview.getJanuary();
        this.february = kriKpiReview.getFebruary();
        this.march = kriKpiReview.getMarch();
        this.april = kriKpiReview.getApril();
        this.may = kriKpiReview.getMay();
        this.june = kriKpiReview.getJune();
        this.july = kriKpiReview.getJuly();
        this.august = kriKpiReview.getAugust();
        this.september = kriKpiReview.getSeptember();
        this.october = kriKpiReview.getOctober();
        this.november = kriKpiReview.getNovember();
        this.december = kriKpiReview.getDecember();

        this.q1 = kriKpiReview.getQ1();
        this.q2 = kriKpiReview.getQ2();
        this.q3 = kriKpiReview.getQ3();
        this.q4 = kriKpiReview.getQ4();

        this.kriType = kriKpiReview.getKriType();
        this.kriAppetiteStatus = kriKpiReview.getKriAppetiteStatus();
        this.riskAppetiteStatus = kriKpiReview.getRiskAppetiteStatus();
        this.riskAcceptanceLevel = kriKpiReview.getRiskAcceptanceLevel();

        if (kriKpiReview.getKriEvaluationBy() != null) {
            this.kriEvaluationBy = kriKpiReview.getKriEvaluationBy().getId();
        }

        this.kriEvaluationFrequency = kriKpiReview.getKriEvaluationFrequency();

        this.dueDate = kriKpiReview.getDueDate() != null ? kriKpiReview.getDueDate().toString() : null;
        this.actualDate = kriKpiReview.getActualDate() != null ? kriKpiReview.getActualDate().toString() : null;
        this.lastKriEvaluationDate = kriKpiReview.getLastKriEvaluationDate() != null
                ? kriKpiReview.getLastKriEvaluationDate().toString()
                : null;

        this.nextEvaluationDate = kriKpiReview.getNextEvaluationDate() != null
                ? kriKpiReview.getNextEvaluationDate().toString()
                : null;

        this.status = kriKpiReview.getStatus();

        this.riskToleranceStatus =
                kriKpiReview.getRiskAssessment() != null
                        ? kriKpiReview.getRiskAssessment().getRiskToleranceStatus()
                        : "";

    // ✅ SAFE derived field
        if (this.riskToleranceStatus == null) {
            this.riskToleranceStatus = Optional.ofNullable(kriKpiReview.getRisk())
                    .map(Risk::getRiskAssessments)
                    .filter(list -> !list.isEmpty())
                    .map(list -> list.get(0))
                    .map(RiskAssessment::getRiskToleranceStatus)
                    .orElse("");
        }
}
}
