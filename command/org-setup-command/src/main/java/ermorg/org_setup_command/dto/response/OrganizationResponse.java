package ermorg.org_setup_command.dto.response;

import java.util.List;
import java.util.stream.Collectors;

import ermorg.org_setup_command.modal.Organization;
import ermorg.org_setup_command.modal.CustomField;
import ermorg.org_setup_command.modal.Modules;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class OrganizationResponse {
	private long orgId;
	private String orgName;
	private List<CompanyResponse> companies;
	private List<ModuleResponse> modules;
	
	
	public OrganizationResponse(Organization organization, List<CompanyResponse> companies){
		
		this.orgId = organization.getId();
		this.orgName = organization.getName();
		this.companies = companies;		
//		setOrgmodules(organization.getModules());
	}


	private void setOrgmodules(List<Modules> modules2) {
	List<ModuleResponse> moduleResponse = modules2.stream()
	.map(module -> new ModuleResponse(module))
	.collect(Collectors.toList());
	
	this.setModules(moduleResponse);
		
	}
	

}
