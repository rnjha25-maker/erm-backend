package ermorg.erm.erm_command_organization.serviceimpl;

import java.util.List;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import ermorg.erm.erm_command_organization.dto.responseDTO.PlanDto;
import ermorg.erm.erm_command_organization.model.Plan;
import ermorg.erm.erm_command_organization.repository.PlanRepository;
import ermorg.erm.erm_command_organization.service.IPlanService;

@Service
public class PlanService implements IPlanService {

	@Autowired
	private PlanRepository planRepository;
	@Override
	public PlanDto getPlan(Long planId) {
		
		Plan planEntity = planRepository.findById(planId)
		.filter(plan -> !plan.getDeleted())
		.orElseThrow(() -> new RuntimeException("Plan not found."));
		
		return new PlanDto(planEntity);
	}

	@Override
	public List<PlanDto> getAllPlans() {
		
		List<Plan> plans = planRepository.findAll();
		
		return plans.stream().filter(plan -> !plan.getDeleted()).map(plan -> new PlanDto(plan))
				.collect(Collectors.toList());
	}

}
