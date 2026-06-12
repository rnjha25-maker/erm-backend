package ermorg.erm.erm_api_gateway.service;

import java.time.LocalDate;
import java.util.List;

import org.springframework.stereotype.Service;

import ermorg.erm.erm_api_gateway.enums.LicenseStatus;
import ermorg.erm.erm_api_gateway.exception.OrganizationValidationException;
import ermorg.erm.erm_api_gateway.model.License;
import ermorg.erm.erm_api_gateway.model.Organization;
import ermorg.erm.erm_api_gateway.repository.LicenseRepository;

@Service
public class OrganizationValidationService {

	private final OrganizationRepository organizationRepository;
	private final LicenseRepository licenseRepository;

	public OrganizationValidationService(OrganizationRepository organizationRepository,
			LicenseRepository licenseRepository) {
		this.organizationRepository = organizationRepository;
		this.licenseRepository = licenseRepository;
	}

	public void validateOrganizationCanAuthenticate(Long organizationId) {
		if (organizationId == null || organizationId <= 0) {
			throw new OrganizationValidationException("Invalid organization.");
		}

		Organization organization = organizationRepository.findById(organizationId)
				.orElseThrow(() -> new OrganizationValidationException("Organization not found."));

		if (Boolean.TRUE.equals(organization.getDeleted())) {
			throw new OrganizationValidationException("Organization is deleted.");
		}

		License license = licenseRepository
				.findByOrganizationIdAndStatusIn(organizationId, List.of(LicenseStatus.ACTIVE, LicenseStatus.GRACE))
				.orElseThrow(() -> new OrganizationValidationException("Organization license expired."));

		if (!isLicenseValid(license)) {
			throw new OrganizationValidationException("Organization license expired.");
		}
	}

	private boolean isLicenseValid(License license) {
		if (license.getEndDate() == null) {
			return false;
		}

		LocalDate today = LocalDate.now();
		if (!today.isAfter(license.getEndDate())) {
			return true;
		}

		if (license.getGracePeriodDays() == null) {
			return false;
		}

		LocalDate graceEnd = license.getEndDate().plusDays(license.getGracePeriodDays());
		return !today.isAfter(graceEnd);
	}
}
