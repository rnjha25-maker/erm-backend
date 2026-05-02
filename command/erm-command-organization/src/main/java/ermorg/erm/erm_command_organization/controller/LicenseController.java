package ermorg.erm.erm_command_organization.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import ermorg.erm.erm_command_organization.dto.requestDTO.LicenseRequest;
import ermorg.erm.erm_command_organization.dto.requestDTO.LicenseValidationResponse;
import ermorg.erm.erm_command_organization.dto.responseDTO.GeneralResponse;
import ermorg.erm.erm_command_organization.dto.responseDTO.ResponseStatus;
import ermorg.erm.erm_command_organization.model.License;
import ermorg.erm.erm_command_organization.repository.OrganizationRepository;
import ermorg.erm.erm_command_organization.service.ILicenseService;
import io.swagger.v3.oas.annotations.parameters.RequestBody;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/license")
@RequiredArgsConstructor
public class LicenseController {

	private final ILicenseService licenseService;
	@Autowired
	OrganizationRepository organizationRepository;

	// ✅ 1. Get License Validation Info
	@GetMapping("/validate/{organizationId}")
	public GeneralResponse<LicenseValidationResponse> validateLicense(@PathVariable Long organizationId) {

		LicenseValidationResponse validation = licenseService.validateLicense(organizationId);

		GeneralResponse<LicenseValidationResponse> response = new GeneralResponse<>();
		response.setData(validation);
		response.setStatus(ResponseStatus.SUCCESS);

		return response;
	}

	// ✅ 2. Create License
	@PostMapping("/create")
	public GeneralResponse<License> createLicense(@RequestBody LicenseRequest request) {
		License license = licenseService.createLicense(request);
		GeneralResponse<License> response = new GeneralResponse<>();
		response.setData(license);
		response.setMessage("License created successfully");
		response.setStatus(ResponseStatus.SUCCESS);

		return response;
	}

	// ✅ 3. Update License
	@PutMapping("/update/{licenseId}")
	public GeneralResponse<License> updateLicense(@PathVariable Long licenseId, @RequestBody LicenseRequest request) {

		License updated = licenseService.updateLicense(licenseId, request);

		GeneralResponse<License> response = new GeneralResponse<>();
		response.setData(updated);
		response.setMessage("License updated successfully");
		response.setStatus(ResponseStatus.SUCCESS);

		return response;
	}
}