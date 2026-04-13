package com.erm.controller;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.erm.constant.ErmStakeholderRole;
import com.erm.dto.ResponseStatus;
import com.erm.dto.response.UserDto;
import com.erm.dto.riskDTO.UserRequestDTO;
import com.erm.model.User;
import com.erm.response.GeneralResponse;
import com.erm.service.IUserService;

@RestController
@RequestMapping("user")
public class UserController {

	@Autowired
	private IUserService userService;
	
	@PostMapping("/save")	
	public GeneralResponse<User> saveUsere(@RequestBody UserRequestDTO request){
		GeneralResponse<User> response = new GeneralResponse<>();
		User savedUser = userService.saveUser(request);
		
		response.setData(savedUser);
		response.setMessage("User saved.");
		response.setStatus(ResponseStatus.SUCCESS);
		
		return response;
	}
	
	@GetMapping("/user-list")
	public GeneralResponse<List<UserDto>> getUserList(){
		GeneralResponse<List<UserDto>> response = new GeneralResponse<>();
		List<UserDto> userList = userService.getUserList();
		
		response.setData(userList);
		response.setStatus(ResponseStatus.SUCCESS);
		
		return response;
	}

	@GetMapping("/by-role/risk-owner")
	public GeneralResponse<List<UserDto>> getUsersByRiskOwner() {
		return usersByRole(ErmStakeholderRole.RISK_OWNER);
	}

	@GetMapping("/by-role/risk-champion")
	public GeneralResponse<List<UserDto>> getUsersByRiskChampion() {
		return usersByRole(ErmStakeholderRole.RISK_CHAMPION);
	}

	@GetMapping("/by-role/control-owner")
	public GeneralResponse<List<UserDto>> getUsersByControlOwner() {
		return usersByRole(ErmStakeholderRole.CONTROL_OWNER);
	}

	@GetMapping("/by-role/primary-responsible")
	public GeneralResponse<List<UserDto>> getUsersByPrimaryResponsible() {
		return usersByRole(ErmStakeholderRole.PRIMARY_RESPONSIBLE);
	}

	@GetMapping("/by-role/approver")
	public GeneralResponse<List<UserDto>> getUsersByApprover() {
		return usersByRole(ErmStakeholderRole.APPROVER);
	}

	@GetMapping("/by-role/risk-reporting")
	public GeneralResponse<List<UserDto>> getUsersByRiskReporting() {
		return usersByRole(ErmStakeholderRole.RISK_REPORTING);
	}

	@GetMapping("/by-role/key/{roleKey}")
	public GeneralResponse<List<UserDto>> getUsersByRoleKey(@PathVariable("roleKey") String roleKey) {
		return usersByRole(ErmStakeholderRole.fromApiKey(roleKey));
	}

	private GeneralResponse<List<UserDto>> usersByRole(ErmStakeholderRole role) {
		GeneralResponse<List<UserDto>> response = new GeneralResponse<>();
		response.setData(userService.getUsersByRole(role));
		response.setStatus(ResponseStatus.SUCCESS);
		return response;
	}
}
