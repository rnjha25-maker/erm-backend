package ermorg.erm.model.history;

import ermorg.erm.model.BaseModel;
import ermorg.erm.model.Risk;
import ermorg.erm.model.SubRisk;
import com.fasterxml.jackson.annotation.JsonIgnore;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Entity;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Entity
public class SubRiskHistory extends BaseModel{

	@ManyToOne(cascade=CascadeType.MERGE)
	@JoinColumn(name="sub_risk_id")
	private SubRisk oSubRisk;
	private String subRisk;
	
	private String operation;
	
	@ManyToOne
    @JsonIgnore
    @JoinColumn(name = "risk_history_id")
    private RiskHistory riskHistory;
	
	@ManyToOne(cascade=CascadeType.MERGE)
	@JoinColumn(name="risk_id")
	private Risk risk;
	
}
