package ermorg.erm.erm_command_organization.dto.responseDTO;

import ermorg.erm.erm_command_organization.model.Company;

import lombok.Data;

@Data
public class CompanyResponse {
	
	private long companyId;
	private String companyName;
	private String companySite;
	private long countryId;
	private long stateId;
	private long cityId;
	private String pincode;
	private String address;
	private String companyLogoImageUrl;
	private String status;
	private String createdDate;
	private long organizationId;
	private String organizationName;
	
	public CompanyResponse(Company company) {
		this.companyId = company.getId();
		this.companyName = company.getName();
		this.companySite = company.getCompanySite();
		this.countryId = company.getCountry().getId();
		this.stateId = company.getState().getId();
		this.cityId = company.getCity().getId();
		this.pincode = company.getPincode();
		this.address = company.getAddress();
		this.companyLogoImageUrl = company.getCompanyLogoImageUrl();
		this.status = company.getStatus();
		this.createdDate = company.getCreatedAt().toString();
		this.organizationId = company.getOrganization().getId();
		this.organizationName = company.getOrganization().getName();
	}

}
