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
import org.springframework.transaction.annotation.Transactional;

import ermorg.erm.dto.response.CustomResponse;
import ermorg.erm.dto.response.ErmMaturityResponse;
import ermorg.erm.dto.riskDTO.ErmMaturityDto;
import ermorg.erm.dto.riskDTO.ErmMaturityRequest;
import ermorg.erm.exception.LimitExceedException;
import ermorg.erm.exception.ResourceNotFoundException;
import ermorg.erm.model.Company;
import ermorg.erm.model.Department;
import ermorg.erm.model.ERMMaturityAssessment;
import ermorg.erm.model.ERMMaturityScore;
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
	@Transactional
	public ErmMaturityResponse save(ErmMaturityRequest request) throws ResourceNotFoundException, LimitExceedException {

		if (request == null || request.getMaturityRequest() == null || request.getMaturityRequest().isEmpty()) {
			throw new ResourceNotFoundException("Invailed request.");
		}

		Organization organization = OrganizationContext.getOrganization();
		Company company = resolveCompany(request);
		List<ErmMaturityDto> maturityItems = request.getMaturityRequest();

		List<Long> departmentIds = resolveCommonDepartmentIds(maturityItems);
		String ermMaturityId = resolveErmMaturityId(company.getId(), departmentIds);

		double totalMarks = maturityItems.stream()
				.mapToDouble(dto -> MaturityLevelResolver.parseMarksAchieved(dto.getMarksAchieved())).sum();
		String maturityLabel = MaturityLevelResolver.resolveMaturityLabel(totalMarks);

		ERMMaturityAssessment parent = resolveParent(organization.getId(), ermMaturityId, maturityItems);
		fillParentFields(parent, maturityItems.get(0), organization, company, departmentIds, ermMaturityId,
				maturityLabel);
		syncScores(parent, maturityItems);

		ERMMaturityAssessment saved = ermMaturityRepository.save(parent);
		return new ErmMaturityResponse(saved);
	}

	private ERMMaturityAssessment resolveParent(Long orgId, String ermMaturityId, List<ErmMaturityDto> maturityItems) {
		Long parentId = maturityItems.stream().map(ErmMaturityDto::getMaturityId).filter(id -> id != 0).findFirst()
				.orElse(0L);

		List<ERMMaturityAssessment> groupParents = ermMaturityRepository
				.findAllByOrganizationIdAndErmMaturityId(orgId, ermMaturityId);

		ERMMaturityAssessment parent = null;
		if (parentId != 0) {
			parent = groupParents.stream().filter(p -> Objects.equals(p.getId(), parentId)).findFirst().orElse(null);
			if (parent == null) {
				parent = ermMaturityRepository.getByOrg(orgId, parentId);
			}
		}
		if (parent == null && !groupParents.isEmpty()) {
			parent = groupParents.get(0);
		}
		if (parent == null) {
			return new ERMMaturityAssessment();
		}

		consolidateSiblingParents(parent, groupParents);
		return parent;
	}

	/**
	 * Approach 1 expects one header per ermMaturityId. Soft-delete duplicate parents in the same group.
	 * Score rows for the saved request are synced onto the keeper; run the SQL migration to re-point any
	 * historical child rows from duplicates before soft-delete if needed.
	 */
	private void consolidateSiblingParents(ERMMaturityAssessment keeper, List<ERMMaturityAssessment> groupParents) {
		for (ERMMaturityAssessment sibling : groupParents) {
			if (Objects.equals(sibling.getId(), keeper.getId())) {
				continue;
			}
			sibling.setDeleted(true);
			ermMaturityRepository.save(sibling);
		}
	}

	private void fillParentFields(ERMMaturityAssessment parent, ErmMaturityDto req, Organization organization,
			Company company, List<Long> departmentIds, String ermMaturityId, String maturityLabel) {
		parent.setDeleted(false);
		parent.setAssessedBy(req.getAssessedBy());
		parent.setDueDate(req.getDueDate());
		parent.setActualDate(req.getActualDate());
		parent.setLastAssessmentDate(req.getLastAssessmentDate());
		parent.setNextAssessmentDate(req.getNextAssessmentDate());
		parent.setOverallMaturityLevel(maturityLabel);
		parent.setStatus(req.getStatus());
		parent.setRiskAppetiteStatus(req.getRiskAppetiteStatus());
		parent.setRiskAcceptanceLevel(req.getRiskAcceptanceLevel());
		parent.setOrganization(organization);
		parent.setCompany(company);
		parent.setErmMaturityId(ermMaturityId);
		parent.setDepartmentIds(departmentIds != null ? new ArrayList<>(departmentIds) : new ArrayList<>());
	}

	private void syncScores(ERMMaturityAssessment parent, List<ErmMaturityDto> maturityItems) {
		if (parent.getScores() == null) {
			parent.setScores(new ArrayList<>());
		}

		Map<Long, ERMMaturityScore> existingById = parent.getScores().stream()
				.filter(s -> s.getId() != null)
				.collect(Collectors.toMap(ERMMaturityScore::getId, s -> s, (a, b) -> a));

		Set<Long> retainedIds = new HashSet<>();
		List<ERMMaturityScore> nextScores = new ArrayList<>();

		for (ErmMaturityDto dto : maturityItems) {
			ERMMaturityScore score;
			if (dto.getScoreId() != 0 && existingById.containsKey(dto.getScoreId())) {
				score = existingById.get(dto.getScoreId());
				retainedIds.add(dto.getScoreId());
			} else {
				score = new ERMMaturityScore();
			}
			fillScoreFields(score, dto);
			score.setMaturityAssessment(parent);
			nextScores.add(score);
		}

		parent.getScores().removeIf(existing -> existing.getId() != null && !retainedIds.contains(existing.getId()));

		for (ERMMaturityScore score : nextScores) {
			if (score.getId() == null) {
				parent.getScores().add(score);
			}
		}
	}

	private void fillScoreFields(ERMMaturityScore score, ErmMaturityDto req) {
		score.setDeleted(false);
		score.setAssessmentAreaName(req.getAssessmentAreaName());
		score.setAssessmentAreaId(req.getAssessmentArea());
		score.setKeyAssessmentParameters(req.getKeyAssessmentParameters());
		score.setMarksAchieved(req.getMarksAchieved());
		if (req.getWeightageScore() != null && !req.getWeightageScore().isBlank()) {
			score.setWeightageScore(BigDecimal.valueOf(Double.parseDouble(req.getWeightageScore())));
		} else {
			score.setWeightageScore(null);
		}
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

	@Override
	@Transactional(readOnly = true)
	public ErmMaturityResponse get(Long maturityId) throws ResourceNotFoundException {
		Organization organization = OrganizationContext.getOrganization();
		ERMMaturityAssessment ermMaturity = ermMaturityRepository.getByOrg(organization.getId(), maturityId);

		if (ermMaturity == null) {
			throw new ResourceNotFoundException("No record found.");
		}

		return new ErmMaturityResponse(ermMaturity);
	}

	@Override
	@Transactional(readOnly = true)
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
	@Transactional(readOnly = true)
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
	@Transactional
	public void delete(Long maturityId) throws ResourceNotFoundException {
		Organization organization = OrganizationContext.getOrganization();
		ERMMaturityAssessment ermMaturity = ermMaturityRepository.getByOrg(organization.getId(), maturityId);

		if (ermMaturity == null) {
			throw new ResourceNotFoundException("No record found.");
		}

		ermMaturity.setDeleted(true);
		if (ermMaturity.getScores() != null) {
			for (ERMMaturityScore score : ermMaturity.getScores()) {
				score.setDeleted(true);
			}
		}
		ermMaturityRepository.save(ermMaturity);
	}

}
