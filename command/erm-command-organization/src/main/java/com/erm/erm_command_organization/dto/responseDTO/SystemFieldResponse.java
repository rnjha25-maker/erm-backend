package com.erm.erm_command_organization.dto.responseDTO;

import com.erm.erm_command_organization.model.SystemField;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class SystemFieldResponse {

	private Long id;
	private String systemField;
	
	public SystemFieldResponse(SystemField field){
		this.id = field.getId();
		this.systemField = field.getField();
	}
	
}
