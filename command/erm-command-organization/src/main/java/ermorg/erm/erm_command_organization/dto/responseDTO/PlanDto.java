package ermorg.erm.erm_command_organization.dto.responseDTO;

import ermorg.erm.erm_command_organization.model.Plan;

import lombok.Data;

@Data
public class PlanDto {
	
	private long planId;
	private String planName;
	private String planDescription;
	
	public PlanDto(Plan plan) {
		this.planId = plan.getId();
		this.planName = plan.getPlanName();
		this.planDescription = plan.getPlanDescription();
	}
	
}
