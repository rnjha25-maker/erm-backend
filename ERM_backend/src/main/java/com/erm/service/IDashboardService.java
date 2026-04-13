package com.erm.service;

import org.springframework.data.domain.Pageable;

import com.erm.dto.response.BasicDashboardResponse;
import com.erm.dto.response.CompanyAdminDashboardDto;
import com.erm.dto.response.OrgAdminDashboardDto;
import com.erm.exception.ResourceNotFoundException;

public interface IDashboardService {

	BasicDashboardResponse getBasicDashboardData(String period, Pageable pageable) throws ResourceNotFoundException;

	OrgAdminDashboardDto getAdminDashboardData(String period, Pageable pageable) throws ResourceNotFoundException;

	CompanyAdminDashboardDto getCompanyAdminDashboardData(String period, Pageable pageable) throws ResourceNotFoundException;

	
}
