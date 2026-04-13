package com.erm.dto.response;

import com.erm.model.SystemField;

import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class SystemFieldResponse {

	private Long id;
	private String field;
	
	public SystemFieldResponse(SystemField systemField) {
		this.id = systemField.getId();
		this.field = systemField.getField();
	}
}
