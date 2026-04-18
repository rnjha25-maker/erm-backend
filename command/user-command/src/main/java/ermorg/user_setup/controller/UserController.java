package ermorg.user_setup.controller;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import ermorg.user_setup.constant.ErmStakeholderRole;
import ermorg.user_setup.dto.request.UserDTO;
import ermorg.user_setup.dto.response.GeneralResponse;
import ermorg.user_setup.dto.response.ResponseStatus;
import ermorg.user_setup.dto.response.UserResponse;
import ermorg.user_setup.exception.InvalidResourceAccess;
import ermorg.user_setup.exception.ResourceNotFoundException;
import ermorg.user_setup.service.IUserService;

@RestController
@RequestMapping("/users")
public class UserController {
	
	@Autowired
	private IUserService userService;
	
	@PostMapping("/save")
	public GeneralResponse<UserResponse> save(@RequestBody UserDTO request) throws ResourceNotFoundException, InvalidResourceAccess{
		
		GeneralResponse<UserResponse> response = new GeneralResponse<>();
		UserResponse savedUser = userService.saveUser(request);
		
		response.setData(savedUser);
		response.setMessage("User added.");
		response.setStatus(ResponseStatus.SUCCESS);
		return response;
	}
	
	@PutMapping("/update")
	public GeneralResponse<UserResponse> update(@RequestBody UserDTO request) throws ResourceNotFoundException, InvalidResourceAccess{
		
		GeneralResponse<UserResponse> response = new GeneralResponse<>();
		UserResponse savedUser = userService.updateUser(request);
		
		response.setData(savedUser);
		response.setMessage("User updated.");
		response.setStatus(ResponseStatus.SUCCESS);
		return response;
	}
	
	@GetMapping("/{userId}")
	public GeneralResponse<UserResponse> getUser(@PathVariable("userId") Long userId) throws ResourceNotFoundException, InvalidResourceAccess{
		
		GeneralResponse<UserResponse> response = new GeneralResponse<>();
		UserResponse savedUser = userService.getUser(userId);
		
		response.setData(savedUser);
		response.setStatus(ResponseStatus.SUCCESS);
		return response;
	}
	
	@GetMapping("/all/{companyId:[\\d]+}")
	public GeneralResponse<List<UserResponse>> getUsers(@PathVariable("companyId") Long companyId) throws ResourceNotFoundException, InvalidResourceAccess{
		
		GeneralResponse<List<UserResponse>> response = new GeneralResponse<>();
		List<UserResponse> savedUser = userService.getAllUser(companyId);
		
		response.setData(savedUser);
		response.setStatus(ResponseStatus.SUCCESS);
		return response;
	}

	@GetMapping("/by-role/risk-owner/{companyId:[\\d]+}")
	public GeneralResponse<List<UserResponse>> getUsersByRiskOwner(@PathVariable("companyId") Long companyId)
			throws ResourceNotFoundException, InvalidResourceAccess {
		return usersByRole(companyId, ErmStakeholderRole.RISK_OWNER);
	}

	@GetMapping("/by-role/risk-champion/{companyId:[\\d]+}")
	public GeneralResponse<List<UserResponse>> getUsersByRiskChampion(@PathVariable("companyId") Long companyId)
			throws ResourceNotFoundException, InvalidResourceAccess {
		return usersByRole(companyId, ErmStakeholderRole.RISK_CHAMPION);
	}

	@GetMapping("/by-role/control-owner/{companyId:[\\d]+}")
	public GeneralResponse<List<UserResponse>> getUsersByControlOwner(@PathVariable("companyId") Long companyId)
			throws ResourceNotFoundException, InvalidResourceAccess {
		return usersByRole(companyId, ErmStakeholderRole.CONTROL_OWNER);
	}

	@GetMapping("/by-role/primary-responsible/{companyId:[\\d]+}")
	public GeneralResponse<List<UserResponse>> getUsersByPrimaryResponsible(@PathVariable("companyId") Long companyId)
			throws ResourceNotFoundException, InvalidResourceAccess {
		return usersByRole(companyId, ErmStakeholderRole.PRIMARY_RESPONSIBLE);
	}

	@GetMapping("/by-role/approver/{companyId:[\\d]+}")
	public GeneralResponse<List<UserResponse>> getUsersByApprover(@PathVariable("companyId") Long companyId)
			throws ResourceNotFoundException, InvalidResourceAccess {
		return usersByRole(companyId, ErmStakeholderRole.APPROVER);
	}

	@GetMapping("/by-role/risk-reporting/{companyId:[\\d]+}")
	public GeneralResponse<List<UserResponse>> getUsersByRiskReporting(@PathVariable("companyId") Long companyId)
			throws ResourceNotFoundException, InvalidResourceAccess {
		return usersByRole(companyId, ErmStakeholderRole.RISK_REPORTING);
	}

	@GetMapping("/by-role/{companyId:[\\d]+}/{roleKey}")
	public GeneralResponse<List<UserResponse>> getUsersByRoleKey(@PathVariable("companyId") Long companyId,
			@PathVariable("roleKey") String roleKey) throws ResourceNotFoundException, InvalidResourceAccess {
		return usersByRole(companyId, ErmStakeholderRole.fromApiKey(roleKey));
	}

	private GeneralResponse<List<UserResponse>> usersByRole(Long companyId, ErmStakeholderRole role)
			throws ResourceNotFoundException, InvalidResourceAccess {
		GeneralResponse<List<UserResponse>> response = new GeneralResponse<>();
		response.setData(userService.getUsersByRole(companyId, role));
		response.setStatus(ResponseStatus.SUCCESS);
		return response;
	}

}
