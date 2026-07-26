package ermorg.erm.serviceimpl;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import ermorg.erm.dto.response.CustomResponse;
import ermorg.erm.dto.response.ErmMaturityResponse;
import ermorg.erm.dto.riskDTO.ErmMaturityDto;
import ermorg.erm.dto.riskDTO.ErmMaturityRequest;
import ermorg.erm.exception.LimitExceedException;
import ermorg.erm.exception.ResourceNotFoundException;
import ermorg.erm.model.Company;
import ermorg.erm.model.Department;
import ermorg.erm.model.ERMMaturityAssessment;
import ermorg.erm.model.Organization;
import ermorg.erm.repository.CompanyRepository;
import ermorg.erm.repository.ErmMaturityRepository;
import ermorg.erm.service.DepartmentRepository;
import ermorg.erm.service.IErmMaturityService;
import ermorg.erm.util.CompanyContext;
import ermorg.erm.util.ErmMaturityGroupingUtil;
import ermorg.erm.util.MaturityLevelResolver;
import ermorg.erm.util.OrganizationContext;
import ermorg.erm.util.mapper.CustomResponseMapper;

@Service
public class MaturityService implements IErmMaturityService {

	@Autowired
	private ErmMaturityRepository ermMaturityRepository;

	@Autowired
	private CompanyRepository companyRepository;

	@Autowired
	private DepartmentRepository departmentRepository;

	@Autowired
	private CustomResponseMapper customResponseMapper;

	@Override
	public ErmMaturityResponse save(ErmMaturityRequest request) throws ResourceNotFoundException, LimitExceedException {

		if (request == null || request.getMaturityRequest() == null || request.getMaturityRequest().isEmpty()) {
			throw new ResourceNotFoundException("Invailed request.");
		}

		Organization organization = OrganizationContext.getOrganization();
		Company company = resolveCompany(request);
		List<ErmMaturityDto> maturityItems = request.getMaturityRequest();

		List<Long> departmentIds = resolveCommonDepartmentIds(maturityItems);
		String ermMaturityId = resolveErmMaturityId(company.getId(), departmentIds);

		long existingCount = ermMaturityRepository.countByOrganizationIdAndErmMaturityId(organization.getId(),
				ermMaturityId);
		if (existingCount >= 9) {
			throw new LimitExceedException(
					"Maximum 9 maturity assessments already exist for group: " + ermMaturityId);
		}

		double totalMarks = maturityItems.stream()
				.mapToDouble(dto -> MaturityLevelResolver.parseMarksAchieved(dto.getMarksAchieved())).sum();
		String maturityLabel = MaturityLevelResolver.resolveMaturityLabel(totalMarks);

		List<Long> maturityIds = maturityItems.stream().map(ErmMaturityDto::getMaturityId).collect(Collectors.toList());
		List<ERMMaturityAssessment> ermMaturityList = ermMaturityRepository
				.getAllByOrgAndMaturityIds(organization.getId(), maturityIds);

		for (ErmMaturityDto req : maturityItems) {
			ermMaturityList.stream()
					.filter(m -> Objects.equals(m.getId(), req.getMaturityId()) && req.getMaturityId() != 0)
					.findFirst()
					.ifPresentOrElse(
							m -> fillFields(m, req, organization, company, departmentIds, ermMaturityId, maturityLabel),
							() -> {
								ERMMaturityAssessment m = new ERMMaturityAssessment();
								fillFields(m, req, organization, company, departmentIds, ermMaturityId, maturityLabel);
								ermMaturityList.add(m);
							});
		}

		List<ERMMaturityAssessment> saved = ermMaturityRepository.saveAll(ermMaturityList);
		return saved.isEmpty() ? null : new ErmMaturityResponse(saved.get(0));
	}

	private Company resolveCompany(ErmMaturityRequest request) throws ResourceNotFoundException {
		if (request.getCompanyId() != null && request.getCompanyId() != 0) {
			return companyRepository.findById(request.getCompanyId())
					.filter(c -> !c.getDeleted())
					.orElseThrow(() -> new ResourceNotFoundException("Company not found."));
		}
		Company company = CompanyContext.getCompany();
		if (company == null) {
			throw new ResourceNotFoundException("Company not found.");
		}
		return company;
	}

