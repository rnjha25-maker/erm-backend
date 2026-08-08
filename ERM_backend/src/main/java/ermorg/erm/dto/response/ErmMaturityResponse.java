package ermorg.erm.dto.response;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import ermorg.erm.constant.RiskAcceptanceLevel;
import ermorg.erm.model.ERMMaturityAssessment;
import ermorg.erm.model.ERMMaturityScore;
import ermorg.erm.util.ErmMaturityGroupingUtil;

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

    /** Present on save/get detail; not used by list/dashboard aggregation. */
    private List<ErmMaturityScoreResponse> scores;

    public ErmMaturityResponse() {
    }

    public ErmMaturityResponse(ERMMaturityAssessment assessment) {
	    this.maturityId = assessment.getId();
	    this.status = assessment.getStatus();
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

        List<ERMMaturityScore> activeScores = ErmMaturityGroupingUtil.activeScores(assessment);
        BigDecimal totalMarks = ErmMaturityGroupingUtil.totalScoreFromScores(activeScores);
        this.totalWeightageScore = totalMarks;
        this.marksAchieved = totalMarks != null ? totalMarks.toPlainString() : "0";
        this.weightageScore = totalMarks != null ? totalMarks.toPlainString() : "0";

        if (!activeScores.isEmpty()) {
        	ERMMaturityScore first = activeScores.get(0);
        	this.assessmentAreaName = first.getAssessmentAreaName();
        	this.assessmentAreaId = first.getAssessmentAreaId();
        	this.keyAssessmentParameters = first.getKeyAssessmentParameters();
        }

        this.scores = new ArrayList<>();
        for (ERMMaturityScore score : activeScores) {
        	this.scores.add(new ErmMaturityScoreResponse(score));
        }
    }
}
