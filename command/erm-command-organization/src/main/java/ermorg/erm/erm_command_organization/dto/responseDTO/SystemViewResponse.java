package ermorg.erm.erm_command_organization.dto.responseDTO;

import java.util.List;
import java.util.stream.Collectors;

import ermorg.erm.erm_command_organization.model.SystemView;
import ermorg.erm.erm_command_organization.model.SystemViewField;

import lombok.Data;

@Data
public class SystemViewResponse {
	
	private Long id;
	private String tableName;
	
	private List<SystemViewFieldResponse> systemFields;
	
	public SystemViewResponse(SystemView systemView){
		this.id = systemView.getId();
		this.tableName = systemView.getViewName();
		updateSystemFields(systemView.getFields());
	}

	private void updateSystemFields(List<SystemViewField> fields) {
		
		this.systemFields = fields.stream().map(field -> new SystemViewFieldResponse(field))
				.collect(Collectors.toList());
		
	}

}
