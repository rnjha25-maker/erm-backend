package ermorg.erm.erm_command_organization.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import ermorg.erm.erm_command_organization.dto.requestDTO.BusinessSegmentRequest;
import ermorg.erm.erm_command_organization.dto.responseDTO.BusinessSegmentDto;
import ermorg.erm.erm_command_organization.dto.responseDTO.BusinessSegmentResponse;
import ermorg.erm.erm_command_organization.dto.responseDTO.GeneralResponse;
import ermorg.erm.erm_command_organization.dto.responseDTO.ResponseStatus;
import ermorg.erm.erm_command_organization.exception.ResourceNotFoundException;
import ermorg.erm.erm_command_organization.service.IBusinessSegmentService;

@RestController
@RequestMapping("business-segment")
//@CrossOrigin
public class BusinessSegmentController {

    @Autowired
    private IBusinessSegmentService businessSegmentService;

    @PostMapping("/create")
    public GeneralResponse<BusinessSegmentResponse> createBusinessSegment(@RequestBody BusinessSegmentRequest request) {
        GeneralResponse<BusinessSegmentResponse> response = new GeneralResponse<>();
        BusinessSegmentResponse businessSegment = businessSegmentService.createBusinessSegment(request);
        response.setData(businessSegment);
        response.setStatus(ResponseStatus.SUCCESS);
        response.setMessage("Business Segment created!");
        return response;
    }

    @PutMapping("/update")
    public GeneralResponse<BusinessSegmentResponse> updateBusinessSegment(@RequestBody BusinessSegmentRequest request) throws ResourceNotFoundException {
        GeneralResponse<BusinessSegmentResponse> response = new GeneralResponse<>();
        
        BusinessSegmentResponse businessSegment = businessSegmentService.updateBusinessSegment(request);
        response.setData(businessSegment);
        response.setStatus(ResponseStatus.SUCCESS);
        response.setMessage("Business Segment updated!");
        
        return response;
    }

    @GetMapping("/{businessSegmentId:[\\d]+}")
    public GeneralResponse<BusinessSegmentDto> getBusinessSegment(@PathVariable Long businessSegmentId) throws ResourceNotFoundException {
        GeneralResponse<BusinessSegmentDto> response = new GeneralResponse<>();
       
        BusinessSegmentDto businessSegment = businessSegmentService.getBusinessSegment(businessSegmentId);
        response.setData(businessSegment);
        response.setStatus(ResponseStatus.SUCCESS);
        response.setMessage("Business Segment retrieved!");
       
        return response;
    }
    
    @GetMapping("/get-all-by-company-id/{companyId:[\\d]+}")
    public GeneralResponse<BusinessSegmentResponse> getAllBusinessSegmentsByCompanyId(@PathVariable Long companyId) throws ResourceNotFoundException {
        GeneralResponse<BusinessSegmentResponse> response = new GeneralResponse<>();
       
        BusinessSegmentResponse businessSegment = businessSegmentService.getAllBusinessSegmentsByCompanyId(companyId);
        
        response.setData(businessSegment);
        response.setStatus(ResponseStatus.SUCCESS);
        response.setMessage("Business Segments retrieved!");
       
        return response;
    }

    @GetMapping("/get-all-by-department-id/{departmentId:[\\d]+}")
    public GeneralResponse<BusinessSegmentResponse> getAllBusinessSegmentsByDepartmentId(@PathVariable Long departmentId) throws ResourceNotFoundException {
        GeneralResponse<BusinessSegmentResponse> response = new GeneralResponse<>();

        BusinessSegmentResponse businessSegment = businessSegmentService.getAllBusinessSegmentsByDepartmentId(departmentId);

        response.setData(businessSegment);
        response.setStatus(ResponseStatus.SUCCESS);
        response.setMessage("Business Segments retrieved!");

        return response;
    }

    @DeleteMapping("/delete/{id:[\\d]+}")
    public GeneralResponse<Void> deleteBusinessSegment(@PathVariable Long id) {
        GeneralResponse<Void> response = new GeneralResponse<>();
       
        businessSegmentService.deleteBusinessSegment(id);
        response.setStatus(ResponseStatus.SUCCESS);
        response.setMessage("Business Segment deleted!");
       
        return response;
    }
}
