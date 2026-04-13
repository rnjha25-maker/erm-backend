package com.erm.dto.response;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import com.erm.model.Category;
import com.erm.model.CustomField;

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
