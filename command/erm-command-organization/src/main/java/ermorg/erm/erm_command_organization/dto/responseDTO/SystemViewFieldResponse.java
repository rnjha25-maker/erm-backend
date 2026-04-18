package ermorg.erm.erm_command_organization.dto.responseDTO;

import ermorg.erm.erm_command_organization.model.SystemViewField;

import lombok.Data;

@Data
public class SystemViewFieldResponse {
	
	private Long id;
	private String systemViewField;
	
	public SystemViewFieldResponse(SystemViewField field){
		this.id = field.getId();
		this.systemViewField = field.getField();
	}

}
