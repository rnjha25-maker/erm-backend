package com.erm.dto.response;

import java.util.ArrayList;
import java.util.List;

import com.erm.model.Category;

import lombok.Data;

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
		fields.sort((o1, o2) -> o1.getFieldOrder().compareTo(o2.getFieldOrder()));
	}
}
