package ermorg.erm.service;

import org.springframework.data.domain.Pageable;

import ermorg.erm.constant.ErmDashboardPeriodType;
import ermorg.erm.dto.response.BasicDashboardResponse;
import ermorg.erm.dto.response.CompanyAdminDashboardDto;
import ermorg.erm.dto.response.ErmDashboardSummaryResponse;
import ermorg.erm.dto.response.OrgAdminDashboardDto;
import ermorg.erm.exception.ResourceNotFoundException;

public interface IDashboardService {

	BasicDashboardResponse getBasicDashboardData(String period, Pageable pageable) throws ResourceNotFoundException;

	OrgAdminDashboardDto getAdminDashboardData(String period, Pageable pageable) throws ResourceNotFoundException;

	CompanyAdminDashboardDto getCompanyAdminDashboardData(String period, Pageable pageable) throws ResourceNotFoundException;

	ErmDashboardSummaryResponse getErmDashboardSummary(int year, ErmDashboardPeriodType periodType, Long companyId,
			Long branchId, Long functionId) throws ResourceNotFoundException;

}
