package ermorg.erm.dto.response;

import java.math.BigDecimal;

import ermorg.erm.model.ERMMaturityScore;
import lombok.Data;

@Data
public class ErmMaturityScoreResponse {

	private long scoreId;
	private String assessmentAreaName;
	private Long assessmentAreaId;
	private String keyAssessmentParameters;
	private String weightageScore;
	private String marksAchieved;

	public ErmMaturityScoreResponse() {
	}

	public ErmMaturityScoreResponse(ERMMaturityScore score) {
		this.scoreId = score.getId() != null ? score.getId() : 0L;
		this.assessmentAreaName = score.getAssessmentAreaName();
		this.assessmentAreaId = score.getAssessmentAreaId();
		this.keyAssessmentParameters = score.getKeyAssessmentParameters();
		this.weightageScore = score.getWeightageScore() != null ? score.getWeightageScore().toPlainString() : null;
		this.marksAchieved = score.getMarksAchieved();
	}
}
