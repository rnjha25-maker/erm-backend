package ermorg.erm.erm_command_organization.dto.responseDTO;

import java.util.ArrayList;
import java.util.List;

import ermorg.erm.erm_command_organization.model.RoleRight;

import lombok.Data;

@Data
public class RoleRightMappingResponse {

	private long roleId;
	List<RightMappingRespose> rightMapings = new ArrayList<>();
	
	public RoleRightMappingResponse(List<RoleRight> roleRights, long roleId) {
		
		roleRights.forEach(roleRight->rightMapings.add(new RightMappingRespose(roleRight)));
		this.roleId = roleId;
	}
}
