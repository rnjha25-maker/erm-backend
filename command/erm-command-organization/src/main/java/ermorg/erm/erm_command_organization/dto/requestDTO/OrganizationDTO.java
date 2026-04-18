package ermorg.erm.erm_command_organization.dto.requestDTO;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class OrganizationDTO {

	private long organizationId;
	@NotBlank(message = "Organization name must be provided.")
	private String name;
	@NotBlank(message = "Pin code must be provided.")
	private String pinCode;
	@NotBlank(message = "Business location must be provided.")
	private String businessLocation;
	@NotNull(message = "Admin count must be provided.")
	private Integer adminCount;
	@NotNull(message = "Company count must be provided.")
	private Integer companyCount;
	private String orgImgUrl;
	@NotNull(message="Country must be selected.")
	private long countryId;
	@Min(value=1, message="State must be selected.")
	@NotNull(message="Please select state.")
	private long stateId;
	
	@Min(value=1, message="City must be selected.")
	@NotNull(message="Please select city ")
	private long cityId;
	private String description;

	@NotBlank(message="Admin First Name is required.")
	private String adminFirstName;
	
	private String adminMiddleName;
	
	private String adminLastName;
	
	@NotBlank(message="Email is required.")
	@Email(message="Email is not in valid format.")
	private String email;
	
	@Size(min = 10, max = 10, message = "Phone number must be 10 characters long")
	@Pattern(regexp = "^[0-9]+$", message = "Phone number must contain only numbers")
	@NotBlank(message="Phone number is required.")
	private String phone;
	
	@Size(min = 10, max = 10, message = "Alternate Phone number must be 10 characters long")
	@Pattern(regexp = "^[0-9]+$", message = "Alternate Phone number must contain only numbers")
	private String alternatePhone;
	private String profileImageUrl;
	private boolean adminPoc;
	@NotNull(message = "Plan is required.")
	@Min(value = 1, message = "Plan is required.")
	private long planId;
	
	private String status;
	
	@NotBlank(message="GST number is required.")
	private String gstNo;
	
	@NotBlank(message="PAN number is required.")
	private String panNo;
	
	@NotNull(message="Number of basic users is required.")
	private long noOfBasicUsers;
	
	@NotNull(message="Number of advanced users is required.")
	private long noOfAdvancedUsers;
	private String logoId;
	

}
