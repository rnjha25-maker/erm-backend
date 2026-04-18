package ermorg.erm.erm_query_organization.controller;

import ermorg.erm.erm_query_organization.dto.OrganizationDTO;
import ermorg.erm.erm_query_organization.dto.responseDTO.GeneralResponse;
import ermorg.erm.erm_query_organization.dto.responseDTO.ResponseStatus;
import ermorg.erm.erm_query_organization.exception.DataNotFoundException;
import ermorg.erm.erm_query_organization.model.Organization;
import ermorg.erm.erm_query_organization.service.IOrganizationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("organization")
//@CrossOrigin(origins = "*")
public class OrganizationController {
    @Autowired
    private IOrganizationService organizationService;

    @GetMapping("/all")
    public GeneralResponse<List<OrganizationDTO>> getAllOrganizations() {
        GeneralResponse<List<OrganizationDTO>> response = new GeneralResponse<>();
        List<OrganizationDTO> orgList = organizationService.getAllOrganizations();
        response.setData(orgList);
        response.setStatus(ResponseStatus.SUCCESS);
        String message = orgList.isEmpty() ? "No organization found!" : "Found " + orgList.size() + " organizations!";
        response.setMessage(message);
        return response;
    }

    @GetMapping("/{id:[\\d]+}")
    public GeneralResponse<Organization> getOrganizationById(@PathVariable Long id) {
        GeneralResponse<Organization> response = new GeneralResponse<>();
        try{
            Organization org = organizationService.getOrganizationById(id);
            response.setData(org);
            response.setMessage("Organization found!");
            response.setStatus(ResponseStatus.SUCCESS);
        }catch (DataNotFoundException e){
            response.setMessage(e.getMessage());
            response.setStatus(ResponseStatus.FAILED);
        }
        return response;
    }
}
