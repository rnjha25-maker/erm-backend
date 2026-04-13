package com.erm.erm_command_organization.service;

import com.erm.erm_command_organization.dto.responseDTO.DashboardDto;
import com.erm.erm_command_organization.exception.ResourceNotFoundException;

public interface IDashboardService {

	DashboardDto getDashboardData(String period) throws ResourceNotFoundException;

}
