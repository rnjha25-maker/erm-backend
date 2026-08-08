package ermorg.erm.model;

import java.math.BigDecimal;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Entity
@Table(name = "erm_maturity_scores", indexes = {
	@jakarta.persistence.Index(name = "idx_ermaturity_score_parent_deleted", columnList = "maturity_assessment_id, deleted")
})
public class ERMMaturityScore extends BaseModel {

	@Column(name = "assessment_area_name")
	private String assessmentAreaName;

	@Column(name = "assessment_area_id")
	private Long assessmentAreaId;

	@Column(name = "key_assessment_parameters")
	private String keyAssessmentParameters;

	@Column(name = "weightage_score")
	private BigDecimal weightageScore;

	@Column(name = "marks_achieved")
	private String marksAchieved;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "maturity_assessment_id", nullable = false)
	private ERMMaturityAssessment maturityAssessment;
}
