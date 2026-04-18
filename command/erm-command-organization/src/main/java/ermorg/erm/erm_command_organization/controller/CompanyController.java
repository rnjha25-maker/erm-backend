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

import ermorg.erm.erm_command_organization.dto.requestDTO.companyDTO.AddRequest;
import ermorg.erm.erm_command_organization.dto.responseDTO.CompanyResponse;
import ermorg.erm.erm_command_organization.dto.responseDTO.GeneralResponse;
import ermorg.erm.erm_command_organization.dto.responseDTO.ResponseStatus;
import ermorg.erm.erm_command_organization.exception.DataNotFoundException;
import ermorg.erm.erm_command_organization.exception.InvalidDataException;
import ermorg.erm.erm_command_organization.exception.LimitExceedException;
import ermorg.erm.erm_command_organization.exception.ResourceNotFoundException;
import ermorg.erm.erm_command_organization.model.Company;
import ermorg.erm.erm_command_organization.service.ICompanyService;

@RestController
@RequestMapping("company")
//@CrossOrigin
public class CompanyController {

    @Autowired
    private ICompanyService companyService;

    @PostMapping("/create")
    public GeneralResponse<CompanyResponse> createCompany(@RequestBody AddRequest request) throws ResourceNotFoundException, LimitExceedException {
        GeneralResponse<CompanyResponse> response = new GeneralResponse<>();
        CompanyResponse company = companyService.createCompany(request);
        response.setData(company);
        response.setStatus(ResponseStatus.SUCCESS);
        response.setMessage("Company created!");
        return response;
    }

    @PutMapping("/update")
    public GeneralResponse<CompanyResponse> updateCompany(@RequestBody AddRequest request) throws ResourceNotFoundException {
        GeneralResponse<CompanyResponse> response = new GeneralResponse<>();
        try{
        	CompanyResponse company = companyService.updateCompany(request);
            response.setData(company);
            response.setStatus(ResponseStatus.SUCCESS);
            response.setMessage("Company updated!");
        }catch (DataNotFoundException e) {
            response.setStatus(ResponseStatus.FAILED);
            response.setMessage(e.getMessage());
        }
        return response;
    }

    @GetMapping("/{companyId:[\\d]+}")
    public GeneralResponse<CompanyResponse> getCompany(@PathVariable Long companyId) throws ResourceNotFoundException {
        GeneralResponse<CompanyResponse> response = new GeneralResponse<>();
        try{
        	CompanyResponse company = companyService.getCompany(companyId);
        	
        	response.setData(company);
            response.setStatus(ResponseStatus.SUCCESS);
            response.setMessage("Company deleted!");	
        }catch (InvalidDataException e){
            response.setStatus(ResponseStatus.FAILED);
            response.setMessage(e.getMessage());
        }
        return response;
    }
    
    @GetMapping("/all/{organizationId:[\\d]+}")
    public GeneralResponse<List<CompanyResponse>> getAllCompanies(@PathVariable Long organizationId) throws ResourceNotFoundException {
        GeneralResponse<List<CompanyResponse>> response = new GeneralResponse<>();
        try{
        	List<CompanyResponse> companies = companyService.getAllCompanies(organizationId);
        	
        	response.setData(companies);
            response.setStatus(ResponseStatus.SUCCESS);
            response.setMessage("Company deleted!");	
        }catch (InvalidDataException e){
            response.setStatus(ResponseStatus.FAILED);
            response.setMessage(e.getMessage());
        }
        return response;
    }
    
    @DeleteMapping("/{id:[\\d]+}")
    public GeneralResponse<Company> deleteCompany(@PathVariable Long id) throws ResourceNotFoundException {
        GeneralResponse<Company> response = new GeneralResponse<>();
        try{
            companyService.deleteCompany(id);
            response.setStatus(ResponseStatus.SUCCESS);
            response.setMessage("Company deleted!");	
        }catch (InvalidDataException e){
            response.setStatus(ResponseStatus.FAILED);
            response.setMessage(e.getMessage());
        }
        return response;
    }
}
