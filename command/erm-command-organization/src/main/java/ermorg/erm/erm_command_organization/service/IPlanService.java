package ermorg.erm.erm_command_organization.service;

import java.util.List;

import ermorg.erm.erm_command_organization.dto.responseDTO.PlanDto;

public interface IPlanService {

	PlanDto getPlan(Long planId);
	List<PlanDto> getAllPlans();
}
