package ermorg.erm.erm_command_organization.controller;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import ermorg.erm.erm_command_organization.dto.requestDTO.RightDto;
import ermorg.erm.erm_command_organization.dto.responseDTO.GeneralResponse;
import ermorg.erm.erm_command_organization.dto.responseDTO.ResponseStatus;
import ermorg.erm.erm_command_organization.dto.responseDTO.RightResponse;
import ermorg.erm.erm_command_organization.exception.ResourceNotFoundException;
import ermorg.erm.erm_command_organization.service.IRightService;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/right")
//@CrossOrigin
public class RightController {
	
	@Autowired
	private IRightService rightService;
	@PostMapping("/save")
	public GeneralResponse<RightResponse> saveRight(@Valid @RequestBody RightDto request) throws ResourceNotFoundException {
		GeneralResponse<RightResponse> response = new GeneralResponse<>();

		RightResponse right = rightService.saveRight(request);
		
		response.setData(right);
		response.setMessage("Right saved.");
		response.setStatus(ResponseStatus.SUCCESS);
		return response;
		
	}
	
    @DeleteMapping("/delete/{id:[\\d]+}")
	public GeneralResponse<RightResponse> deleteRight(@PathVariable Long id) throws ResourceNotFoundException {
		GeneralResponse<RightResponse> response = new GeneralResponse<>();

		rightService.deleteRight(id);
		
		response.setMessage("Right deleted.");
		response.setStatus(ResponseStatus.SUCCESS);
		return response;
		
	}
    
    @GetMapping("/{id:[\\d]+}")
	public GeneralResponse<RightResponse> getRight(@PathVariable Long id) throws ResourceNotFoundException {
		GeneralResponse<RightResponse> response = new GeneralResponse<>();

		RightResponse right = rightService.getRightById(id);
		
		response.setData(right);
		response.setStatus(ResponseStatus.SUCCESS);
		return response;
		
	}
    
    @GetMapping("/all")
	public GeneralResponse<List<RightResponse>> getAllRight() throws ResourceNotFoundException {
		GeneralResponse<List<RightResponse>> response = new GeneralResponse<>();

		List<RightResponse> rights = rightService.getAllRights();
		
		response.setData(rights);
		response.setStatus(ResponseStatus.SUCCESS);
		return response;
		
	}
    
   

}
