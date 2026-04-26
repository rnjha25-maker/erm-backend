package ermorg.erm.serviceimpl;

import java.util.ArrayList;
import java.util.List;

import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import ermorg.erm.dto.response.CustomResponse;
import ermorg.erm.dto.response.EscalationResponse;
import ermorg.erm.dto.riskDTO.EscalationRequestDto;
import ermorg.erm.exception.ResourceNotFoundException;
import ermorg.erm.model.Company;
import ermorg.erm.model.Escalation;
import ermorg.erm.model.Organization;
import ermorg.erm.repository.EscalationRepository;
import ermorg.erm.service.IEscalationService;
import ermorg.erm.util.CompanyContext;
import ermorg.erm.util.OrganizationContext;
import ermorg.erm.util.mapper.CustomResponseMapper;

import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
public class EscalationService implements IEscalationService {

	@Autowired
	private EscalationRepository escalationRepository;

	@Autowired
	private CustomResponseMapper customResponseMapper;

	@Override
	public EscalationResponse save(EscalationRequestDto request) throws ResourceNotFoundException {
		Organization organization = OrganizationContext.getOrganization();
		Company company = CompanyContext.getCompany();

		Escalation esclation;
		if (request.getEsclationId() == 0) {
			// Create new entity for new escalations
			esclation = new Escalation();
		} else {
			// Find existing entity for updates
			esclation = escalationRepository.findById(request.getEsclationId()).filter(r -> !r.getDeleted())
					.filter(r -> r.getOrganization().getId().equals(organization.getId()))
					.orElseThrow(() -> new ResourceNotFoundException("Escalation not found."));
		}

		ModelMapper mapper = new ModelMapper();

//		mapper.map(request, esclation);

		esclation.setAction(request.getAction());
		esclation.setAlertReminder(request.getAlertReminder());
		esclation.setDescription(request.getDescription());
		esclation.setEmailIdPrimaryResponsible(request.getEmailIdPrimaryResponsible());
		esclation.setEscalationEmailId(request.getEscalationEmailId());
		esclation.setEscalationRequired(request.getEscalationRequired());
		esclation.setEscalationResolutionPeriod(request.getEscalationResolutionPeriod());
		esclation.setPriority(request.getPriority());
		esclation.setRemarks(request.getRemarks());
		esclation.setReportingLevel(request.getReportingLevel());
		esclation.setReportingLevelEmailId(request.getReportingLevelEmailId());
		esclation.setStatus(request.getStatus());
		esclation.setAssignedToPrimaryResponsible(request.getAssignedToPrimaryResponsible());

		esclation.setOrganization(organization);
		esclation.setCompany(company);

		Escalation saved = escalationRepository.save(esclation);

		return new EscalationResponse(saved);
	}

	@Override
	public EscalationResponse get(Long esclationId) throws ResourceNotFoundException {
		Organization organization = OrganizationContext.getOrganization();
		Escalation esclation = escalationRepository.findByOrgIdAndId(organization.getId(), esclationId);

		if (esclation == null) {
			throw new ResourceNotFoundException("No esclation found.");
		}

		return new EscalationResponse(esclation);
	}

	@Override
	public List<CustomResponse> getById(Long esclationId) throws ResourceNotFoundException {
		Organization organization = OrganizationContext.getOrganization();
		Escalation esclation = escalationRepository.findByOrgIdAndId(organization.getId(), esclationId);

		if (esclation == null) {
			throw new ResourceNotFoundException("No esclation found.");

		}

		List<CustomResponse> response = customResponseMapper.map("escalation", 1l, new EscalationResponse(esclation),
				false);

		return response;
	}

	@Override
	@Transactional(readOnly = true)
	public List<List<CustomResponse>> getAll() throws ResourceNotFoundException {
		Organization organization = OrganizationContext.getOrganization();

		List<Escalation> allEscalations = escalationRepository.getByOrgId(organization.getId());

		List<List<CustomResponse>> allResponse = new ArrayList<>();
		for (Escalation esclation : allEscalations) {
			List<CustomResponse> response = customResponseMapper.map("escalation", 1l,
					new EscalationResponse(esclation), true);

			allResponse.add(response);
		}

		return allResponse;
	}

	@Override
	public void delete(Long esclationId) throws ResourceNotFoundException {
		Organization organization = OrganizationContext.getOrganization();
		Escalation esclation = escalationRepository.findByOrgIdAndId(organization.getId(), esclationId);

		if (esclation == null) {
			throw new ResourceNotFoundException("No esclation found.");
		}

		esclation.setDeleted(true);
		escalationRepository.save(esclation);
	}

}
