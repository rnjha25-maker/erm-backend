package ermorg.erm.dto.riskDTO;

import lombok.Data;

@Data
public class UserRequestDTO {

	private long id;
	private String firstname;
    private String lastname;
    private String email;
    private String mobileNo;
}
