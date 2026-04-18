package ermorg.erm.erm_command_organization.dto.responseDTO;

import ermorg.erm.erm_command_organization.model.CustomViewFields;

import lombok.Data;

@Data
public class CustomViewFieldResponse {

	private Long id;
	private String fieldName;
	private Long systemViewFieldId;
	
	public CustomViewFieldResponse(CustomViewFields customViewField) {
		this.fieldName = customViewField.getFieldName();
		this.systemViewFieldId = customViewField.getSystemViewField().getId();
		this.id = customViewField.getId();
	}
}
