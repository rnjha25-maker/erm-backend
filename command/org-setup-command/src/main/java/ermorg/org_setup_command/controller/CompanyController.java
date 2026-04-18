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

import ermorg.org_setup_command.dto.request.CompanyDTO;
import ermorg.org_setup_command.dto.response.CompanyResponse;
import ermorg.org_setup_command.dto.response.GeneralResponse;
import ermorg.org_setup_command.dto.response.ResponseStatus;
import ermorg.org_setup_command.exception.ResourceNotFoundException;
import ermorg.org_setup_command.service.ICompanyService;

@RestController
@RequestMapping("company")
public class CompanyController {

    @Autowired
    private ICompanyService companyService;

    @PostMapping("/create")
    public GeneralResponse<CompanyResponse> createCompany(@RequestBody CompanyDTO request) throws ResourceNotFoundException {
        GeneralResponse<CompanyResponse> response = new GeneralResponse<>();
        CompanyResponse company = companyService.createCompany(request);
       
        response.setData(company);
        response.setStatus(ResponseStatus.SUCCESS);
        response.setMessage("Company created!");
        return response;
    }

    @PutMapping("/update")
    public GeneralResponse<CompanyResponse> updateCompany(@RequestBody CompanyDTO request) throws ResourceNotFoundException {
        GeneralResponse<CompanyResponse> response = new GeneralResponse<>();
        
            CompanyResponse company = companyService.updateCompany(request);
            response.setData(company);
            response.setStatus(ResponseStatus.SUCCESS);
            response.setMessage("Company updated!");
       
        return response;
    }

    @DeleteMapping("/delete/{id:[\\d]+}")
    public GeneralResponse<CompanyResponse> deleteCompany(@PathVariable Long id) throws ResourceNotFoundException {
        GeneralResponse<CompanyResponse> response = new GeneralResponse<>();
      
            companyService.deleteCompany(id);
            response.setStatus(ResponseStatus.SUCCESS);
            response.setMessage("Company deleted!");
       
        return response;
    }
    
    @GetMapping("/{id:[\\d]+}")
    public GeneralResponse<CompanyResponse> getCompany(@PathVariable Long id) throws ResourceNotFoundException {
        GeneralResponse<CompanyResponse> response = new GeneralResponse<>();
      
        CompanyResponse company = companyService.getCompany(id);
        	response.setData(company);
            response.setStatus(ResponseStatus.SUCCESS);
            response.setMessage("Company deleted!");
       
        return response;
    }
    
    @GetMapping("/all/{id:[\\d]+}")
    public GeneralResponse<List<CompanyResponse>> getAll(@PathVariable Long id) throws ResourceNotFoundException {
        GeneralResponse<List<CompanyResponse>> response = new GeneralResponse<>();
      
        List<CompanyResponse> company = companyService.getAllCompany(id);
        	response.setData(company);
            response.setStatus(ResponseStatus.SUCCESS);
            response.setMessage("Company deleted!");
       
        return response;
    }
    
}
