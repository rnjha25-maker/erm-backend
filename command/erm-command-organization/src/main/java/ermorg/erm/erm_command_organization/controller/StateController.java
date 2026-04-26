package ermorg.erm.erm_command_organization.controller;

import ermorg.erm.erm_command_organization.dto.responseDTO.GeneralResponse;
import ermorg.erm.erm_command_organization.dto.responseDTO.StateResponse;
import ermorg.erm.erm_command_organization.exception.ResourceNotFoundException;
import ermorg.erm.erm_command_organization.dto.responseDTO.ResponseStatus;
import ermorg.erm.erm_command_organization.service.IStateService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

//@CrossOrigin
@RestController
@RequestMapping("state")
public class StateController {
	@Autowired
	private IStateService stateService;

	@GetMapping("/all/{id:[\\d]+}")
	public GeneralResponse<List<StateResponse>> getAllStates(@PathVariable Long id) {
		GeneralResponse<List<StateResponse>> response = new GeneralResponse<>();
		try {
			List<StateResponse> states = stateService.getAllStates(id);
			response.setData(states);
			response.setStatus(ResponseStatus.SUCCESS);
		} catch (ResourceNotFoundException e) {
			response.setStatus(ResponseStatus.FAILED);
			response.setMessage(e.getMessage());
		}
		return response;
	}
}
