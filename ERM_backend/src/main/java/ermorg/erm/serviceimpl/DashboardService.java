package ermorg.erm.serviceimpl;

import java.math.BigDecimal;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import ermorg.erm.constant.ErmDashboardAccessScope;
import ermorg.erm.constant.ErmDashboardPeriodType;
import ermorg.erm.dto.response.BasicDashboardResponse;
import ermorg.erm.dto.response.CompanyAdminDashboardDto;
import ermorg.erm.dto.response.CustomResponse;
import ermorg.erm.dto.response.ErmDashboardCardCounts;
import ermorg.erm.dto.response.ErmDashboardSummaryResponse;
import ermorg.erm.dto.response.ErmHierarchyBreakdown;
import ermorg.erm.dto.response.ErmBranchRatingGroup;
import ermorg.erm.dto.response.ErmCategoryBranchGroup;
import ermorg.erm.dto.response.ErmCompanyCategoryGroup;
import ermorg.erm.dto.response.ErmFunctionRatingGroup;
import ermorg.erm.dto.response.ErmMaturitySummaryGroup;
import ermorg.erm.dto.response.ErmOwnerRatingGroup;
import ermorg.erm.dto.response.ErmRatingHierarchyGroup;
import ermorg.erm.dto.response.NamedCount;
import ermorg.erm.dto.response.OrgAdminDashboardDto;
import ermorg.erm.dto.response.RiskResponse;
import ermorg.erm.exception.ResourceNotFoundException;
import ermorg.erm.model.Branch;
import ermorg.erm.model.Company;
import ermorg.erm.model.Department;
import ermorg.erm.model.ERMMaturityAssessment;
import ermorg.erm.model.Organization;
import ermorg.erm.model.Risk;
import ermorg.erm.model.RiskAssessment;
import ermorg.erm.model.RiskReview;
import ermorg.erm.model.User;
import ermorg.erm.model.UserDetail;
import ermorg.erm.repository.BranchRepository;
import ermorg.erm.repository.CompanyRepository;
import ermorg.erm.repository.ErmMaturityRepository;
import ermorg.erm.repository.KpaKpiReviewRepository;
import ermorg.erm.repository.RiskRepository;
import ermorg.erm.repository.RiskReviewRepository;
import ermorg.erm.repository.UserRepository;
import ermorg.erm.service.DepartmentRepository;
import ermorg.erm.service.IDashboardService;
import ermorg.erm.util.ErmDashboardPeriodBounds;
import ermorg.erm.util.ErmDashboardRoleResolver;
import ermorg.erm.util.ErmMaturityGroupingUtil;
import ermorg.erm.util.CompanyContext;
import ermorg.erm.util.OrganizationContext;
import ermorg.erm.util.UserContext;
import ermorg.erm.util.mapper.CustomResponseMapper;

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

	@Autowired
	private ErmMaturityRepository ermMaturityRepository;

	@Autowired
	private RiskReviewRepository riskReviewRepository;

	@Autowired
	private KpaKpiReviewRepository kpaKpiReviewRepository;

	@Autowired
	private RiskRegisterService riskRegisterService;

	@Override
	public BasicDashboardResponse getBasicDashboardData(String period, Pageable pageable)
			throws ResourceNotFoundException {

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
			allRisksByOrgIdAndCreatedBy = riskRepository
					.getAllRisksByOrgIdAndCreatedByNoPage(user.getOrganization().getId(), user.getId());

		} else {
			allRisksByOrgIdAndCreatedBy = riskRepository.getAllRisksByOrgIdAndCreatedByDateRangeNoPage(
					organization.getId(), user.getId(), startDate, endDate);

		}

		Map<String, Long> countByStatus = allRisksByOrgIdAndCreatedBy.stream()
				.collect(Collectors.groupingBy(Risk::getRiskStatus, Collectors.counting()));

		// For the response list, use pageable to limit
		List<Risk> topRisks = allRisksByOrgIdAndCreatedBy.stream().limit(pageable.getPageSize())
				.collect(Collectors.toList());
		List<List<CustomResponse>> responseList = toCustomReponse(topRisks);

		return new BasicDashboardResponse(countByStatus, responseList);
	}

	@Override
	public OrgAdminDashboardDto getAdminDashboardData(String period, Pageable pageable)
			throws ResourceNotFoundException {

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

			allRisksByOrgId = riskRepository.getAllRisksByOrgIdDateRangeNoPage(organization.getId(), startDate,
					endDate);
		}

		Map<String, Long> riskCountByStatus = allRisksByOrgId.stream()
				.collect(Collectors.groupingBy(Risk::getRiskStatus, Collectors.counting()));

		List<List<CustomResponse>> customReponse = toCustomReponse(
				allRisksByOrgId.stream().limit(pageable.getPageSize()).collect(Collectors.toList()));

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
	public CompanyAdminDashboardDto getCompanyAdminDashboardData(String period, Pageable pageable)
			throws ResourceNotFoundException {

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

		List<List<CustomResponse>> customReponse = toCustomReponse(
				allRisksByCompany.stream().limit(pageable.getPageSize()).collect(Collectors.toList()));

		long totalSubRisks = allRisksByCompany.stream()
				.flatMap(r -> r.getSubRisk() != null ? r.getSubRisk().stream() : Stream.empty())
				.count();
		CompanyAdminDashboardDto dashboardData = new CompanyAdminDashboardDto();
		dashboardData.setTotalBranches(branches.size());
		dashboardData.setTotalDepartments(totalDepartments);
		dashboardData.setTotalRisks(allRisksByCompany.size());
		dashboardData.setTotalSubRisks(totalSubRisks);
		dashboardData.setTopRisks(customReponse);
		return dashboardData;
	}

	@Override
	@Transactional(readOnly = true)
	public ErmDashboardSummaryResponse getErmDashboardSummary(int year, ErmDashboardPeriodType periodType, Long companyId,
			Long branchId, Long functionId, int page, int size) throws ResourceNotFoundException {

		ErmDashboardData dashboardData = loadErmDashboardData(year, periodType, companyId, branchId, functionId);
		Organization organization = dashboardData.organization();
		ErmDashboardAccessScope scope = dashboardData.scope();
		Long scopeCompanyId = dashboardData.scopeCompanyId();
		Long scopeCreatorUserId = dashboardData.scopeCreatorUserId();
		boolean applyBranchDepartmentScope = dashboardData.applyBranchDepartmentScope();
		List<Long> scopeDepartmentIds = dashboardData.scopeDepartmentIds();
		ErmDashboardPeriodBounds bounds = dashboardData.bounds();
		List<Risk> risks = dashboardData.risks();

		ErmDashboardSummaryResponse response = new ErmDashboardSummaryResponse();
		response.setTotalRisks(risks.size());

		Map<String, Long> byCategory = risks.stream()
				.collect(Collectors.groupingBy(DashboardService::riskRegisterTypeKey, Collectors.counting()));
		response.setByCategory(sortedCounts(byCategory, Map.of()));

		response.setByCompany(buildByCompanyFromRiskReviews(organization.getId(), risks, bounds));

		Map<String, String> companyLabels = resolveCompanyLabels(risks);
		Map<String, String> functionLabels = resolveDepartmentLabels(risks);
		Map<String, String> branchLabels = resolveBranchLabels(risks);
		companyLabels.put("NONE", "Unassigned");
		functionLabels.put("NONE", "Unassigned");
		branchLabels.put("NONE", "Unassigned");

		Map<String, String> ownerLabels = resolveOwnerLabels(risks);
		ownerLabels.put("NONE", "Unassigned");

		response.setHierarchy(buildHierarchyBreakdown(risks, companyLabels, functionLabels, branchLabels));
		response.setByBranchRating(buildByBranchRating(risks, branchLabels));
		response.setByFunctionRating(buildByFunctionRating(risks, functionLabels));
		response.setByCategoryBranch(buildByCategoryBranch(risks, branchLabels));
		response.setByOwnerRating(buildByOwnerRating(risks, ownerLabels));

		Map<String, Long> byPriority = risks.stream()
				.flatMap(this::streamAssessments)
				.collect(Collectors.groupingBy(
						a -> assessmentBucket(a, RiskAssessment::getRiskPriority),
						Collectors.counting()));
		withoutUnassessed(byPriority);
		response.setByPriority(sortedCounts(byPriority, Map.of()));

		Map<String, Long> byTreatment = risks.stream()
				.flatMap(this::streamAssessments)
				.collect(Collectors.groupingBy(
						a -> assessmentBucket(a, RiskAssessment::getRiskTreatmentStrategy),
						Collectors.counting()));
		withoutUnassessed(byTreatment);
		response.setByTreatmentStrategy(sortedCounts(byTreatment, Map.of()));

		Map<String, Long> byImpact = risks.stream()
				.flatMap(this::streamAssessments)
				.collect(Collectors.groupingBy(
						a -> assessmentBucket(a, RiskAssessment::getGrossImpactScore),
						Collectors.counting()));
		withoutUnassessed(byImpact);
		response.setByImpact(sortedCounts(byImpact, Map.of()));

		Map<String, Long> byRating = risks.stream()
				.flatMap(this::streamAssessments)
				.collect(Collectors.groupingBy(
						a -> assessmentBucket(a, RiskAssessment::getRiskRating),
						Collectors.counting()));
		withoutUnassessed(byRating);
		response.setByRating(sortedCounts(byRating, Map.of()));

		Map<String, Long> byAnalysisType = risks.stream()
				.flatMap(this::streamAssessments)
				.collect(Collectors.groupingBy(DashboardService::normalizedAnalysisType, Collectors.counting()));
		response.setByAnalysisType(sortedCounts(byAnalysisType, Map.of()));

		Map<String, Long> byFinancialExposure = risks.stream()
				.collect(Collectors.groupingBy(this::financialExposureKey, Collectors.counting()));
		response.setByFinancialExposure(sortedCounts(byFinancialExposure, Map.of()));

		Map<String, Long> bySource = risks.stream()
				.collect(Collectors.groupingBy(r -> unknownIfBlank(r.getRiskSource()), Collectors.counting()));
		response.setBySource(sortedCounts(bySource, Map.of()));

		if (scope == ErmDashboardAccessScope.ORGANIZATION_WIDE) {
			response.setByRatingHierarchy(buildByRatingHierarchy(risks, companyLabels, functionLabels, branchLabels));
			response.setByCompanyCategory(buildByCompanyCategory(risks, companyLabels));
		} else {
			response.setByRatingHierarchy(null);
			response.setByCompanyCategory(null);
		}

		boolean scopeByDepartment = applyBranchDepartmentScope && !scopeDepartmentIds.isEmpty();
		List<ERMMaturityAssessment> scopedMaturities = loadScopedMaturityAssessments(organization, bounds,
				scopeCompanyId, functionId, applyBranchDepartmentScope, scopeByDepartment, scopeDepartmentIds);
		response.setCardCounts(buildCardCounts(organization.getId(), risks, bounds, scopeCompanyId,
				scopeCreatorUserId, scopedMaturities));
		populateErmMaturitySummary(response, scopedMaturities);
		response.setRiskRegister(riskRegisterService.buildPage(organization.getId(), risks,
				bounds.getStartInclusive(), bounds.getEndInclusive(), functionId, scopeByDepartment,
				scopeDepartmentIds, page, size));

		return response;
	}

	@Override
	@Transactional(readOnly = true)
	public byte[] exportErmRiskRegisterCsv(int year, ErmDashboardPeriodType periodType, Long companyId, Long branchId,
			Long functionId) throws ResourceNotFoundException {

		ErmDashboardData data = loadErmDashboardData(year, periodType, companyId, branchId, functionId);
		boolean scopeByDepartment = data.applyBranchDepartmentScope() && !data.scopeDepartmentIds().isEmpty();
		return riskRegisterService.exportCsv(data.organization().getId(), data.risks(),
				data.bounds().getStartInclusive(), data.bounds().getEndInclusive(), functionId, scopeByDepartment,
				data.scopeDepartmentIds());
	}

	private ErmDashboardData loadErmDashboardData(int year, ErmDashboardPeriodType periodType, Long companyId,
			Long branchId, Long functionId) throws ResourceNotFoundException {

		Organization organization = OrganizationContext.getOrganization();
		if (organization == null) {
			throw new ResourceNotFoundException("No organization found.");
		}

		User ctxUser = UserContext.getUser();
		if (ctxUser == null) {
			throw new ResourceNotFoundException("User not found.");
		}

		User user = userRepository.findActiveByIdWithRolesAndCompany(ctxUser.getId())
				.orElseThrow(() -> new ResourceNotFoundException("User not found."));
		ErmDashboardAccessScope scope = ErmDashboardRoleResolver.resolveScope(user);

		Long scopeCompanyId = null;
		Long scopeCreatorUserId = null;
		boolean applyBranchDepartmentScope = false;
		List<Long> scopeBranchIds = Collections.emptyList();
		List<Long> scopeDepartmentIds = Collections.emptyList();
		switch (scope) {
		case ORGANIZATION_WIDE:
			scopeCompanyId = companyId;
			break;
		case COMPANY_SCOPED:
			if (user.getCompany() == null) {
				throw new ResourceNotFoundException("No company for user.");
			}
			scopeCompanyId = user.getCompany().getId();
			break;
		case ADVANCED_USER_SCOPED:
			if (user.getCompany() == null) {
				throw new ResourceNotFoundException("No company for user.");
			}
			scopeCompanyId = user.getCompany().getId();
			scopeBranchIds = ErmDashboardRoleResolver.resolveAssignedBranchIds(user);
			scopeDepartmentIds = ErmDashboardRoleResolver.resolveAssignedDepartmentIds(user);
			applyBranchDepartmentScope = true;
			break;
		case CREATOR_ONLY:
			scopeCreatorUserId = user.getId();
			break;
		}

		ErmDashboardPeriodBounds bounds = ErmDashboardPeriodBounds.forYearAndPeriod(year, periodType,
				ZoneId.systemDefault());
		List<Risk> risks;
		if (applyBranchDepartmentScope && scopeBranchIds.isEmpty() && scopeDepartmentIds.isEmpty()) {
			risks = Collections.emptyList();
		} else {
			boolean scopeByBranch = applyBranchDepartmentScope && !scopeBranchIds.isEmpty();
			boolean scopeByDepartment = applyBranchDepartmentScope && !scopeDepartmentIds.isEmpty();
			List<Long> queryBranchIds = scopeByBranch ? scopeBranchIds : List.of(-1L);
			List<Long> queryDepartmentIds = scopeByDepartment ? scopeDepartmentIds : List.of(-1L);
			risks = riskRepository.findRisksForErmDashboard(organization.getId(), bounds.getStartInclusive(),
					bounds.getEndInclusive(), scopeCompanyId, scopeCreatorUserId, branchId, functionId,
					applyBranchDepartmentScope, scopeByBranch, scopeByDepartment, queryBranchIds, queryDepartmentIds);
		}

		return new ErmDashboardData(organization, scope, scopeCompanyId, scopeCreatorUserId,
				applyBranchDepartmentScope, scopeDepartmentIds, bounds, risks);
	}

	private record ErmDashboardData(Organization organization, ErmDashboardAccessScope scope, Long scopeCompanyId,
			Long scopeCreatorUserId, boolean applyBranchDepartmentScope, List<Long> scopeDepartmentIds,
			ErmDashboardPeriodBounds bounds, List<Risk> risks) {
	}

	private ErmDashboardCardCounts buildCardCounts(Long organizationId, List<Risk> risks,
			ErmDashboardPeriodBounds bounds, Long scopeCompanyId, Long scopeCreatorUserId,
			List<ERMMaturityAssessment> scopedMaturities) {

		ErmDashboardCardCounts cardCounts = new ErmDashboardCardCounts();
		cardCounts.setTotalRiskCount(risks.size());

		if (risks.isEmpty()) {
			cardCounts.setTotalRiskAppetite(0L);
		} else {
			List<Long> riskIds = risks.stream().map(Risk::getId).toList();
			cardCounts.setTotalRiskAppetite(riskReviewRepository.countForErmDashboardByRiskIds(organizationId, riskIds,
					bounds.getStartInclusive(), bounds.getEndInclusive()));
		}

		cardCounts.setTotalRiskTolerance(kpaKpiReviewRepository.countForErmDashboard(organizationId,
				bounds.getStartInclusive(), bounds.getEndInclusive(), scopeCompanyId, scopeCreatorUserId));

		Date now = new Date();
		long overdue = scopedMaturities.stream()
				.filter(m -> m.getDueDate() != null && m.getDueDate().before(now))
				.count();
		cardCounts.setTotalOverdue(overdue);
		return cardCounts;
	}

	private List<ERMMaturityAssessment> loadScopedMaturityAssessments(Organization organization,
			ErmDashboardPeriodBounds bounds, Long scopeCompanyId, Long functionId,
			boolean applyBranchDepartmentScope, boolean scopeByDepartment, List<Long> scopeDepartmentIds) {

		List<ERMMaturityAssessment> assessments = ErmMaturityGroupingUtil
				.dedupeById(ermMaturityRepository.findForErmDashboard(organization.getId(), bounds.getStartInclusive(),
						bounds.getEndInclusive(), scopeCompanyId, functionId));

		Map<String, List<ERMMaturityAssessment>> byGroup = ErmMaturityGroupingUtil.groupByErmMaturityId(assessments);
		boolean functionIdIsSpecific = functionId != null && functionId != 0;
		List<ERMMaturityAssessment> scoped = new ArrayList<>();

		byGroup.forEach((ermMaturityId, group) -> {
			List<Long> activeDeptIds = ErmMaturityGroupingUtil
					.activeDepartmentIds(ErmMaturityGroupingUtil.firstRowDepartmentIds(group));
			if (!passesMaturityDepartmentScope(activeDeptIds, applyBranchDepartmentScope, scopeByDepartment,
					scopeDepartmentIds)) {
				return;
			}
			if (functionIdIsSpecific && !activeDeptIds.contains(functionId)) {
				return;
			}
			scoped.addAll(group);
		});
		return scoped;
	}

	private void populateErmMaturitySummary(ErmDashboardSummaryResponse response,
			List<ERMMaturityAssessment> scopedAssessments) {

		Map<String, List<ERMMaturityAssessment>> byGroup = ErmMaturityGroupingUtil
				.groupByErmMaturityId(scopedAssessments);

		Set<Long> functionDeptIds = new HashSet<>();
		for (List<ERMMaturityAssessment> group : byGroup.values()) {
			List<Long> activeDeptIds = ErmMaturityGroupingUtil
					.activeDepartmentIds(ErmMaturityGroupingUtil.firstRowDepartmentIds(group));
			if (!ErmMaturityGroupingUtil.isCompanyWiseMaturity(activeDeptIds)) {
				functionDeptIds.addAll(activeDeptIds);
			}
		}
		Map<String, String> departmentLabels = resolveDepartmentLabelsByIds(functionDeptIds);

		List<ErmMaturitySummaryGroup> companyWise = new ArrayList<>();
		List<ErmMaturitySummaryGroup> functionWise = new ArrayList<>();

		byGroup.forEach((ermMaturityId, group) -> {
			List<Long> activeDeptIds = ErmMaturityGroupingUtil
					.activeDepartmentIds(ErmMaturityGroupingUtil.firstRowDepartmentIds(group));
			ErmMaturitySummaryGroup summary = buildMaturitySummaryGroup(ermMaturityId, group, activeDeptIds,
					departmentLabels);
			if (ErmMaturityGroupingUtil.isCompanyWiseMaturity(activeDeptIds)) {
				companyWise.add(summary);
			} else {
				functionWise.add(summary);
			}
		});

		response.setErmMaturityCompanyWise(companyWise);
		response.setErmMaturityFunctionWise(functionWise);
	}

	private ErmMaturitySummaryGroup buildMaturitySummaryGroup(String ermMaturityId,
			List<ERMMaturityAssessment> group, List<Long> activeDeptIds, Map<String, String> departmentLabels) {

		ERMMaturityAssessment first = group.get(0);
		BigDecimal totalWeightageScore = ErmMaturityGroupingUtil.totalScore(group);
		String overallMaturityLevel = ErmMaturityGroupingUtil.maturityLabel(totalWeightageScore);

		ErmMaturitySummaryGroup summary = new ErmMaturitySummaryGroup();
		summary.setErmMaturityId(ermMaturityId);
		summary.setTotalWeightageScore(totalWeightageScore);
		summary.setOverallMaturityLevel(overallMaturityLevel);
		summary.setDepartmentIds(new ArrayList<>(activeDeptIds));

		Company company = first.getCompany();
		if (company != null) {
			summary.setCompanyId(company.getId());
		}

		summary.setDisplayLabel(
				ErmMaturityGroupingUtil.resolveDisplayLabel(ermMaturityId, activeDeptIds, company, departmentLabels));
		return summary;
	}

	private static boolean passesMaturityDepartmentScope(List<Long> activeDeptIds, boolean applyBranchDepartmentScope,
			boolean scopeByDepartment, List<Long> scopeDepartmentIds) {
		if (!applyBranchDepartmentScope || !scopeByDepartment) {
			return true;
		}
		if (ErmMaturityGroupingUtil.isCompanyWiseMaturity(activeDeptIds)) {
			return true;
		}
		return activeDeptIds.stream().anyMatch(scopeDepartmentIds::contains);
	}

	private Map<String, String> resolveDepartmentLabelsByIds(Set<Long> ids) {
		Map<String, String> labels = new HashMap<>();
		if (!ids.isEmpty()) {
			for (Department d : departmentRepostory.findAllById(ids)) {
				labels.put(String.valueOf(d.getId()), d.getName());
			}
		}
		return labels;
	}

	private ErmHierarchyBreakdown buildHierarchyBreakdown(List<Risk> risks, Map<String, String> companyLabels,
			Map<String, String> functionLabels, Map<String, String> branchLabels) {
		Map<String, Long> byCompany = risks.stream().collect(Collectors.groupingBy(
				r -> r.getCompanyId() == null ? "NONE" : String.valueOf(r.getCompanyId()), Collectors.counting()));
		Map<String, Long> byFunction = risks.stream()
				.collect(Collectors.groupingBy(DashboardService::functionKey, Collectors.counting()));
		Map<String, Long> byBranch = risks.stream()
				.collect(Collectors.groupingBy(DashboardService::branchKey, Collectors.counting()));

		ErmHierarchyBreakdown hierarchy = new ErmHierarchyBreakdown();
		hierarchy.setByCompany(sortedCounts(byCompany, companyLabels));
		hierarchy.setByFunction(sortedCounts(byFunction, functionLabels));
		hierarchy.setByBranch(sortedCounts(byBranch, branchLabels));
		return hierarchy;
	}

	private List<ErmRatingHierarchyGroup> buildByRatingHierarchy(List<Risk> risks, Map<String, String> companyLabels,
			Map<String, String> functionLabels, Map<String, String> branchLabels) {
		Map<String, List<Risk>> byRatingKey = risks.stream()
				.flatMap(this::streamRiskAssessmentPairs)
				.collect(Collectors.groupingBy(
						p -> assessmentBucket(p.assessment(), RiskAssessment::getRiskRating),
						Collectors.mapping(RiskAssessmentPair::risk, Collectors.toList())));
		byRatingKey.remove("UNASSESSED");

		return byRatingKey.entrySet().stream().sorted((a, b) -> compareRatingKeys(a.getKey(), b.getKey())).map(entry -> {
			String ratingKey = entry.getKey();
			List<Risk> subset = entry.getValue();
			ErmRatingHierarchyGroup group = new ErmRatingHierarchyGroup();
			group.setKey(ratingKey);
			group.setDisplayLabel(ratingKey);
			group.setTotal(subset.size());
			group.setHierarchy(buildHierarchyBreakdown(subset, companyLabels, functionLabels, branchLabels));
			return group;
		}).collect(Collectors.toList());
	}

	private static int compareRatingKeys(String a, String b) {
		return a.compareTo(b);
	}

	private List<ErmCompanyCategoryGroup> buildByCompanyCategory(List<Risk> risks,
			Map<String, String> companyLabels) {
		Map<String, List<Risk>> byCompanyKey = risks.stream().collect(Collectors.groupingBy(DashboardService::companyKey));

		return byCompanyKey.entrySet().stream().sorted((a, b) -> compareCompanyKeys(a.getKey(), b.getKey()))
				.map(entry -> {
					String companyKey = entry.getKey();
					List<Risk> subset = entry.getValue();
					Map<String, Long> byCategory = subset.stream()
							.collect(Collectors.groupingBy(DashboardService::riskRegisterTypeKey, Collectors.counting()));

					ErmCompanyCategoryGroup group = new ErmCompanyCategoryGroup();
					group.setKey(companyKey);
					group.setDisplayLabel(companyLabels.getOrDefault(companyKey, companyKey));
					group.setTotal(subset.size());
					group.setByCategory(sortedCounts(byCategory, Map.of()));
					return group;
				}).collect(Collectors.toList());
	}

	private static String companyKey(Risk r) {
		return r.getCompanyId() == null ? "NONE" : String.valueOf(r.getCompanyId());
	}

	private static String categoryKey(Risk r) {
		return r.getCategory() == null ? "UNKNOWN" : r.getCategory().name();
	}

	private static String riskRegisterTypeKey(Risk r) {
		return unknownIfBlank(r.getRiskRegisterType());
	}

	private List<NamedCount> buildByCompanyFromRiskReviews(Long organizationId, List<Risk> risks,
			ErmDashboardPeriodBounds bounds) {
		if (risks.isEmpty()) {
			return List.of();
		}
		List<Long> riskIds = risks.stream().map(Risk::getId).toList();
		List<RiskReview> reviews = riskReviewRepository.findForRiskRegister(organizationId, riskIds,
				bounds.getStartInclusive(), bounds.getEndInclusive());
		Map<String, Long> byResidualRating = reviews.stream()
				.map(RiskReview::getResidualRiskRating)
				.filter(Objects::nonNull)
				.map(String::trim)
				.filter(v -> !v.isBlank())
				.filter(v -> !"UNASSESSED".equals(v))
				.collect(Collectors.groupingBy(v -> v, Collectors.counting()));
		return sortedCounts(byResidualRating, Map.of());
	}

	private static void withoutUnassessed(Map<String, Long> counts) {
		counts.remove("UNASSESSED");
	}

	private static int compareCategoryKeys(String a, String b) {
		if ("UNKNOWN".equals(a)) {
			return 1;
		}
		if ("UNKNOWN".equals(b)) {
			return -1;
		}
		return a.compareTo(b);
	}

	private static int compareCompanyKeys(String a, String b) {
		if ("NONE".equals(a)) {
			return 1;
		}
		if ("NONE".equals(b)) {
			return -1;
		}
		return a.compareTo(b);
	}

	private List<ErmBranchRatingGroup> buildByBranchRating(List<Risk> risks, Map<String, String> branchLabels) {
		Map<String, List<Risk>> byBranchKey = risks.stream().collect(Collectors.groupingBy(DashboardService::branchKey));

		return byBranchKey.entrySet().stream().sorted((a, b) -> compareBranchKeys(a.getKey(), b.getKey()))
				.map(entry -> {
					String key = entry.getKey();
					List<Risk> subset = entry.getValue();
					Map<String, Long> byRating = subset.stream()
							.flatMap(this::streamAssessments)
							.collect(Collectors.groupingBy(
									a -> assessmentBucket(a, RiskAssessment::getRiskRating),
									Collectors.counting()));
					withoutUnassessed(byRating);

					ErmBranchRatingGroup group = new ErmBranchRatingGroup();
					group.setKey(key);
					group.setDisplayLabel(branchLabels.getOrDefault(key, key));
					group.setTotal(byRating.values().stream().mapToLong(Long::longValue).sum());
					group.setByRating(sortedRatingCounts(byRating));
					return group;
				}).collect(Collectors.toList());
	}

	private List<ErmFunctionRatingGroup> buildByFunctionRating(List<Risk> risks, Map<String, String> functionLabels) {
		Map<String, List<Risk>> byFunctionKey = risks.stream()
				.collect(Collectors.groupingBy(DashboardService::functionKey));

		return byFunctionKey.entrySet().stream().sorted((a, b) -> compareFunctionKeys(a.getKey(), b.getKey()))
				.map(entry -> {
					String key = entry.getKey();
					List<Risk> subset = entry.getValue();
					Map<String, Long> byRating = subset.stream()
							.flatMap(this::streamAssessments)
							.collect(Collectors.groupingBy(
									a -> assessmentBucket(a, RiskAssessment::getRiskRating),
									Collectors.counting()));
					withoutUnassessed(byRating);

					ErmFunctionRatingGroup group = new ErmFunctionRatingGroup();
					group.setKey(key);
					group.setDisplayLabel(functionLabels.getOrDefault(key, key));
					group.setTotal(byRating.values().stream().mapToLong(Long::longValue).sum());
					group.setByRating(sortedRatingCounts(byRating));
					return group;
				}).collect(Collectors.toList());
	}

	private List<ErmOwnerRatingGroup> buildByOwnerRating(List<Risk> risks, Map<String, String> ownerLabels) {
		Map<String, List<Risk>> byOwnerKey = risks.stream().collect(Collectors.groupingBy(DashboardService::ownerKey));

		return byOwnerKey.entrySet().stream().sorted((a, b) -> compareOwnerKeys(a.getKey(), b.getKey()))
				.map(entry -> {
					String key = entry.getKey();
					List<Risk> subset = entry.getValue();
					Map<String, Long> byRating = subset.stream()
							.flatMap(this::streamAssessments)
							.collect(Collectors.groupingBy(
									a -> assessmentBucket(a, RiskAssessment::getRiskRating),
									Collectors.counting()));
					withoutUnassessed(byRating);

					ErmOwnerRatingGroup group = new ErmOwnerRatingGroup();
					group.setKey(key);
					group.setDisplayLabel(ownerLabels.getOrDefault(key, key));
					group.setTotal(byRating.values().stream().mapToLong(Long::longValue).sum());
					group.setByRating(sortedRatingCounts(byRating));
					return group;
				}).collect(Collectors.toList());
	}

	private List<ErmCategoryBranchGroup> buildByCategoryBranch(List<Risk> risks, Map<String, String> branchLabels) {
		Map<String, List<Risk>> byCategory = risks.stream().collect(Collectors.groupingBy(DashboardService::categoryKey));

		return byCategory.entrySet().stream().sorted((a, b) -> compareCategoryKeys(a.getKey(), b.getKey()))
				.map(entry -> {
					String key = entry.getKey();
					List<Risk> subset = entry.getValue();
					Map<String, Long> byBranch = subset.stream()
							.collect(Collectors.groupingBy(DashboardService::branchKey, Collectors.counting()));

					ErmCategoryBranchGroup group = new ErmCategoryBranchGroup();
					group.setKey(key);
					group.setDisplayLabel(key);
					group.setTotal(subset.size());
					group.setByBranch(sortedCounts(byBranch, branchLabels));
					return group;
				}).collect(Collectors.toList());
	}

	private static String branchKey(Risk r) {
		return r.getBranchId() == null ? "NONE" : String.valueOf(r.getBranchId());
	}

	private static int compareBranchKeys(String a, String b) {
		if ("NONE".equals(a)) {
			return 1;
		}
		if ("NONE".equals(b)) {
			return -1;
		}
		return a.compareTo(b);
	}

	private static String functionKey(Risk r) {
		return r.getFunction() == null ? "NONE" : String.valueOf(r.getFunction());
	}

	private static int compareFunctionKeys(String a, String b) {
		if ("NONE".equals(a)) {
			return 1;
		}
		if ("NONE".equals(b)) {
			return -1;
		}
		return a.compareTo(b);
	}

	private static String ownerKey(Risk r) {
		return r.getRiskOwner() == null ? "NONE" : String.valueOf(r.getRiskOwner().getId());
	}

	private static int compareOwnerKeys(String a, String b) {
		if ("NONE".equals(a)) {
			return 1;
		}
		if ("NONE".equals(b)) {
			return -1;
		}
		return a.compareTo(b);
	}

	private static List<NamedCount> sortedRatingCounts(Map<String, Long> counts) {
		return counts.entrySet().stream().sorted((a, b) -> compareRatingKeys(a.getKey(), b.getKey())).map(e -> {
			String key = e.getKey();
			return new NamedCount(key, e.getValue(), key);
		}).collect(Collectors.toList());
	}

	private static String assessmentBucket(RiskAssessment a, java.util.function.Function<RiskAssessment, String> field) {
		if (a == null) {
			return "UNASSESSED";
		}
		String v = field.apply(a);
		if (v == null || v.isBlank()) {
			return "UNASSESSED";
		}
		return v.trim();
	}

	private static String normalizedAnalysisType(RiskAssessment a) {
		if (a == null || a.getRiskAnalysisType() == null || a.getRiskAnalysisType().isBlank()) {
			return "UNKNOWN";
		}
		return a.getRiskAnalysisType().trim();
	}

	private String financialExposureKey(Risk r) {
		if (r.getExposure() != null && !r.getExposure().isBlank()) {
			return r.getExposure().trim();
		}
		if (r.getRiskAssessments() != null) {
			for (RiskAssessment a : r.getRiskAssessments()) {
				if (!a.getDeleted() && a.getFinancialImpact() != null && !a.getFinancialImpact().isBlank()) {
					return a.getFinancialImpact().trim();
				}
			}
		}
		return "UNKNOWN";
	}

	private static String unknownIfBlank(String value) {
		if (value == null || value.isBlank()) {
			return "UNKNOWN";
		}
		return value.trim();
	}

	private Map<String, String> resolveCompanyLabels(List<Risk> risks) {
		Set<Long> ids = risks.stream().map(Risk::getCompanyId).filter(Objects::nonNull).collect(Collectors.toSet());
		Map<String, String> labels = new HashMap<>();
		if (!ids.isEmpty()) {
			for (Company c : companyRepository.findAllById(ids)) {
				labels.put(String.valueOf(c.getId()), c.getName());
			}
		}
		return labels;
	}

	private Map<String, String> resolveDepartmentLabels(List<Risk> risks) {
		Set<Long> ids = risks.stream().map(Risk::getFunction).filter(Objects::nonNull).collect(Collectors.toSet());
		Map<String, String> labels = new HashMap<>();
		if (!ids.isEmpty()) {
			for (Department d : departmentRepostory.findAllById(ids)) {
				labels.put(String.valueOf(d.getId()), d.getName());
			}
		}
		return labels;
	}

	private Map<String, String> resolveBranchLabels(List<Risk> risks) {
		Set<Long> ids = risks.stream().map(Risk::getBranchId).filter(Objects::nonNull).collect(Collectors.toSet());
		Map<String, String> labels = new HashMap<>();
		if (!ids.isEmpty()) {
			for (Branch b : branchRepository.findAllById(ids)) {
				labels.put(String.valueOf(b.getId()), b.getName());
			}
		}
		return labels;
	}

	private Map<String, String> resolveOwnerLabels(List<Risk> risks) {
		Map<String, String> labels = new HashMap<>();
		for (Risk risk : risks) {
			User owner = risk.getRiskOwner();
			if (owner == null) {
				continue;
			}
			String key = String.valueOf(owner.getId());
			if (labels.containsKey(key)) {
				continue;
			}
			UserDetail detail = owner.getUserDetail();
			if (detail != null) {
				String name = (detail.getFirstName() + " " + detail.getLastName()).trim();
				if (!name.isBlank()) {
					labels.put(key, name);
					continue;
				}
			}
			String email = owner.getEmail();
			labels.put(key, email != null && !email.isBlank() ? email : key);
		}
		return labels;
	}

	private static List<NamedCount> sortedCounts(Map<String, Long> counts, Map<String, String> labelByKey) {
		return counts.entrySet().stream().sorted(Map.Entry.comparingByKey()).map(e -> {
			String key = e.getKey();
			String label = labelByKey.getOrDefault(key, key);
			return new NamedCount(key, e.getValue(), label);
		}).collect(Collectors.toList());
	}

	List<List<CustomResponse>> toCustomReponse(List<Risk> risks) throws ResourceNotFoundException {
		List<Risk> topRisk = risks.stream().sorted(Comparator.comparing(Risk::getCreatedAt)).limit(13)
				.collect(Collectors.toList());

		List<List<CustomResponse>> responseList = new ArrayList<>();
		for (Risk risk : topRisk) {
			List<CustomResponse> customResponse = customResponseMapper.map("risk", 1l, new RiskResponse(risk), true);
			addAssessmentDashboardFields(customResponse, primaryAssessment(risk));
			responseList.add(customResponse);
		}

		return responseList;
	}

	private void addAssessmentDashboardFields(List<CustomResponse> customResponse, RiskAssessment assessment) {
		addDashboardField(customResponse, "Likelihood", assessment != null ? assessment.getLikelihood() : null);
		addDashboardField(customResponse, "Velocity", assessment != null ? assessment.getVelocity() : null);
		addDashboardField(customResponse, "Gross Impact Score",
				assessment != null ? assessment.getGrossImpactScore() : null);
		addDashboardField(customResponse, "Risk Rating", assessment != null ? assessment.getRiskRating() : null);
	}

	private void addDashboardField(List<CustomResponse> customResponse, String fieldName, String value) {
		String normalized = fieldName.toLowerCase().replaceAll("[^a-z0-9]", "");
		boolean exists = customResponse.stream()
				.map(CustomResponse::getFieldName)
				.filter(Objects::nonNull)
				.map(name -> name.toLowerCase().replaceAll("[^a-z0-9]", ""))
				.anyMatch(normalized::equals);
		if (!exists) {
			CustomResponse response = new CustomResponse();
			response.setFieldName(fieldName);
			response.setFieldType("Text");
			response.setValue(value);
			customResponse.add(response);
		}
	}

	private record RiskAssessmentPair(Risk risk, RiskAssessment assessment) {
	}

	private Stream<RiskAssessment> streamAssessments(Risk risk) {
		if (risk.getRiskAssessments() == null || risk.getRiskAssessments().isEmpty()) {
			return Stream.of((RiskAssessment) null);
		}
		return risk.getRiskAssessments().stream().filter(a -> !a.getDeleted());
	}

	private Stream<RiskAssessmentPair> streamRiskAssessmentPairs(Risk risk) {
		if (risk.getRiskAssessments() == null || risk.getRiskAssessments().isEmpty()) {
			return Stream.of(new RiskAssessmentPair(risk, null));
		}
		return risk.getRiskAssessments().stream()
				.filter(a -> !a.getDeleted())
				.map(a -> new RiskAssessmentPair(risk, a));
	}

	private RiskAssessment primaryAssessment(Risk risk) {
		if (risk.getRiskAssessments() == null) {
			return null;
		}
		return risk.getRiskAssessments().stream().filter(a -> !a.getDeleted()).findFirst().orElse(null);
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
