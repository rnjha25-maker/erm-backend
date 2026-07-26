package ermorg.erm.dto.response;

import java.math.BigDecimal;
import java.util.Date;
import java.util.List;

import ermorg.erm.constant.RiskAcceptanceLevel;
import ermorg.erm.model.ERMMaturityAssessment;

import lombok.Data;

@Data
public class ErmMaturityResponse {

	private long maturityId;

    private String assessmentAreaName;

    private Long assessmentAreaId;

    private String keyAssessmentParameters;

    private String status;

    private String weightageScore;

    private String marksAchieved;

    private String overallMaturityLevel;

    private String assessedBy;

    private Date dueDate;

    private Date actualDate;

    private Date lastAssessmentDate;
    private Date nextAssessmentDate;
    private String riskAppetiteStatus;
    private RiskAcceptanceLevel riskAcceptanceLevel;

    private String ermMaturityId;

    private List<Long> departmentIds;

    private String displayLabel;

    private Long companyId;

    private BigDecimal totalWeightageScore;

    public ErmMaturityResponse() {
    }

    public ErmMaturityResponse(ERMMaturityAssessment assessment) {
	    this.maturityId = assessment.getId();
	    this.assessmentAreaName = assessment.getAssessmentAreaName();
	    this.assessmentAreaId = assessment.getAssessmentAreaId();
	    this.keyAssessmentParameters = assessment.getKeyAssessmentParameters();
	    this.status = assessment.getStatus();
	    this.weightageScore = assessment.getWeightageScore() != null ? assessment.getWeightageScore().toString() : null;
	    this.marksAchieved = assessment.getMarksAchieved();
	    this.overallMaturityLevel = assessment.getOverallMaturityLevel();
	    this.assessedBy = assessment.getAssessedBy();
	    this.dueDate = assessment.getDueDate();
	    this.actualDate = assessment.getActualDate();
	    this.lastAssessmentDate = assessment.getLastAssessmentDate();
	    this.nextAssessmentDate = assessment.getNextAssessmentDate();
	    this.ermMaturityId = assessment.getErmMaturityId();
	    this.departmentIds = assessment.getDepartmentIds();
        this.riskAppetiteStatus = assessment.getRiskAppetiteStatus();
        this.riskAcceptanceLevel = assessment.getRiskAcceptanceLevel();
        if (assessment.getCompany() != null) {
        	this.companyId = assessment.getCompany().getId();
        }
    }
}
