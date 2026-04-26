package ermorg.erm.dto.response;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import ermorg.erm.model.KriKpiReview;

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
    private long riskId;//new
    private List<Long> subRiskIds = new ArrayList<>();//new
    private String target;
    private String keyRiskParameters;
    private String keyRiskIndicatorKri;
    private String typesOfKeyRiskIndicatorKri;
    private String typeOfRiskIndicator;//new
    
    private String performanceIndicators;
    private String stakeholderDepartments;
    private String riskToleranceRangeMinValue;
    private String riskToleranceRangeMaxValue;
    private String targets;
    private String activities;
    private String thresholds;
    private String riskToleranceStatus; //new
    private String riskAppetite;
    private String escalationMatrix;
    private String measurableParameters; //updated
    private long reporting; //new
    private String unitOfMeasurement; //new
    private String reportingFrequency;
    private String currency;
    private String targetValue;
    private String actualValue; // new
    private String actuals; //updated
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
    private String kriAppetiteStatus; // updated
    private long kriEvaluationBy;
    private String kriEvaluationFrequency;
    private String dueDate;
    private String actualDate;
    private long overDue;
    private String lastKriEvaluationDate;
    private String nextEvaluationDate;
    private String status;
    
    public KriKpiReviewResponseDTO(KriKpiReview kriKpiReview) {
	    this.kriId = kriKpiReview.getId();
	    this.businessObjectives = kriKpiReview.getBusinessObjectives();
	    this.businessFunction = kriKpiReview.getBusinessFunction();
	    this.riskOwner = kriKpiReview.getRiskOwner().getId();
	    this.target = kriKpiReview.getTarget();
	    this.keyRiskParameters = kriKpiReview.getKeyRiskParameters();
	    this.keyRiskIndicatorKri = kriKpiReview.getKeyRiskIndicatorKri();
	    this.typesOfKeyRiskIndicatorKri = kriKpiReview.getTypesOfKeyRiskIndicatorKri();
	    this.performanceIndicators = kriKpiReview.getPerformanceIndicators();
	    this.stakeholderDepartments = kriKpiReview.getStakeholderDepartments();
	    this.riskToleranceRangeMinValue = kriKpiReview.getRiskToleranceRangeMinValue();
	    this.riskToleranceRangeMaxValue = kriKpiReview.getRiskToleranceRangeMaxValue();
	    this.targets = kriKpiReview.getTargets();
	    this.activities = kriKpiReview.getActivities();
	    this.thresholds = kriKpiReview.getThresholds();
	    this.riskAppetite = kriKpiReview.getRiskAppetite();
	    this.escalationMatrix = kriKpiReview.getEscalationMatrix();
	    this.measurableParameters = kriKpiReview.getLevelOfMeasurementLevel();
	    this.reportingFrequency = kriKpiReview.getReportingFrequency();
	    this.currency = kriKpiReview.getCurrency();
	    this.targetValue = kriKpiReview.getTargetValue();
	    this.actualValue = kriKpiReview.getActualValue();
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
	    this.kriEvaluationBy = kriKpiReview.getKriEvaluationBy().getId();
	    this.kriEvaluationFrequency = kriKpiReview.getKriEvaluationFrequency();
	    this.dueDate = kriKpiReview.getDueDate().toString();
	    this.actualDate = kriKpiReview.getActualDate().toString();
//	    this.kriStatus = kriKpiReview.getDashboardKriStatus();
	    this.lastKriEvaluationDate = kriKpiReview.getLastKriEvaluationDate() != null ? kriKpiReview.getLastKriEvaluationDate().toString() : null;
	    this.nextEvaluationDate = kriKpiReview.getNextEvaluationDate().toString();
	    this.status = kriKpiReview.getStatus();
    }
}
