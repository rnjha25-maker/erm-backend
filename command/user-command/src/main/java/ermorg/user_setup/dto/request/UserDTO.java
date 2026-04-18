package ermorg.user_setup.dto.request;

import java.util.ArrayList;
import java.util.List;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class UserDTO {

	private long userId;
	@NotBlank(message = "First Name is required.")
	private String firstName;
	@NotBlank(message = "Last Name is required.")
	private String lastName;
	@NotBlank(message = "State is required.")
	private long stateId;
	@NotBlank(message = "City is required.")
	private long cityId;
	@NotBlank(message = "Country is required.")
	private long countryId;
	@NotBlank(message = "Email is required.")
	@Email(message="Invalid Email")
	private String email;
	@NotBlank(message = "Branch is required.")
	private long branchId;
	@NotBlank(message = "Department is required.")
	private long departmentId;
	private long organizationId;
	@NotBlank(message = "Company is required.")
	private long componyId;
	private String address;
	private String phone;
	@NotBlank(message = "Role is required.")
	private List<Long> roleIds = new ArrayList<>();
	
}
