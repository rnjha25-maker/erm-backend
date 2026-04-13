package com.erm.erm_command_organization.dto.responseDTO;

import java.util.ArrayList;
import java.util.List;

import com.erm.erm_command_organization.model.Modules;

import lombok.Data;

@Data
public class ModuleListResponse {

	private long moduleId;
	private String moduleName;
	List<CategoryListResponse> categories = new ArrayList<>();
	List<ViewCategoryResponse> viewCategories = new ArrayList<>();
	List<RightResponse> rights = new ArrayList<>();
	public ModuleListResponse(Modules module){
		this.moduleId = module.getId();
		this.moduleName = module.getName();
		module.getCategories().stream()
		.filter(filter -> !filter.getDeleted())
		.forEach(category-> categories.add(new CategoryListResponse(category)));
		
		module.getViewCategories().stream()
		.filter(filter -> !filter.getDeleted())
		.forEach(category-> viewCategories.add(new ViewCategoryResponse(category)));
		
		module.getRights().stream()
		.filter(filter -> !filter.getDeleted())
		.forEach(right-> rights.add(new RightResponse(right)));
	}
}
