package ermorg.erm.service;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;

import ermorg.erm.dto.response.BasicDashboardResponse;
import ermorg.erm.dto.response.CompanyAdminDashboardDto;
import ermorg.erm.dto.response.CustomResponse;
import ermorg.erm.dto.response.OrgAdminDashboardDto;
import ermorg.erm.dto.response.RiskResponse;
import ermorg.erm.exception.ResourceNotFoundException;
import ermorg.erm.model.Branch;
import ermorg.erm.model.Company;
import ermorg.erm.model.Organization;
import ermorg.erm.model.Risk;
import ermorg.erm.model.User;
import ermorg.erm.repository.BranchRepository;
import ermorg.erm.repository.CompanyRepository;
import ermorg.erm.repository.RiskRepository;
import ermorg.erm.repository.UserRepository;
import ermorg.erm.util.CompanyContext;
import ermorg.erm.util.OrganizationContext;
import ermorg.erm.util.UserContext;
import ermorg.erm.util.mapper.CustomResponseMapper;
import org.springframework.data.domain.Pageable;

@Service
public class DashboardService implements IDashboardService {

	@Autowired
	private RiskRepository riskRepository;

	@Autowired
	private CustomResponseMapper customResponseMapper;

	@Autowired
	private CompanyRepository companyRepository;

	@Autowired
	private DepartmentRepository departmentRepostory;

	@Autowired
	private UserRepository userRepository;
	@Autowired
	private BranchRepository branchRepository;

	@Override
	public BasicDashboardResponse getBasicDashboardData(String period, Pageable pageable) throws ResourceNotFoundException 
	{

		Organization organization = OrganizationContext.getOrganization(); // TODO: check if this is correct.U
		User user = UserContext.getUser();
		if (user == null) {
			throw new ResourceNotFoundException("User not found.");
		}

		List<Risk> allRisksByOrgIdAndCreatedBy = new ArrayList<>();
		List<Date> calculatePeriod = calculatePeriod(period);
		Date startDate = calculatePeriod.get(0);
		Date endDate = calculatePeriod.get(1);
		if (startDate == null || endDate == null) {
			allRisksByOrgIdAndCreatedBy = riskRepository.getAllRisksByOrgIdAndCreatedByNoPage(user.getOrganization().getId(),
					user.getId());

		} else {
			allRisksByOrgIdAndCreatedBy = riskRepository.getAllRisksByOrgIdAndCreatedByDateRangeNoPage(organization.getId(),
					user.getId(), startDate, endDate);

		}

		Map<String, Long> countByStatus = allRisksByOrgIdAndCreatedBy.stream()
				.collect(Collectors.groupingBy(Risk::getRiskStatus, Collectors.counting()));

		// For the response list, use pageable to limit
		List<Risk> topRisks = allRisksByOrgIdAndCreatedBy.stream().limit(pageable.getPageSize()).collect(Collectors.toList());
		List<List<CustomResponse>> responseList = toCustomReponse(topRisks);

		return new BasicDashboardResponse(countByStatus, responseList);
	}

	@Override
	public OrgAdminDashboardDto getAdminDashboardData(String period, Pageable pageable) throws ResourceNotFoundException {

		Organization organization = OrganizationContext.getOrganization();

		if (organization == null) {
			throw new ResourceNotFoundException("No organization found.");
		}

		List<Date> calculatePeriod = calculatePeriod(period);
		Date startDate = calculatePeriod.get(0);
		Date endDate = calculatePeriod.get(1);

		List<Risk> allRisksByOrgId = new ArrayList<>();
		Long totalBranchesByOrg = 0l;
		long totalUsersByOrg = 0;
		Long totalDepartments = 0l;
		Long totalCompanies = 0l;
		if (startDate == null || endDate == null) {
			totalCompanies = companyRepository.totalCompanies(organization.getId());

			totalDepartments = departmentRepostory.totalDepartments(organization.getId());

			totalBranchesByOrg = branchRepository.totalBranchesByOrg(organization.getId());

			totalUsersByOrg = userRepository.totalUsersByOrg(organization.getId());

			allRisksByOrgId = riskRepository.getAllRisksByOrgIdNoPage(organization.getId());
		} else {
			totalCompanies = companyRepository.totalCompanies(organization.getId(), startDate, endDate);

			totalDepartments = departmentRepostory.totalDepartments(organization.getId(), startDate, endDate);

			totalBranchesByOrg = branchRepository.totalBranchesByOrg(organization.getId(), startDate, endDate);

			totalUsersByOrg = userRepository.totalUsersByOrg(organization.getId(), startDate, endDate);

			allRisksByOrgId = riskRepository.getAllRisksByOrgIdDateRangeNoPage(organization.getId(), startDate, endDate);
		}

		Map<String, Long> riskCountByStatus = allRisksByOrgId.stream()
				.collect(Collectors.groupingBy(Risk::getRiskStatus, Collectors.counting()));

		List<List<CustomResponse>> customReponse = toCustomReponse(allRisksByOrgId.stream().limit(pageable.getPageSize()).collect(Collectors.toList()));

		OrgAdminDashboardDto dashboardData = new OrgAdminDashboardDto();
		dashboardData.setTotalCompanies(totalCompanies);
		dashboardData.setTotalActiveUsers(totalUsersByOrg);
		dashboardData.setTotalBranches(totalBranchesByOrg);
		dashboardData.setTotalDepartments(totalDepartments);
		dashboardData.setCountByStatus(riskCountByStatus);
		dashboardData.setTopRisks(customReponse);
		return dashboardData;
	}

