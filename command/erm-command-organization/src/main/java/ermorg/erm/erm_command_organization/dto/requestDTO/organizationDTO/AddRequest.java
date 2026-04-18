package ermorg.erm.erm_command_organization.dto.requestDTO.organizationDTO;

import lombok.Getter;
import lombok.Setter;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;

@Getter
@Setter
public class AddRequest {
    @NotBlank(message="Organization name must be provided.")
    private String name;
    private String orgImgUrl;
//    @NotBlank(message="First Name can not be blank.")
//    private String adminFirstName;
//    @NotBlank(message="Last Name can not be blank.")
//    private String adminLastName;
//    @Pattern( message="Phone number is not valid.", regexp = "^\\+?[1-9]\\d{1,14}$")
//    private String adminPhone;
//    @Email(message="Email is not in valid format.")
//    private String adminEmail;
}
