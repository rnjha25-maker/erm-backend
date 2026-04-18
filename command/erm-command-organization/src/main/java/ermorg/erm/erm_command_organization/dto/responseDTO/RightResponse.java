package ermorg.erm.erm_command_organization.dto.responseDTO;

import ermorg.erm.erm_command_organization.model.Right;

import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class RightResponse {

	private String rightName;
	private String rightDescription;
	private long rightId;
	private long moduleId;
	
	public RightResponse(Right right) {
		this.rightName = right.getName();
		this.rightDescription = right.getDescription();
		this.rightId = right.getId();
		this.moduleId = right.getModule().getId();
		
	}
}
