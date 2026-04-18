package ermorg.org_setup_command.dto.request;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class CompanyDTO {
	private long companyId;
	private String companyName;
	private String logo;
	private long organizationId;
}
