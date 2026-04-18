package ermorg.user_setup.dto.request;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class RoleDTO {
	private long organizationId;
	private long roleId;
	private String roleName;
	private long companyId;
	private String description;
	
}
