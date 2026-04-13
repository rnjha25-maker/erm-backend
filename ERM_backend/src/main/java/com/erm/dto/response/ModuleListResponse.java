package com.erm.dto.response;

import com.erm.model.Modules;

import lombok.Data;

@Data
public class ModuleListResponse {

	private long moduleId;
	private String moduleName;
	
	public ModuleListResponse(Modules module){
		this.moduleId = module.getId();
		this.moduleName = module.getName();
	}
}
