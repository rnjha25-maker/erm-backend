package ermorg.erm.erm_api_gateway.dto.response;

import java.util.List;
import java.util.Objects;

import ermorg.erm.erm_api_gateway.model.Role;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class RoleResponse {
	private String name;
	private String description;
	private Long roleId;

	private List<RightResponse> rights;

	public RoleResponse(Role role, List<RightResponse> rights) {

		this.name = role.getName();
		this.description = role.getDescription();
		this.rights = rights;
		this.roleId = role.getId();
	}

	public RoleResponse(Role role) {

		this.name = role.getName();
		this.description = role.getDescription();
		this.roleId = role.getId();
	}

	@Override
	public boolean equals(Object o) {
		if (o == null || getClass() != o.getClass())
			return false;
		RoleResponse that = (RoleResponse) o;
		return Objects.equals(name, that.name) && Objects.equals(description, that.description)
				&& Objects.equals(roleId, that.roleId);
	}

	@Override
	public int hashCode() {
		return Objects.hash(name, description, roleId);
	}
}
