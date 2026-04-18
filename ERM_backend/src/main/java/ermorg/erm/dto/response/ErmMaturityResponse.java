package ermorg.erm.dto.response;

import java.util.Date;

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


    public ErmMaturityResponse(ERMMaturityAssessment assessment) {
	    this.maturityId = assessment.getId();
	    this.assessmentAreaName = assessment.getAssessmentAreaName();
	    this.assessmentAreaId = assessment.getAssessmentAreaId();
	    this.keyAssessmentParameters = assessment.getKeyAssessmentParameters();
	    this.status = assessment.getStatus();
	    this.weightageScore = assessment.getWeightageScore().toString();
	    this.marksAchieved = assessment.getMarksAchieved();
	    this.overallMaturityLevel = assessment.getOverallMaturityLevel();
	    this.assessedBy = assessment.getAssessedBy();
	    this.dueDate = assessment.getDueDate();
	    this.actualDate = assessment.getActualDate();
	    this.lastAssessmentDate = assessment.getLastAssessmentDate();
	    this.nextAssessmentDate = assessment.getNextAssessmentDate();
    }
}
