package ermorg.erm.serviceimpl;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import ermorg.erm.dto.response.CustomResponse;
import ermorg.erm.dto.response.ErmMaturityResponse;
import ermorg.erm.dto.riskDTO.ErmMaturityDto;
import ermorg.erm.dto.riskDTO.ErmMaturityRequest;
import ermorg.erm.exception.LimitExceedException;
import ermorg.erm.exception.ResourceNotFoundException;
import ermorg.erm.model.Company;
import ermorg.erm.model.ERMMaturityAssessment;
import ermorg.erm.model.Organization;
import ermorg.erm.repository.CompanyRepository;
import ermorg.erm.repository.ErmMaturityRepository;
import ermorg.erm.service.IErmMaturityService;
import ermorg.erm.util.CompanyContext;
import ermorg.erm.util.OrganizationContext;
import ermorg.erm.util.mapper.CustomResponseMapper;

@Service
public class MaturityService implements IErmMaturityService {

	@Autowired
	private ErmMaturityRepository ermMaturityRepository;

	@Autowired
	private CompanyRepository companyRepository;

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

		double totalMarks = maturityItems.stream().mapToDouble(dto -> parseMarksAchieved(dto.getMarksAchieved())).sum();
		String maturityLabel = resolveMaturityLabel(totalMarks);

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
		List<Long> activeDeptIds = departmentIds.stream()
				.filter(id -> id != null && id != 0)
				.distinct()
				.sorted()
				.toList();

		if (activeDeptIds.isEmpty()) {
			return String.valueOf(companyId);
		}
		return companyId + "_" + activeDeptIds.stream().map(String::valueOf).collect(Collectors.joining("_"));
	}

	private double parseMarksAchieved(String marksAchieved) {
		if (marksAchieved == null || marksAchieved.isBlank()) {
			return 0;
		}
		return Double.parseDouble(marksAchieved.trim());
	}

	private String resolveMaturityLabel(double totalMarks) {
		if (totalMarks < 20) {
			return "Nascent";
		}
		if (totalMarks < 40) {
			return "Emerging";
		}
		if (totalMarks < 60) {
			return "Developed";
		}
		if (totalMarks <= 80) {
			return "Integrated";
		}
		return "Advanced";
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

		Page<ERMMaturityAssessment> page = ermMaturityRepository.getAllByOrg(organization.getId(), pageable);

		if (page.isEmpty()) {
			throw new ResourceNotFoundException("No record found.");
		}

		return page.map(this::mapToCustomResponse);
	}

	private List<CustomResponse> mapToCustomResponse(ERMMaturityAssessment ermMaturity) {

		return customResponseMapper.map("ermMaturity", 1L, new ErmMaturityResponse(ermMaturity), true);
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
