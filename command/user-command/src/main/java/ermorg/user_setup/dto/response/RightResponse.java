package ermorg.user_setup.dto.response;

import ermorg.user_setup.modal.RoleRight;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class RightResponse {
	private Long rightId;
	private String name;
    private String description;
    private Boolean create;
    private Boolean update;
    private Boolean delete;
	public RightResponse(RoleRight roleRight) {
		this.rightId = roleRight.getId();
		this.name = roleRight.getRight().getName();
		this.description = roleRight.getRight().getDescription();
		this.create = roleRight.getCreate();
		this.update = roleRight.getUpdate();
		this.delete = roleRight.getUpdate();
	}
    
    

}
