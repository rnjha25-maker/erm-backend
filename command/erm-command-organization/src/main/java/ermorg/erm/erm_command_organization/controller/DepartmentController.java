package ermorg.erm.erm_command_organization.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import ermorg.erm.erm_command_organization.dto.requestDTO.DepartmentRequest;
import ermorg.erm.erm_command_organization.dto.responseDTO.DepartmentDto;
import ermorg.erm.erm_command_organization.dto.responseDTO.DepartmentResponse;
import ermorg.erm.erm_command_organization.dto.responseDTO.GeneralResponse;
import ermorg.erm.erm_command_organization.dto.responseDTO.ResponseStatus;
import ermorg.erm.erm_command_organization.exception.ResourceNotFoundException;
import ermorg.erm.erm_command_organization.service.IDepartmentService;

@RestController
@RequestMapping("department")
//@CrossOrigin
public class DepartmentController {

    @Autowired
    private IDepartmentService departmentService;

    @PostMapping("/create")
    public GeneralResponse<DepartmentResponse> createDepartment(@RequestBody DepartmentRequest request) {
        GeneralResponse<DepartmentResponse> response = new GeneralResponse<>();
        DepartmentResponse department = departmentService.createDepartment(request);
        response.setData(department);
        response.setStatus(ResponseStatus.SUCCESS);
        response.setMessage("Department created!");
        return response;
    }

    @PutMapping("/update")
    public GeneralResponse<DepartmentResponse> updateDepartment(@RequestBody DepartmentRequest request) throws ResourceNotFoundException {
        GeneralResponse<DepartmentResponse> response = new GeneralResponse<>();
        
        	DepartmentResponse department = departmentService.updateDepartment(request);
            response.setData(department);
            response.setStatus(ResponseStatus.SUCCESS);
            response.setMessage("Department updated!");
        
        return response;
    }

    @GetMapping("/{departmentId:[\\d]+}")
    public GeneralResponse<DepartmentDto> getDepartment(@PathVariable Long departmentId) throws ResourceNotFoundException {
        GeneralResponse<DepartmentDto> response = new GeneralResponse<>();
       
        DepartmentDto department = departmentService.getDepartment(departmentId);
        response.setData(department);
        response.setStatus(ResponseStatus.SUCCESS);
        response.setMessage("Department deleted!");
       
        return response;
    }
    
    @GetMapping("/all-by-organization-id/{organizationId:[\\d]+}")
    public GeneralResponse<DepartmentResponse> getAllDepartmentByOrganizationId(@PathVariable Long organizationId) throws ResourceNotFoundException {
        GeneralResponse<DepartmentResponse> response = new GeneralResponse<>();
       
        DepartmentResponse department = departmentService.getAllDepartmentByOrganizationId(organizationId);
        
        	response.setData(department);
            response.setStatus(ResponseStatus.SUCCESS);
            response.setMessage("Department deleted!");
       
        return response;
    }
    @DeleteMapping("/delete/{id:[\\d]+}")
    public GeneralResponse<Void> deleteDepartment(@PathVariable Long id) {
        GeneralResponse<Void> response = new GeneralResponse<>();
       
            departmentService.deleteDepartment(id);
            response.setStatus(ResponseStatus.SUCCESS);
            response.setMessage("Department deleted!");
       
        return response;
    }
}
