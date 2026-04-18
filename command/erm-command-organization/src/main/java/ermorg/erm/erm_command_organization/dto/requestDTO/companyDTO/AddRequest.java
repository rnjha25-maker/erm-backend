package ermorg.erm.erm_command_organization.dto.requestDTO.companyDTO;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class AddRequest {
	private long companyId;
	private long organizationId;
    private String companyName;
    private String companySite;
    private long countryId;
    private long stateId;
    private long cityId;
    private String pincode;
    private String address;
    private String companyLogoImageUrl;
}
