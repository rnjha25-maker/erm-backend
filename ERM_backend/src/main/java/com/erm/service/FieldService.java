package com.erm.service;

import java.util.List;

import com.erm.dto.response.CategoryListResponse;
import com.erm.dto.response.CategoryResponse;
import com.erm.dto.response.CustomFieldResponse;
import com.erm.dto.response.SystemTableResponse;
import com.erm.exception.ResourceNotFoundException;

public interface FieldService {
	

	

	public List<CategoryListResponse> getAllCategories(Long moduleId)throws ResourceNotFoundException;

	public CategoryResponse getCategory(Long categoryId)throws ResourceNotFoundException;

	public List<CustomFieldResponse> getCustomFieldResponse(long moduleId, String tableName) throws ResourceNotFoundException;

	public List<SystemTableResponse> getSystemTables(Long moduleId) throws ResourceNotFoundException;

	public SystemTableResponse getSystemTableByName(String tableName) throws ResourceNotFoundException;

}
