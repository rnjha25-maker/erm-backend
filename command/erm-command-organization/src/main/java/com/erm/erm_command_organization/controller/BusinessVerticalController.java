package com.erm.erm_command_organization.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.erm.erm_command_organization.dto.requestDTO.BusinessVerticalRequest;
import com.erm.erm_command_organization.dto.responseDTO.BusinessVerticalDto;
import com.erm.erm_command_organization.dto.responseDTO.BusinessVerticalResponse;
import com.erm.erm_command_organization.dto.responseDTO.GeneralResponse;
import com.erm.erm_command_organization.dto.responseDTO.ResponseStatus;
import com.erm.erm_command_organization.exception.ResourceNotFoundException;
import com.erm.erm_command_organization.service.IBusinessVerticalService;

@RestController
@RequestMapping("business-vertical")
//@CrossOrigin
public class BusinessVerticalController {

    @Autowired
    private IBusinessVerticalService businessVerticalService;

    @PostMapping("/create")
    public GeneralResponse<BusinessVerticalResponse> createBusinessVertical(@RequestBody BusinessVerticalRequest request) {
        GeneralResponse<BusinessVerticalResponse> response = new GeneralResponse<>();
        BusinessVerticalResponse businessVertical = businessVerticalService.createBusinessVertical(request);
        response.setData(businessVertical);
        response.setStatus(ResponseStatus.SUCCESS);
        response.setMessage("Business Vertical created!");
        return response;
    }

    @PutMapping("/update")
    public GeneralResponse<BusinessVerticalResponse> updateBusinessVertical(@RequestBody BusinessVerticalRequest request) throws ResourceNotFoundException {
        GeneralResponse<BusinessVerticalResponse> response = new GeneralResponse<>();
        
        BusinessVerticalResponse businessVertical = businessVerticalService.updateBusinessVertical(request);
        response.setData(businessVertical);
        response.setStatus(ResponseStatus.SUCCESS);
        response.setMessage("Business Vertical updated!");
        
        return response;
    }

    @GetMapping("/{businessVerticalId:[\\d]+}")
    public GeneralResponse<BusinessVerticalDto> getBusinessVertical(@PathVariable Long businessVerticalId) throws ResourceNotFoundException {
        GeneralResponse<BusinessVerticalDto> response = new GeneralResponse<>();
       
        BusinessVerticalDto businessVertical = businessVerticalService.getBusinessVertical(businessVerticalId);
        response.setData(businessVertical);
        response.setStatus(ResponseStatus.SUCCESS);
        response.setMessage("Business Vertical retrieved!");
       
        return response;
    }
    
    @GetMapping("/get-all-by-company-id/{companyId:[\\d]+}")
    public GeneralResponse<BusinessVerticalResponse> getAllBusinessVerticalsByCompanyId(@PathVariable Long companyId) throws ResourceNotFoundException {
        GeneralResponse<BusinessVerticalResponse> response = new GeneralResponse<>();
       
        BusinessVerticalResponse businessVertical = businessVerticalService.getAllBusinessVerticalsByCompanyId(companyId);
        
        response.setData(businessVertical);
        response.setStatus(ResponseStatus.SUCCESS);
        response.setMessage("Business Verticals retrieved!");
       
        return response;
    }

    @GetMapping("/get-all-by-department-id/{departmentId:[\\d]+}")
    public GeneralResponse<BusinessVerticalResponse> getAllBusinessVerticalsByDepartmentId(@PathVariable Long departmentId) throws ResourceNotFoundException {
        GeneralResponse<BusinessVerticalResponse> response = new GeneralResponse<>();

        BusinessVerticalResponse businessVertical = businessVerticalService.getAllBusinessVerticalsByDepartmentId(departmentId);

        response.setData(businessVertical);
        response.setStatus(ResponseStatus.SUCCESS);
        response.setMessage("Business Verticals retrieved!");

        return response;
    }

    @GetMapping("/get-all-by-segment-id/{segmentId:[\\d]+}")
    public GeneralResponse<BusinessVerticalResponse> getAllBusinessVerticalsBySegmentId(@PathVariable Long segmentId) throws ResourceNotFoundException {
        GeneralResponse<BusinessVerticalResponse> response = new GeneralResponse<>();

        BusinessVerticalResponse businessVertical = businessVerticalService.getAllBusinessVerticalsByBusinessSegmentId(segmentId);

        response.setData(businessVertical);
        response.setStatus(ResponseStatus.SUCCESS);
        response.setMessage("Business Verticals retrieved!");

        return response;
    }

    @DeleteMapping("/delete/{id:[\\d]+}")
    public GeneralResponse<Void> deleteBusinessVertical(@PathVariable Long id) {
        GeneralResponse<Void> response = new GeneralResponse<>();
       
        businessVerticalService.deleteBusinessVertical(id);
        response.setStatus(ResponseStatus.SUCCESS);
        response.setMessage("Business Vertical deleted!");
       
        return response;
    }
}
