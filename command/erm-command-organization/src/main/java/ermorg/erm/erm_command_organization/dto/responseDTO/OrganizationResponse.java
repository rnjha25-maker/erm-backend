package ermorg.erm.erm_command_organization.dto.responseDTO;

import java.util.Date;

import ermorg.erm.erm_command_organization.model.City;
import ermorg.erm.erm_command_organization.model.Country;
import ermorg.erm.erm_command_organization.model.Organization;
import ermorg.erm.erm_command_organization.model.Plan;
import ermorg.erm.erm_command_organization.model.State;
import ermorg.erm.erm_command_organization.model.UserDetail;
import ermorg.erm.erm_command_organization.model.history.OrganizationHistory;
import ermorg.erm.erm_command_organization.model.history.PlanHistory;
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
	        // Extract repeated null-checked references into local variables
	        Plan plan           = organization.getPlan();
	        Country country     = organization.getCountry();
	        State state         = organization.getState();
	        City city           = organization.getCity();
	        UserDetail poc      = organization.getPOCPerson();

	        this.organizationId     = organization.getId();
	        this.name               = organization.getName();
	        this.logo               = organization.getOrganizationLogoImageUrl(); // ✅ removed duplicate orgImgUrl assignment
	        this.orgImgUrl          = this.logo;
	        this.description        = organization.getDescription();
	        this.pinCode            = organization.getPinCode();
	        this.businessLocation   = organization.getBusinessLocation();         // ✅ removed duplicate assignment
	        this.status             = organization.getStatus();
	        this.gstNo              = organization.getGstNo();
	        this.panNo              = organization.getPanNo();
	        this.adminCount         = organization.getAdminCount()   != null ? organization.getAdminCount()   : 0;
	        this.companyCount       = organization.getCompanyCount() != null ? organization.getCompanyCount() : 0;
	        this.totalCompanies     = this.companyCount;
	        this.isActive           = "ACTIVE".equals(organization.getStatus());
	        this.createDate         = organization.getCreatedAt();
	        this.noOfBasicUsers     = organization.getNoOfBasicUsers()    != null ? organization.getNoOfBasicUsers()    : 0;
	        this.noOfAdvancedUsers  = organization.getNoOfAdvancedUsers() != null ? organization.getNoOfAdvancedUsers() : 0;

	        // Plan
	        if (plan != null) {
	            this.planId          = plan.getId();
	            this.planName        = plan.getPlanName();
	            this.planDescription = plan.getPlanDescription(); // ✅ was never set
	        }

	        // Location
	        if (country != null) { this.countryId = country.getId(); this.countryName = country.getName(); }
	        if (state   != null) { this.stateId   = state.getId();   this.stateName   = state.getName();   }
	        if (city    != null) { this.cityId    = city.getId();    this.cityName    = city.getName();    }

	        // POC Person
	        if (poc != null) {
	            this.adminFirstName   = poc.getFirstName();
	            this.adminMiddleName  = poc.getMiddleName();
	            this.adminLastName    = poc.getLastName();
	            this.email            = poc.getEmail();
	            this.phone            = poc.getPhone();
	            this.alternatePhone   = poc.getAlternatePhone();
	            this.profileImageUrl  = poc.getProfileImageUrl();
	        }
	    }

	    public OrganizationResponse(Organization organization, OrganizationHistory organizationHistory) {
	        // Reuse the first constructor to avoid duplication
	        this(organization);

	        // ✅ noOfBasicUsers / noOfAdvancedUsers now set via this(organization)

	        // Override with history values if present
	        if (organizationHistory != null) {
	        	PlanHistory historyPlan = organizationHistory.getPlan();
	            if (historyPlan != null) {
	                this.planId          = historyPlan.getId();
	                this.planName        = historyPlan.getPlanName();
	                this.planDescription = historyPlan.getPlanDescription();
	            }
	            this.status = organizationHistory.getStatus();
	        }
	    }
	} 
