package com.erm.erm_command_organization.dto.responseDTO;

import java.util.List;
import java.util.stream.Collectors;

import com.erm.erm_command_organization.model.SystemField;
import com.erm.erm_command_organization.model.SystemTable;

import lombok.Data;

@Data
public class SystemTableResponse {

	private Long id;
	private String tableName;
	
	private List<SystemFieldResponse> systemFields;
	
	public SystemTableResponse(SystemTable systemTable){
		this.id = systemTable.getId();
		this.tableName = systemTable.getTableName();
		updateSystemFields(systemTable.getFields());
	}

	private void updateSystemFields(List<SystemField> fields) {
		
		this.systemFields = fields.stream().map(field -> new SystemFieldResponse(field))
				.collect(Collectors.toList());
		
	}
	
	
	
}
