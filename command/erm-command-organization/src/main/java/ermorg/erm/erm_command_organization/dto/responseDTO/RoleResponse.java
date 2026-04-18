package ermorg.erm.erm_command_organization.dto.responseDTO;

import ermorg.erm.erm_command_organization.model.Role;

import ermorg.erm.erm_command_organization.model.RoleRight;
import lombok.Data;

import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;

@Data
public class RoleResponse {

	private long roleId;
	private String roleName;
	private String description;
	private long priority;
	private Set<RightMappingRespose> rights = new HashSet<>();
	
	public RoleResponse(Role role) {
		this.roleId = role.getId();
		this.roleName = role.getName();
		this.description = role.getDescription();
		this.rights = role.getRoleRights() != null ? role.getRoleRights().stream().map(roleRight -> new RightMappingRespose(roleRight)).collect(Collectors.toSet()) : new HashSet<>();
//		this.priority = role.getPriority();
	}
}
