package ermorg.erm.serviceimpl;

import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;

import org.springframework.stereotype.Component;

import ermorg.erm.exception.ResourceNotFoundException;
import ermorg.erm.model.ERMMaturityAssessment;
import ermorg.erm.model.Escalation;
import ermorg.erm.model.KpaKpiReview;
import ermorg.erm.model.KriKpiReview;
import ermorg.erm.model.Organization;
import ermorg.erm.model.Risk;
import ermorg.erm.model.RiskAssessment;
import ermorg.erm.model.RiskControl;
import ermorg.erm.model.RiskResponseTreatment;
import ermorg.erm.model.RiskReview;
import ermorg.erm.model.RiskTreatment;
import ermorg.erm.model.SubRisk;
import ermorg.erm.repository.ErmMaturityRepository;
import ermorg.erm.repository.EscalationRepository;
import ermorg.erm.repository.KpaKpiReviewRepository;
import ermorg.erm.repository.KriKpiRiskRepository;
import ermorg.erm.repository.RiskAsessmentRepository;
import ermorg.erm.repository.RiskControlRepository;
import ermorg.erm.repository.RiskRepository;
import ermorg.erm.repository.RiskResponseTreatmentRepository;
import ermorg.erm.repository.RiskReviewRepository;
import ermorg.erm.repository.RiskTreatmentRepository;
import ermorg.erm.repository.SubRiskRepository;
import ermorg.erm.util.OrganizationContext;

@Component
public class ApprovalSourceRecordValidator {

	private final RiskRepository riskRepository;
	private final SubRiskRepository subRiskRepository;
	private final RiskAsessmentRepository riskAsessmentRepository;
	private final RiskControlRepository riskControlRepository;
	private final RiskTreatmentRepository riskTreatmentRepository;
	private final RiskResponseTreatmentRepository riskResponseTreatmentRepository;
	private final RiskReviewRepository riskReviewRepository;
	private final KriKpiRiskRepository kriKpiRiskRepository;
	private final KpaKpiReviewRepository kpaKpiReviewRepository;
	private final ErmMaturityRepository ermMaturityRepository;
	private final EscalationRepository escalationRepository;

	public ApprovalSourceRecordValidator(RiskRepository riskRepository, SubRiskRepository subRiskRepository,
			RiskAsessmentRepository riskAsessmentRepository, RiskControlRepository riskControlRepository,
			RiskTreatmentRepository riskTreatmentRepository,
			RiskResponseTreatmentRepository riskResponseTreatmentRepository, RiskReviewRepository riskReviewRepository,
			KriKpiRiskRepository kriKpiRiskRepository, KpaKpiReviewRepository kpaKpiReviewRepository,
			ErmMaturityRepository ermMaturityRepository, EscalationRepository escalationRepository) {
		this.riskRepository = riskRepository;
		this.subRiskRepository = subRiskRepository;
		this.riskAsessmentRepository = riskAsessmentRepository;
		this.riskControlRepository = riskControlRepository;
		this.riskTreatmentRepository = riskTreatmentRepository;
		this.riskResponseTreatmentRepository = riskResponseTreatmentRepository;
		this.riskReviewRepository = riskReviewRepository;
		this.kriKpiRiskRepository = kriKpiRiskRepository;
		this.kpaKpiReviewRepository = kpaKpiReviewRepository;
		this.ermMaturityRepository = ermMaturityRepository;
		this.escalationRepository = escalationRepository;
	}

	public void validate(String sourceModule, String sourceRecordId) throws ResourceNotFoundException {
		if (isBlank(sourceModule) || isBlank(sourceRecordId)) {
			throw new ResourceNotFoundException("sourceModule and sourceRecordId are required.");
		}
		Long recordId;
		try {
			recordId = Long.parseLong(sourceRecordId.trim());
		} catch (NumberFormatException ex) {
			throw new ResourceNotFoundException("sourceRecordId must be a numeric id.");
		}

		String module = normalize(sourceModule);
		boolean exists = validators().getOrDefault(module, id -> false).apply(recordId);
		if (!exists) {
			throw new ResourceNotFoundException("Source record not found for module " + sourceModule + ".");
		}
	}

