package ermorg.erm.erm_command_organization.controller;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import ermorg.erm.erm_command_organization.dto.requestDTO.RoleRequest;
import ermorg.erm.erm_command_organization.dto.responseDTO.GeneralResponse;
import ermorg.erm.erm_command_organization.dto.responseDTO.ResponseStatus;
import ermorg.erm.erm_command_organization.dto.responseDTO.RoleResponse;
import ermorg.erm.erm_command_organization.exception.ResourceNotFoundException;
import ermorg.erm.erm_command_organization.service.IRoleService;

@RestController
@RequestMapping("/role")
//@CrossOrigin
public class RoleController {
	
	@Autowired
	private IRoleService roleService;
	
	@PostMapping("/save")
	public GeneralResponse<RoleResponse> saveRole(@RequestBody RoleRequest roleRequest) {
		GeneralResponse<RoleResponse> response = new GeneralResponse<>();
		
		RoleResponse role = roleService.saveRole(roleRequest);
		
		response.setData(role);
		response.setStatus(ResponseStatus.SUCCESS);
		response.setMessage("Role saved.");
		
		
		return response;
	}
	
	@DeleteMapping("/delete/{roleId:[\\d]+}")
	public GeneralResponse<Void> deleteRole(@PathVariable Long roleId) throws ResourceNotFoundException {
		GeneralResponse<Void> response = new GeneralResponse<>();
		
		roleService.deleteRole(roleId);
		
		response.setStatus(ResponseStatus.SUCCESS);
		response.setMessage("Role deleted.");
		
		
		return response;
	}
	@GetMapping("/{roleId:[\\d]+}")
	public GeneralResponse<RoleResponse> getRole(@PathVariable Long roleId) throws ResourceNotFoundException {
		GeneralResponse<RoleResponse> response = new GeneralResponse<>();
		
		RoleResponse role = roleService.getRole(roleId);
		
		response.setData(role);
		response.setStatus(ResponseStatus.SUCCESS);
		
		return response;
	}
	
	@GetMapping("/all")
	public GeneralResponse<List<RoleResponse>> getAllRoles() throws ResourceNotFoundException {
		GeneralResponse<List<RoleResponse>> response = new GeneralResponse<>();
		
		List<RoleResponse> role = roleService.getAllRoles();
		
		response.setData(role);
		response.setStatus(ResponseStatus.SUCCESS);
		
		return response;
	}

}
