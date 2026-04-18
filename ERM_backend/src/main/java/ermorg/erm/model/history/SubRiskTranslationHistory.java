package ermorg.erm.model.history;

import ermorg.erm.model.BaseModel;
import ermorg.erm.model.SubRisk;
import ermorg.erm.model.translation.Language;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Entity;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Entity
public class SubRiskTranslationHistory extends BaseModel{

	@ManyToOne(cascade=CascadeType.MERGE)
	@JoinColumn(name="sub_risk_id")
	private SubRisk subRisk;
	
	@ManyToOne(cascade=CascadeType.MERGE)
	@JoinColumn(name="language_id")
	private Language language;
	
	private String operation;
}
