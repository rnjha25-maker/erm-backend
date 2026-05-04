package ermorg.erm.serviceimpl;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import ermorg.erm.dto.response.LicenseRequest;
import ermorg.erm.dto.response.LicenseValidationResponse;
import ermorg.erm.model.License;
import ermorg.erm.model.Organization;
import ermorg.erm.repository.LicenseRepository;
import ermorg.erm.repository.OrganizationRepository;
import ermorg.erm.service.ILicenseService;
import ermorg.erm.util.mapper.LicenseStatus;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class LicenseServiceImpl implements ILicenseService{
	
	@Autowired
	private LicenseRepository licenseRepository;
	@Autowired
	private OrganizationRepository organizationRepository;
	
	@Override
	public License getActiveLicense(Long orgId) {

		return licenseRepository
				.findByOrganizationIdAndStatusIn(orgId, List.of(LicenseStatus.ACTIVE, LicenseStatus.GRACE))
				.orElseThrow(() -> new RuntimeException("No active license found"));
	}
	
	@Override
	public boolean isLicenseValid(License license) {

	    LocalDate today = LocalDate.now();

	    // ACTIVE
	    if (today.isBefore(license.getEndDate()) || today.isEqual(license.getEndDate())) {
	        return true;
	    }

	    // GRACE PERIOD
	    if (license.getGracePeriodDays() != null) {
	        LocalDate graceEnd =
	                license.getEndDate().plusDays(license.getGracePeriodDays());

	        return today.isBefore(graceEnd) || today.isEqual(graceEnd);
	    }

	    return false;
	}
	
	@Override
	public LicenseValidationResponse validateLicense(Long orgId) {

	    License license = getActiveLicense(orgId);

	    LocalDate today = LocalDate.now();

	    boolean isValid = isLicenseValid(license);

	    boolean inGrace = false;
	    int daysRemaining = 0;

	    if (today.isAfter(license.getEndDate())) {
	        inGrace = true;
	        LocalDate graceEnd = license.getEndDate()
	                .plusDays(license.getGracePeriodDays() != null ? license.getGracePeriodDays() : 0);

	        daysRemaining = (int) ChronoUnit.DAYS.between(today, graceEnd);
	    } else {
	        daysRemaining = (int) ChronoUnit.DAYS.between(today, license.getEndDate());
	    }

	    return LicenseValidationResponse.builder()
	            .valid(isValid)
	            .status(license.getStatus().name())
	            .endDate(license.getEndDate())
	            .daysRemaining(Math.max(daysRemaining, 0))
	            .inGracePeriod(inGrace)
	            .build();
	}

	@Override
	public License createLicense(LicenseRequest request) {

		Organization organization = organizationRepository.findById(request.getOrganizationId())
				.orElseThrow(() -> new RuntimeException("Organization not found"));
		
		// deactivate old license
		licenseRepository.findByOrganizationIdAndStatusIn(request.getOrganizationId(), List.of(LicenseStatus.ACTIVE))
		        .ifPresent(old -> {
		            old.setStatus(LicenseStatus.EXPIRED);
		            licenseRepository.save(old);
		        });
		
		License license = new License();
		license.setOrganization(organization);
		license.setPlanType(request.getPlanType());
		license.setStartDate(request.getStartDate());
		license.setEndDate(request.getEndDate());
		license.setGracePeriodDays(request.getGracePeriodDays());
		license.setAutoRenew(request.getAutoRenew());
		license.setStatus(LicenseStatus.ACTIVE);
		license.setCreatedDate(LocalDate.now());
		license.setUpdateDate(LocalDate.now());
		return licenseRepository.save(license);
	}

	@Override
	public License updateLicense(Long licenseId, LicenseRequest request) {
		License license = licenseRepository.findById(licenseId)
	            .orElseThrow(() -> new RuntimeException("License not found"));

	    license.setPlanType(request.getPlanType());
	    license.setStartDate(request.getStartDate());
	    license.setEndDate(request.getEndDate());
	    license.setGracePeriodDays(request.getGracePeriodDays());
	    license.setAutoRenew(request.getAutoRenew());
	    license.setUpdateDate(LocalDate.now());

	    return licenseRepository.save(license);
	}
}