	@Override
	public CompanyAdminDashboardDto getCompanyAdminDashboardData(String period, Pageable pageable) throws ResourceNotFoundException {

		Organization organization = OrganizationContext.getOrganization();
		Company company = CompanyContext.getCompany();

		if (company == null) {
			throw new ResourceNotFoundException("No company found.");
		}

		List<Date> calculatePeriod = calculatePeriod(period);
		Date startDate = calculatePeriod.get(0);
		Date endDate = calculatePeriod.get(1);

		List<Branch> branches = new ArrayList<>();
		List<Risk> allRisksByCompany = new ArrayList<>();

		if (startDate == null || endDate == null) {
			branches = branchRepository.getBranchesByCompany(company.getId(), organization.getId());
			allRisksByCompany = riskRepository.getAllRisksByCompanyNoPage(company.getId());
		} else {
			branches = branchRepository.getBranchesByCompany(company.getId(), organization.getId(), startDate, endDate);
			allRisksByCompany = riskRepository.getAllRisksByCompanyDateRangeNoPage(company.getId(), startDate, endDate);
		}

		Long totalDepartments = branches.stream().flatMap(branch -> branch.getDepartments().stream()).count();

		List<List<CustomResponse>> customReponse = toCustomReponse(allRisksByCompany.stream().limit(pageable.getPageSize()).collect(Collectors.toList()));

		long totalSubRisks = allRisksByCompany.stream().flatMap(r -> r.getSubRisk().stream()).count();
		CompanyAdminDashboardDto dashboardData = new CompanyAdminDashboardDto();
		dashboardData.setTotalBranches(branches.size());
		dashboardData.setTotalDepartments(totalDepartments);
		dashboardData.setTotalRisks(allRisksByCompany.size());
		dashboardData.setTotalSubRisks(totalSubRisks);
		dashboardData.setTopRisks(customReponse);
		return dashboardData;
	}

	List<List<CustomResponse>> toCustomReponse(List<Risk> risks) throws ResourceNotFoundException {
		List<Risk> topRisk = risks.stream().sorted(Comparator.comparing(Risk::getCreatedAt)).limit(13)
				.collect(Collectors.toList());

		List<List<CustomResponse>> responseList = new ArrayList<>();
		for (Risk risk : topRisk) {
			List<CustomResponse> customResponse = customResponseMapper.map("risk", 1l, new RiskResponse(risk), true);
			responseList.add(customResponse);
		}

		return responseList;
	}

	private List<Date> calculatePeriod(String period) {
		LocalDate now = LocalDate.now();
		LocalDate startDate;
		LocalDate endDate;
		List<Date> periodDates = new ArrayList<>();

		switch (period) {
		case "today":
			startDate = now;
			endDate = now;
			break;
		case "thisMonth":
			startDate = now.withDayOfMonth(1);
			endDate = now.withDayOfMonth(now.lengthOfMonth());
			break;
		case "month":
			startDate = now.withDayOfMonth(1);
			endDate = now.withDayOfMonth(now.lengthOfMonth());
			break;
		case "thisWeek":
			startDate = now.with(DayOfWeek.MONDAY);
			endDate = now.with(DayOfWeek.SUNDAY);
			break;
		case "week":
			startDate = now.with(DayOfWeek.MONDAY);
			endDate = now.with(DayOfWeek.SUNDAY);
			break;
		case "thisYear":
			startDate = now.withMonth(1);
			endDate = now.withMonth(12);
			break;
		case "year":
			startDate = now.withMonth(1);
			endDate = now.withMonth(12);
			break;
		default:
			startDate = null;
			endDate = null;
			break;
		}

		Date startDateInDate = Date.from(startDate.atStartOfDay(ZoneId.systemDefault()).toInstant());
		Date endtDateInDate = Date.from(endDate.atStartOfDay(ZoneId.systemDefault()).toInstant());

		periodDates.add(startDateInDate);
		periodDates.add(endtDateInDate);

		return periodDates;

	}

}
