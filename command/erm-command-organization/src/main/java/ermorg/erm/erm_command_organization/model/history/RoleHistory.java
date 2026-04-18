package ermorg.erm.erm_command_organization.model.history;

import ermorg.erm.erm_command_organization.model.BaseModel;

import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;

@Entity
@Getter
@Setter
@Table(name = "role_history")
public class RoleHistory extends BaseModel {
	
	private Long roleId;
	private String roleName;
	private Long priority;
	private String description;
	private String operation;

}
