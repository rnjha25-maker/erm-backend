package ermorg.erm.erm_command_organization.model;

import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;

@Entity
@Table(name="otp_storage")
@Getter
@Setter
public class OTPStorage extends BaseModel{

	private String email;
	private String otp;
	private String token;
	private Boolean verified = false;
}
