package ermorg.erm.erm_command_organization.service;

import ermorg.erm.erm_command_organization.dto.requestDTO.LicenseRequest;
import ermorg.erm.erm_command_organization.dto.requestDTO.LicenseValidationResponse;
import ermorg.erm.erm_command_organization.model.License;

public interface ILicenseService {
	License getActiveLicense(Long orgId) ;
	boolean isLicenseValid(License license) ;
	LicenseValidationResponse validateLicense(Long orgId);
	License createLicense(LicenseRequest request);
	License updateLicense(Long licenseId, LicenseRequest request) ;
}
