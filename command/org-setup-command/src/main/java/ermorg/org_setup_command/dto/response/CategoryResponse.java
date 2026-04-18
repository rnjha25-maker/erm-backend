package ermorg.org_setup_command.dto.response;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import ermorg.org_setup_command.modal.Category;
import ermorg.org_setup_command.modal.CustomField;

import lombok.Data;

@Data
public class CategoryResponse {

	private long categoryId;
	private String categoryName;
	
	private List<CustomFieldResponse> fields = new ArrayList<>();
	
	public CategoryResponse(Category category) {
		this.categoryId = category.getId();
		this.categoryName = category.getCategoryName();
	}
	
	public void setCustomFields(List<CustomField> customFields) {
		
		this.fields = customFields.stream().map(field -> new CustomFieldResponse(field))
		.collect(Collectors.toList());
		
	};
	
}
