package ermorg.erm.erm_command_organization.dto.responseDTO;

import java.util.Date;

import ermorg.erm.erm_command_organization.model.Organization;
import ermorg.erm.erm_command_organization.model.history.OrganizationHistory;

import lombok.Data;

@Data
public class OrganizationResponse {
	
	private long organizationId;
	private String name;
	private String pinCode;
	private String businessLocation;
	private Integer adminCount;
	private Integer companyCount;
	private String orgImgUrl;
	private long countryId;
	private String countryName;
	private long stateId;
	private String stateName;
	private long cityId;
	private String cityName;
	private String description;

	private String logo;
	private long planId;
	private String planName;
	private int totalCompanies;
	private boolean isActive;
	private Date createDate;
	
	private String adminFirstName;
	private String adminMiddleName;
	private String adminLastName;
	private String email;
	private String phone;
	private String alternatePhone;
	private String profileImageUrl;
	private String status;
	private String gstNo;
	private String panNo;
	
	private String planCode;
	private String planDescription;
	private long noOfBasicUsers;
	private long noOfAdvancedUsers;
	
	public OrganizationResponse(Organization organization) {
		this.organizationId = organization.getId();
		this.name = organization.getName();
		this.logo = organization.getOrganizationLogoImageUrl();
		this.planId = organization.getPlan() != null ? organization.getPlan().getId() : 0; //organization.getPlan().getId();
		this.planName = organization.getPlan() != null ? organization.getPlan().getPlanName() : ""; //organization.getPlan().getPlanName();
		this.totalCompanies = organization.getCompanyCount() != null ? organization.getCompanyCount() : 0;
		this.isActive = "ACTIVE".equals(organization.getStatus());
		this.createDate = organization.getCreatedAt();
		
		this.pinCode = organization.getPinCode();
		this.businessLocation = organization.getBusinessLocation();
		this.adminCount = organization.getAdminCount() != null ? organization.getAdminCount() : 0;
		this.companyCount = organization.getCompanyCount() != null ? organization.getCompanyCount() : 0;
		this.orgImgUrl = organization.getOrganizationLogoImageUrl();
		this.countryId = organization.getCountry() != null ? organization.getCountry().getId() : 0;
		this.stateId = organization.getState() != null ? organization.getState().getId() : 0;
		this.cityId = organization.getCity() != null ? organization.getCity().getId() : 0; //organization.getCity().getId();
		this.description = organization.getDescription();
		
		this.countryName = organization.getCountry() != null ? organization.getCountry().getName() : "";
		this.stateName = organization.getState() != null ?organization.getState().getName() : "";
		this.cityName = organization.getCity() != null ? organization.getCity().getName() : "";
		
		this.adminFirstName = organization.getPOCPerson() != null ? organization.getPOCPerson().getFirstName() : "";
		this.adminMiddleName = organization.getPOCPerson() != null ? organization.getPOCPerson().getMiddleName() : "";
		this.adminLastName = organization.getPOCPerson() != null ? organization.getPOCPerson().getLastName() : "";
		this.email = organization.getPOCPerson() != null ? organization.getPOCPerson().getEmail() : "";
		this.phone = organization.getPOCPerson() != null ? organization.getPOCPerson().getPhone() : "";
		this.alternatePhone = organization.getPOCPerson() != null ? organization.getPOCPerson().getAlternatePhone() : "";
		this.profileImageUrl = organization.getPOCPerson()!= null ? organization.getPOCPerson().getProfileImageUrl() : "";
		this.status = organization.getStatus();
		this.gstNo = organization.getGstNo();
		this.panNo = organization.getPanNo();
		this.noOfBasicUsers = organization.getNoOfBasicUsers() != null ? organization.getNoOfBasicUsers() : 0;
		this.noOfAdvancedUsers = organization.getNoOfAdvancedUsers() != null ? organization.getNoOfAdvancedUsers() : 0; //organization.getNoOfAdvancedUsers();
		this.businessLocation = organization.getBusinessLocation();
	}
	
	public OrganizationResponse(Organization organization, OrganizationHistory organizationHistory) {
		
		
		this.organizationId = organization.getId();
		this.name = organization.getName();
		this.logo = organization.getOrganizationLogoImageUrl();
		if(organization.getPlan() != null) {
			this.planId =  organization.getPlan().getId();
			this.planName = organization.getPlan().getPlanName();
		}
		
		this.totalCompanies = organization.getCompanyCount() == null ? 0 : organization.getCompanyCount();
		this.isActive = "ACTIVE".equals(organization.getStatus());
		this.createDate = organization.getCreatedAt();
		
		this.pinCode = organization.getPinCode();
		this.businessLocation = organization.getBusinessLocation();
		this.adminCount = organization.getAdminCount();
		this.companyCount = organization.getCompanyCount();
		this.orgImgUrl = organization.getOrganizationLogoImageUrl();
		this.countryId = organization.getCountry().getId();
		if(organization.getState() != null) {
			this.stateId = organization.getState().getId();
			this.stateName = organization.getState().getName();

		}
		if(organization.getCity() != null) {
			this.cityName = organization.getCity().getName();

		}
		this.cityId = organization.getCity() != null ? organization.getCity().getId() : 0;
		this.description = organization.getDescription();
		
		this.countryName = organization.getCountry() != null ? organization.getCountry().getName() : "";
		
		if(organization.getPOCPerson() != null) {
			this.adminFirstName =  organization.getPOCPerson().getFirstName();
			this.adminMiddleName = organization.getPOCPerson().getMiddleName();
			this.adminLastName = organization.getPOCPerson().getLastName();
			this.email = organization.getPOCPerson().getEmail();
			this.phone = organization.getPOCPerson().getPhone();
			this.alternatePhone = organization.getPOCPerson().getAlternatePhone();
			this.profileImageUrl = organization.getPOCPerson().getProfileImageUrl();
		}
		
		this.status = organization.getStatus();
		this.gstNo = organization.getGstNo();
		this.panNo = organization.getPanNo();
		
		if(organizationHistory != null) {
			if(organizationHistory.getPlan() != null) {
				this.planDescription = organizationHistory.getPlan().getPlanDescription();
				this.planId = organizationHistory.getPlan().getId();
				this.planName = organizationHistory.getPlan().getPlanName();
			}
			this.status = organizationHistory.getStatus();
			
		}
		
	}

}
