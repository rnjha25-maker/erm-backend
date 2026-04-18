package ermorg.erm.erm_command_organization.dto.responseDTO;

import java.util.ArrayList;
import java.util.List;

import ermorg.erm.erm_command_organization.model.Category;

import ermorg.erm.erm_command_organization.model.Fields;
import lombok.Data;

import java.util.List;

@Data
public class CategoryListResponse {

	private long categoryId;
	private String categoryName;
	private int fieldCount;
	private String mappedWithTable;
	private List<CustomFieldResponse> fields = new ArrayList<>();
	
	public CategoryListResponse(Category category){
		this.categoryId = category.getId();
		this.categoryName = category.getCategoryName();
		this.fieldCount = category.getFields().size();
		this.mappedWithTable = category.getMappedWithTable();
		category.getFields().forEach(field -> this.fields.add(new CustomFieldResponse(field)));
	}
}
