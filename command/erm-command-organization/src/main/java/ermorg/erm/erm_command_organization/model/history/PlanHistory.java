package ermorg.erm.erm_command_organization.model.history;

import ermorg.erm.erm_command_organization.model.BaseModel;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;

@Entity
@Getter
@Setter
@Table(name="Tbl_Plan_History")
public class PlanHistory extends BaseModel{
	
	@Column(name="plan_id")
	private Long planId;
	
	@Column(name="plan_name")
	private String planName;
	
	@Column(name="plan_description")
	private String planDescription;
	
	private String operation;
}
