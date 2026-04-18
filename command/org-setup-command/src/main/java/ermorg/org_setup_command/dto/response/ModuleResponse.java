package ermorg.org_setup_command.dto.response;

import java.util.List;
import java.util.stream.Collectors;

import ermorg.org_setup_command.modal.Category;
import ermorg.org_setup_command.modal.Modules;

import lombok.Data;

@Data
public class ModuleResponse {
	
	private long moduleId;
	private String moduleName;
	private List<CategoryResponse> categories;
	
	
	public ModuleResponse(Modules module) {
		this.moduleId = module.getId();
		this.moduleName = module.getName();
	}
	
	public void updateCategories(List<Category> categories) {
		this.categories = categories.stream().map(category -> new CategoryResponse(category))
		.collect(Collectors.toList());
	}


	
	

}
