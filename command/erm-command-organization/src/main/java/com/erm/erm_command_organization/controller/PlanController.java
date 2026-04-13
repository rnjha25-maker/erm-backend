package com.erm.erm_command_organization.controller;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.erm.erm_command_organization.dto.responseDTO.GeneralResponse;
import com.erm.erm_command_organization.dto.responseDTO.PlanDto;
import com.erm.erm_command_organization.dto.responseDTO.ResponseStatus;
import com.erm.erm_command_organization.service.PlanService;

//@CrossOrigin
@RestController
@RequestMapping("/plan")
public class PlanController {

	@Autowired
	private PlanService planService;
	@RequestMapping("/get-allPlans")
	public GeneralResponse<List<PlanDto>> getAllPlans() {
		GeneralResponse<List<PlanDto>> response = new GeneralResponse<>();
		
		List<PlanDto> plans = planService.getAllPlans();
		
		response.setData(plans);
		response.setMessage("Plans found");
		response.setStatus(ResponseStatus.SUCCESS);
		
		return response;
	}
}