	private List<Long> resolveCommonDepartmentIds(List<ErmMaturityDto> items) {
		List<Long> reference = normalizeDepartmentIds(items.get(0).getDepartmentIds());
		for (int i = 1; i < items.size(); i++) {
			if (!reference.equals(normalizeDepartmentIds(items.get(i).getDepartmentIds()))) {
				throw new IllegalArgumentException("departmentIds must be identical on all maturity request items.");
			}
		}
		return new ArrayList<>(reference);
	}

	private List<Long> normalizeDepartmentIds(List<Long> ids) {
		if (ids == null) {
			return List.of();
		}
		return ids.stream().filter(Objects::nonNull).sorted().toList();
	}

	private String resolveErmMaturityId(long companyId, List<Long> departmentIds) {
		List<Long> activeDeptIds = ErmMaturityGroupingUtil.activeDepartmentIds(departmentIds);

		if (activeDeptIds.isEmpty()) {
			return String.valueOf(companyId);
		}
		return companyId + "_" + activeDeptIds.stream().map(String::valueOf).collect(Collectors.joining("_"));
	}

	private void fillFields(ERMMaturityAssessment m, ErmMaturityDto req, Organization organization, Company company,
			List<Long> departmentIds, String ermMaturityId, String maturityLabel) {
		m.setDeleted(false);
		m.setKeyAssessmentParameters(req.getKeyAssessmentParameters());
		m.setAssessedBy(req.getAssessedBy());
		m.setAssessmentAreaId(req.getAssessmentArea());
		m.setDueDate(req.getDueDate());
		m.setActualDate(req.getActualDate());
		m.setLastAssessmentDate(req.getLastAssessmentDate());
		m.setMarksAchieved(req.getMarksAchieved());
		m.setNextAssessmentDate(req.getNextAssessmentDate());
		m.setOverallMaturityLevel(maturityLabel);
		m.setStatus(req.getStatus());
		if (req.getWeightageScore() != null && !req.getWeightageScore().isBlank()) {
			m.setWeightageScore(BigDecimal.valueOf(Double.parseDouble(req.getWeightageScore())));
		}
		m.setOrganization(organization);
		m.setAssessmentAreaName(req.getAssessmentAreaName());
		m.setCompany(company);
		m.setErmMaturityId(ermMaturityId);
		m.setDepartmentIds(departmentIds != null ? new ArrayList<>(departmentIds) : new ArrayList<>());
	}

	@Override
	public ErmMaturityResponse get(Long maturityId) throws ResourceNotFoundException {
		Organization organization = OrganizationContext.getOrganization();
		ERMMaturityAssessment ermMaturity = ermMaturityRepository.getByOrg(organization.getId(), maturityId);

		if (ermMaturity == null) {
			throw new ResourceNotFoundException("No record found.");
		}

		return new ErmMaturityResponse(ermMaturity);
	}

	@Override
	public List<CustomResponse> getView(Long maturityId) throws ResourceNotFoundException {
		Organization organization = OrganizationContext.getOrganization();
		ERMMaturityAssessment ermMaturity = ermMaturityRepository.getByOrg(organization.getId(), maturityId);

		if (ermMaturity == null) {
			throw new ResourceNotFoundException("No record found.");
		}

		List<CustomResponse> customResponse = customResponseMapper.map("ermMaturity", 1l,
				new ErmMaturityResponse(ermMaturity), false);

		return customResponse;
	}

	@Override
	public Page<List<CustomResponse>> getAll(Pageable pageable) throws ResourceNotFoundException {

		Organization organization = OrganizationContext.getOrganization();

		List<ERMMaturityAssessment> assessments = ErmMaturityGroupingUtil
				.dedupeById(ermMaturityRepository.findAllGroupedByOrg(organization.getId()));

		LinkedHashMap<String, List<ERMMaturityAssessment>> byGroup = ErmMaturityGroupingUtil
				.groupByErmMaturityId(assessments);

		if (byGroup.isEmpty()) {
			throw new ResourceNotFoundException("No record found.");
		}

		Map<String, String> departmentLabels = resolveDepartmentLabels(byGroup);

		List<Map.Entry<String, List<ERMMaturityAssessment>>> groups = new ArrayList<>(byGroup.entrySet());
		int totalGroups = groups.size();
		int fromIndex = (int) Math.min(pageable.getOffset(), totalGroups);
		int toIndex = Math.min(fromIndex + pageable.getPageSize(), totalGroups);
		List<Map.Entry<String, List<ERMMaturityAssessment>>> pageGroups = groups.subList(fromIndex, toIndex);

		List<List<CustomResponse>> rows = pageGroups.stream()
				.map(entry -> mapGroupToCustomResponse(entry.getKey(), entry.getValue(), departmentLabels))
				.toList();

		return new PageImpl<>(rows, pageable, totalGroups);
	}

