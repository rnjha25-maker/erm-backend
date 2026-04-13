package com.erm.erm_command_organization.service;

import java.util.List;

import com.erm.erm_command_organization.dto.responseDTO.PlanDto;

public interface PlanService {

	PlanDto getPlan(Long planId);
	List<PlanDto> getAllPlans();
}
