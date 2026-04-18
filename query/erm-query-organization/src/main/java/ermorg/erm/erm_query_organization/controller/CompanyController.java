package ermorg.erm.erm_query_organization.controller;

import ermorg.erm.erm_query_organization.dto.responseDTO.GeneralResponse;
import ermorg.erm.erm_query_organization.dto.responseDTO.ResponseStatus;
import ermorg.erm.erm_query_organization.exception.DataNotFoundException;
import ermorg.erm.erm_query_organization.model.Company;
import ermorg.erm.erm_query_organization.service.ICompanyService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("company")
public class CompanyController {

    @Autowired
    private ICompanyService companyService;

    @GetMapping("/all")
    public GeneralResponse<List<Company>> getAllCompany() {
        GeneralResponse<List<Company>> response = new GeneralResponse<>();
        List<Company> companyList = companyService.getAllCompanies();
        response.setData(companyList);
        response.setStatus(ResponseStatus.SUCCESS);
        String message = companyList.isEmpty() ? "No company found!" : "Found " + companyList.size() + " companies";
        response.setMessage(message);
        return response;
    }

    @GetMapping("/{id:[\\d]+}")
    public GeneralResponse<Company> getCompanyById(@PathVariable Long id) {
        GeneralResponse<Company> response = new GeneralResponse<>();
        try{
            Company company = companyService.getCompanyById(id);
            response.setData(company);
            response.setStatus(ResponseStatus.SUCCESS);
            response.setMessage("Company found!");
        }catch (DataNotFoundException e) {
            response.setStatus(ResponseStatus.FAILED);
            response.setMessage(e.getMessage());
        }
        return response;
    }
}
