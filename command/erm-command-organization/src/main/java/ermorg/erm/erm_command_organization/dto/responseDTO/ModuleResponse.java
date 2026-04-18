package ermorg.erm.erm_command_organization.dto.responseDTO;

import java.util.List;
import java.util.stream.Collectors;

import ermorg.erm.erm_command_organization.model.Category;
import ermorg.erm.erm_command_organization.model.Modules;
import ermorg.erm.erm_command_organization.model.ViewCategory;

import lombok.Data;

@Data
public class ModuleResponse {
	
	private long moduleId;
	private String moduleName;
	private List<CategoryResponse> categories;
	private List<ViewCategoryResponse> viewCategories; 
	
	
	public ModuleResponse(Modules module) {
		this.moduleId = module.getId();
		this.moduleName = module.getName();
		updateCategories(module.getCategories());
		updateViewCategories(module.getViewCategories());
	}
	
	public void updateCategories(List<Category> categories) {
		this.categories = categories.stream().map(category -> new CategoryResponse(category))
		.collect(Collectors.toList());
	}

	public void updateViewCategories(List<ViewCategory> categories) {
		this.viewCategories = categories.stream().map(category -> new ViewCategoryResponse(category))
		.collect(Collectors.toList());
	}

	
	

}
