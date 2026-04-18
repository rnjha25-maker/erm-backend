package ermorg.erm.erm_command_organization.controller;

import java.util.List;

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

import ermorg.erm.erm_command_organization.dto.request.UpdateModuleRequest;
import ermorg.erm.erm_command_organization.dto.requestDTO.ModuleRightRequest;
import ermorg.erm.erm_command_organization.dto.requestDTO.OrganizationDTO;
import ermorg.erm.erm_command_organization.dto.responseDTO.GeneralResponse;
import ermorg.erm.erm_command_organization.dto.responseDTO.OrganizationResponse;
import ermorg.erm.erm_command_organization.dto.responseDTO.ResponseStatus;
import ermorg.erm.erm_command_organization.exception.DataNotFoundException;
import ermorg.erm.erm_command_organization.exception.InvalidDataException;
import ermorg.erm.erm_command_organization.exception.ResourceNotFoundException;
import ermorg.erm.erm_command_organization.service.IOrganizationService;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;

@RestController
@RequestMapping("organization")
//@CrossOrigin
public class OrganizationController {
    @Autowired
    private IOrganizationService organizationService;

    @Autowired
    private HttpServletRequest httpRequest;
    
    @PostMapping("/create")
    public GeneralResponse<OrganizationResponse> createOrganization(@Valid @RequestBody OrganizationDTO request) throws ResourceNotFoundException {
        GeneralResponse<OrganizationResponse> response = new GeneralResponse<>();
        
        String userId = httpRequest.getHeader("X-User-Id");
        
        OrganizationResponse organization = organizationService.createOrganization(request);
        response.setData(organization);
        response.setStatus(ResponseStatus.SUCCESS);
        response.setMessage("Organization created!");
        return response;
    }

    @PutMapping("/update")
    public GeneralResponse<OrganizationResponse> updateOrganization(@RequestBody OrganizationDTO request) {
        GeneralResponse<OrganizationResponse> response = new GeneralResponse<>();
        try{
        	OrganizationResponse organization = organizationService.updateOrganization(request);
            response.setData(organization);
            response.setStatus(ResponseStatus.SUCCESS);
            response.setMessage("Organization updated!");
        }catch (DataNotFoundException e){
            response.setMessage(e.getMessage());
            response.setStatus(ResponseStatus.FAILED);
        }
        return response;
    }

    @DeleteMapping("/delete/{id:[\\d]+}")
    public GeneralResponse<Void> deleteOrganization(@PathVariable Long id) {
        GeneralResponse<Void> response = new GeneralResponse<>();
        try{
            organizationService.deleteOrganization(id);
            response.setStatus(ResponseStatus.SUCCESS);
            response.setMessage("Organization deleted!");
        } catch (InvalidDataException e) {
            response.setStatus(ResponseStatus.FAILED);
            response.setMessage(e.getMessage());
        }
        return response;
    }
    
    @GetMapping("/{id:[\\d]+}/{back:[\\d]+}")
    public GeneralResponse<OrganizationResponse> getOrganization(@PathVariable Long id, @PathVariable int back) {
        GeneralResponse<OrganizationResponse> response = new GeneralResponse<>();
        try{
        	OrganizationResponse organization = organizationService.getOrganization(id, back);
        	response.setData(organization);
            response.setStatus(ResponseStatus.SUCCESS);
            response.setMessage("Organization deleted!");
        } catch (InvalidDataException e) {
            response.setStatus(ResponseStatus.FAILED);
            response.setMessage(e.getMessage());
        }
        return response;
    }
    
    @GetMapping("/all")
    public GeneralResponse<List<OrganizationResponse>> getAllOrganization() {
        GeneralResponse<List<OrganizationResponse>> response = new GeneralResponse<>();
        try{
        	List<OrganizationResponse> organizations = organizationService.getAllOrganization();
        	response.setData(organizations);
            response.setStatus(ResponseStatus.SUCCESS);
            response.setMessage("Organization deleted!");
        } catch (InvalidDataException e) {
            response.setStatus(ResponseStatus.FAILED);
            response.setMessage(e.getMessage());
        }
        return response;
    }
    
    @PostMapping("/update-module")
    public GeneralResponse<UpdateModuleRequest> updateModule(@Valid @RequestBody UpdateModuleRequest request) throws ResourceNotFoundException {
        GeneralResponse<UpdateModuleRequest> response = new GeneralResponse<>();
        
        String userId = httpRequest.getHeader("X-User-Id");
        
        UpdateModuleRequest updateModuleRequest = organizationService.updateModule(request);
        response.setData(updateModuleRequest);
        response.setStatus(ResponseStatus.SUCCESS);
        response.setMessage("Organization created!");
        return response;
    }
    
    @PostMapping("/update-module-view")
    public GeneralResponse<UpdateModuleRequest> updateModuleView(@Valid @RequestBody UpdateModuleRequest request) throws ResourceNotFoundException {
        GeneralResponse<UpdateModuleRequest> response = new GeneralResponse<>();
        
        String userId = httpRequest.getHeader("X-User-Id");
        
        UpdateModuleRequest updateModuleRequest = organizationService.updateModuleView(request);
        response.setData(updateModuleRequest);
        response.setStatus(ResponseStatus.SUCCESS);
        response.setMessage("Organization created!");
        return response;
    }
    
    @PostMapping("/update-right")
    public GeneralResponse<ModuleRightRequest> updateRight(@RequestBody ModuleRightRequest request) throws ResourceNotFoundException {
        GeneralResponse<ModuleRightRequest> response = new GeneralResponse<>();
        
        String userId = httpRequest.getHeader("X-User-Id");
        
        ModuleRightRequest updateModuleRequest = organizationService.updateRight(request);
        response.setData(updateModuleRequest);
        response.setStatus(ResponseStatus.SUCCESS);
        response.setMessage("Organization created!");
        return response;
    }
    
  
    
    
}
