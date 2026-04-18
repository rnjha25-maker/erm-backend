package ermorg.org_setup_command.controller;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import ermorg.org_setup_command.dto.request.DepartmentDTO;
import ermorg.org_setup_command.dto.response.DepartmentResponse;
import ermorg.org_setup_command.dto.response.GeneralResponse;
import ermorg.org_setup_command.dto.response.ResponseStatus;
import ermorg.org_setup_command.exception.InvalidResourceAccess;
import ermorg.org_setup_command.exception.ResourceNotFoundException;
import ermorg.org_setup_command.service.IDepartmentService;
import ermorg.org_setup_command.service.InvalidDataException;

@RestController
@RequestMapping("department")
public class DepartmentController {

	@Autowired
	private IDepartmentService departmentService;

	@PostMapping("/create")
	public GeneralResponse<DepartmentResponse> createDepartment(@RequestBody DepartmentDTO request)
			throws ResourceNotFoundException, InvalidResourceAccess {
		GeneralResponse<DepartmentResponse> response = new GeneralResponse<>();
		DepartmentResponse department = departmentService.createDepartment(request);
		response.setData(department);
		response.setStatus(ResponseStatus.SUCCESS);
		response.setMessage("Department created!");
		return response;
	}

	@PutMapping("/update")
	public GeneralResponse<DepartmentResponse> updateDepartment(@RequestBody DepartmentDTO request)
			throws ResourceNotFoundException, InvalidResourceAccess {
		GeneralResponse<DepartmentResponse> response = new GeneralResponse<>();

		DepartmentResponse department = departmentService.updateDepartment(request);
		response.setData(department);
		response.setStatus(ResponseStatus.SUCCESS);
		response.setMessage("Department updated!");

		return response;
	}

	@DeleteMapping("/delete/{id:[\\d]+}")
	public GeneralResponse<Void> deleteDepartment(@PathVariable Long id) throws InvalidDataException, ResourceNotFoundException, InvalidResourceAccess {
		GeneralResponse<Void> response = new GeneralResponse<>();
		
			departmentService.deleteDepartment(id);
			response.setStatus(ResponseStatus.SUCCESS);
			response.setMessage("Department deleted!");
		
		return response;
	}
	
	@GetMapping("/{id:[\\d]+}")
	public GeneralResponse<DepartmentResponse> getDepartment(@PathVariable Long id) throws InvalidDataException, ResourceNotFoundException, InvalidResourceAccess {
		GeneralResponse<DepartmentResponse> response = new GeneralResponse<>();
		
		DepartmentResponse department = departmentService.getDepartment(id);
			response.setStatus(ResponseStatus.SUCCESS);
			response.setMessage("Department deleted!");
		
		return response;
	}
	
	@GetMapping("/all/{id:[\\d]+}")
	public GeneralResponse<List<DepartmentResponse>> getAllDepartments(@PathVariable Long id) throws InvalidDataException, ResourceNotFoundException, InvalidResourceAccess {
		GeneralResponse<List<DepartmentResponse>> response = new GeneralResponse<>();
		
			List<DepartmentResponse> departments = departmentService.getAllDepartments(id);
			
			response.setData(departments);
			response.setStatus(ResponseStatus.SUCCESS);
			response.setMessage("Department deleted!");
		
		return response;
	}
}
