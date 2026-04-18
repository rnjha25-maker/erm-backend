package ermorg.org_setup_command.dto.response;

import ermorg.org_setup_command.modal.CustomField;

import lombok.Data;

@Data
public class CustomFieldResponse {
	
	private String fieldName;
	private String fieldType;
	private Boolean required = false;
	private String mappedWith;
	
	public CustomFieldResponse(CustomField customField) {
		
		this.fieldName = customField.getFieldName();
		this.fieldType = customField.getFieldType();
		this.required = customField.getRequired();
		this.mappedWith = customField.getMappedWith();
				
		
	}

}
