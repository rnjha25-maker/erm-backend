package ermorg.erm.erm_command_organization.dto.responseDTO;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import ermorg.erm.erm_command_organization.model.CustomViewFields;
import ermorg.erm.erm_command_organization.model.ViewCategory;

import lombok.Data;

@Data
public class ViewCategoryResponse {
	
	private long viewCategoryId;
	private String ViewCategoryName;
	private String mappedWithView;
	private long fieldCount;
	
	private List<CustomViewFieldResponse> fields = new ArrayList<>();
	
	public ViewCategoryResponse(ViewCategory category) {
		this.viewCategoryId = category.getId();
		this.ViewCategoryName = category.getCategoryName();
		this.mappedWithView = category.getMappedWithTable();
		this.fieldCount = category.getFields().size();
		category.getFields().forEach(field -> this.fields.add(new CustomViewFieldResponse(field)));
	}
	
	public void setCustomFields(List<CustomViewFields> customFields) {
		
		this.fields = customFields.stream().map(field -> new CustomViewFieldResponse(field))
		.collect(Collectors.toList());
		
	};

}