	private Map<String, String> resolveDepartmentLabels(Map<String, List<ERMMaturityAssessment>> byGroup) {
		Set<Long> functionDeptIds = new HashSet<>();
		for (List<ERMMaturityAssessment> group : byGroup.values()) {
			List<Long> activeDeptIds = ErmMaturityGroupingUtil
					.activeDepartmentIds(ErmMaturityGroupingUtil.firstRowDepartmentIds(group));
			if (!ErmMaturityGroupingUtil.isCompanyWiseMaturity(activeDeptIds)) {
				functionDeptIds.addAll(activeDeptIds);
			}
		}

		Map<String, String> labels = new HashMap<>();
		if (!functionDeptIds.isEmpty()) {
			for (Department d : departmentRepository.findAllById(functionDeptIds)) {
				labels.put(String.valueOf(d.getId()), d.getName());
			}
		}
		return labels;
	}

	private List<CustomResponse> mapGroupToCustomResponse(String ermMaturityId, List<ERMMaturityAssessment> group,
			Map<String, String> departmentLabels) {
		return customResponseMapper.map("ermMaturity", 1L,
				buildGroupedResponse(ermMaturityId, group, departmentLabels), true);
	}

	private ErmMaturityResponse buildGroupedResponse(String ermMaturityId, List<ERMMaturityAssessment> group,
			Map<String, String> departmentLabels) {
		ERMMaturityAssessment first = group.get(0);
		List<Long> activeDeptIds = ErmMaturityGroupingUtil
				.activeDepartmentIds(ErmMaturityGroupingUtil.firstRowDepartmentIds(group));
		BigDecimal totalWeightageScore = ErmMaturityGroupingUtil.totalScore(group);
		Company company = first.getCompany();

		ErmMaturityResponse response = new ErmMaturityResponse();
		response.setMaturityId(first.getId());
		response.setErmMaturityId(ermMaturityId);
		response.setDepartmentIds(new ArrayList<>(activeDeptIds));
		response.setTotalWeightageScore(totalWeightageScore);
		response.setMarksAchieved(totalWeightageScore != null ? totalWeightageScore.toPlainString() : "0");
		response.setWeightageScore(totalWeightageScore != null ? totalWeightageScore.toPlainString() : "0");
		response.setOverallMaturityLevel(ErmMaturityGroupingUtil.maturityLabel(totalWeightageScore));
		response.setDisplayLabel(
				ErmMaturityGroupingUtil.resolveDisplayLabel(ermMaturityId, activeDeptIds, company, departmentLabels));
		if (company != null) {
			response.setCompanyId(company.getId());
		}
		response.setStatus(first.getStatus());
		response.setAssessedBy(first.getAssessedBy());
		response.setDueDate(first.getDueDate());
		response.setActualDate(first.getActualDate());
		response.setLastAssessmentDate(first.getLastAssessmentDate());
		response.setNextAssessmentDate(first.getNextAssessmentDate());
		response.setRiskAppetiteStatus(first.getRiskAppetiteStatus());
		response.setRiskAcceptanceLevel(first.getRiskAcceptanceLevel());
		return response;
	}

	@Override
	public void delete(Long maturityId) throws ResourceNotFoundException {
		Organization organization = OrganizationContext.getOrganization();
		ERMMaturityAssessment ermMaturity = ermMaturityRepository.getByOrg(organization.getId(), maturityId);

		if (ermMaturity == null) {
			throw new ResourceNotFoundException("No record found.");
		}

		ermMaturity.setDeleted(true);
		ermMaturityRepository.save(ermMaturity);
	}

}
