package ermorg.erm.erm_command_organization.dto.requestDTO;

import java.util.List;

import lombok.Data;

@Data
public class RoleMappingRequest {

	private long roleId;
	private List<RoleRightMappingRequest> rightIds;
}
