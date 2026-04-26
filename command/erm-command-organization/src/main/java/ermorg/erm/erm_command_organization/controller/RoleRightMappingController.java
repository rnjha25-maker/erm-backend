package ermorg.erm.erm_command_organization.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import ermorg.erm.erm_command_organization.dto.requestDTO.RoleRightMappingRequest;
import ermorg.erm.erm_command_organization.dto.responseDTO.GeneralResponse;
import ermorg.erm.erm_command_organization.dto.responseDTO.ResponseStatus;
import ermorg.erm.erm_command_organization.dto.responseDTO.RoleRightMappingResponse;
import ermorg.erm.erm_command_organization.exception.ResourceNotFoundException;
import ermorg.erm.erm_command_organization.service.IRoleRightMappingService;

@RestController
//@CrossOrigin
@RequestMapping("/role-right-mapping")
public class RoleRightMappingController {

	@Autowired
	IRoleRightMappingService roleRightMappingService;

	@PostMapping("/map")
	public GeneralResponse<RoleRightMappingResponse> map(@RequestBody RoleRightMappingRequest roleRightMappingRequest)
			throws ResourceNotFoundException {
		GeneralResponse<RoleRightMappingResponse> response = new GeneralResponse<>();

		RoleRightMappingResponse roleRightMappings = roleRightMappingService.map(roleRightMappingRequest);

		response.setData(roleRightMappings);
		response.setStatus(ResponseStatus.SUCCESS);
		response.setMessage("Mapped successfully.");
		return response;
	}

	@GetMapping("/get-mappings/{id:[\\d]+}")
	public GeneralResponse<RoleRightMappingResponse> getMappings(@PathVariable Long id) {
		GeneralResponse<RoleRightMappingResponse> response = new GeneralResponse<>();

		RoleRightMappingResponse roleRightMappings = roleRightMappingService.getMapping(id);

		response.setData(roleRightMappings);
		response.setStatus(ResponseStatus.SUCCESS);
		return response;
	}

}