	private Map<String, Function<Long, Boolean>> validators() {
		return Map.ofEntries(
				Map.entry("risk", id -> riskRepository.findById(id).filter(r -> active(r.getDeleted()))
						.filter(this::sameOrganization).isPresent()),
				Map.entry("subrisk", id -> subRiskRepository.findById(id).filter(r -> active(r.getDeleted()))
						.filter(this::sameOrganization).isPresent()),
				Map.entry("riskassessment", id -> riskAsessmentRepository.findById(id)
						.filter(r -> active(r.getDeleted())).filter(this::sameOrganization).isPresent()),
				Map.entry("riskcontrol", id -> riskControlRepository.findById(id).filter(r -> active(r.getDeleted()))
						.filter(this::sameOrganization).isPresent()),
				Map.entry("risktreatment", id -> riskTreatmentRepository.findById(id)
						.filter(r -> active(r.getDeleted())).filter(this::sameOrganization).isPresent()),
				Map.entry("riskresponsetreatment", id -> riskResponseTreatmentRepository.findById(id)
						.filter(r -> active(r.getDeleted())).filter(this::sameOrganization).isPresent()),
				Map.entry("riskreview", id -> riskReviewRepository.findById(id).filter(r -> active(r.getDeleted()))
						.filter(this::sameOrganization).isPresent()),
				Map.entry("krikpireview", id -> kriKpiRiskRepository.findById(id).filter(r -> active(r.getDeleted()))
						.filter(this::sameOrganization).isPresent()),
				Map.entry("kpakpireview", id -> kpaKpiReviewRepository.findById(id).filter(r -> active(r.getDeleted()))
						.filter(this::sameOrganization).isPresent()),
				Map.entry("ermmaturity", id -> ermMaturityRepository.findById(id).filter(r -> active(r.getDeleted()))
						.filter(this::sameOrganization).isPresent()),
				Map.entry("escalation", id -> escalationRepository.findById(id).filter(r -> active(r.getDeleted()))
						.filter(this::sameOrganization).isPresent()),
				Map.entry("escalations", id -> escalationRepository.findById(id).filter(r -> active(r.getDeleted()))
						.filter(this::sameOrganization).isPresent()));
	}

	private boolean sameOrganization(Risk risk) {
		Organization organization = OrganizationContext.getOrganization();
		return organization == null || risk.getOrganizationId() == null || organization.getId().equals(risk.getOrganizationId());
	}

	private boolean sameOrganization(SubRisk subRisk) {
		Organization organization = OrganizationContext.getOrganization();
		return organization == null || subRisk.getOrganizationId() == null
				|| organization.getId().equals(subRisk.getOrganizationId());
	}

	private boolean sameOrganization(RiskAssessment record) {
		return sameOrganization(record.getOrganization());
	}

	private boolean sameOrganization(RiskControl record) {
		return sameOrganization(record.getOrganization());
	}

	private boolean sameOrganization(RiskTreatment record) {
		return sameOrganization(record.getOrganization());
	}

	private boolean sameOrganization(RiskResponseTreatment record) {
		return sameOrganization(record.getOrganization());
	}

	private boolean sameOrganization(RiskReview record) {
		return sameOrganization(record.getOrganization());
	}

	private boolean sameOrganization(KriKpiReview record) {
		return sameOrganization(record.getOrganization());
	}

	private boolean sameOrganization(KpaKpiReview record) {
		return sameOrganization(record.getOrganization());
	}

	private boolean sameOrganization(ERMMaturityAssessment record) {
		return sameOrganization(record.getOrganization());
	}

	private boolean sameOrganization(Escalation record) {
		return sameOrganization(record.getOrganization());
	}

	private boolean sameOrganization(Organization recordOrganization) {
		Organization organization = OrganizationContext.getOrganization();
		return organization == null || recordOrganization == null || organization.getId().equals(recordOrganization.getId());
	}

	private boolean active(Boolean deleted) {
		return !Boolean.TRUE.equals(deleted);
	}

	private boolean isBlank(String value) {
		return value == null || value.isBlank();
	}

	private String normalize(String sourceModule) {
		return Optional.ofNullable(sourceModule).orElse("").toLowerCase(Locale.ROOT).replaceAll("[^a-z0-9]", "");
	}
}
