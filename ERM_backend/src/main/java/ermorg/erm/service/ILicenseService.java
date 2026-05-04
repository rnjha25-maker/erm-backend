package ermorg.erm.service;

import ermorg.erm.dto.response.LicenseRequest;
import ermorg.erm.dto.response.LicenseValidationResponse;
import ermorg.erm.model.License;

public interface ILicenseService {
	License getActiveLicense(Long orgId) ;
	boolean isLicenseValid(License license) ;
	LicenseValidationResponse validateLicense(Long orgId);
	License createLicense(LicenseRequest request);
	License updateLicense(Long licenseId, LicenseRequest request) ;
}
