package ermorg.erm.service;

import ermorg.erm.dto.response.LicenseRequest;
import ermorg.erm.dto.response.LicenseValidationResponse;
import ermorg.erm.dto.riskDTO.LicenseResponseDTO;
import ermorg.erm.model.License;

public interface ILicenseService {
	LicenseResponseDTO getActiveLicense(Long orgId) ;
	boolean isLicenseValid(LicenseResponseDTO license) ;
	LicenseValidationResponse validateLicense(Long orgId);
	LicenseResponseDTO  createLicense(LicenseRequest request);
	License updateLicense(Long licenseId, LicenseRequest request) ;
}
