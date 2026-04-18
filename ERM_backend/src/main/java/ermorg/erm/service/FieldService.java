package ermorg.erm.service;

import java.util.List;

import ermorg.erm.dto.response.CategoryListResponse;
import ermorg.erm.dto.response.CategoryResponse;
import ermorg.erm.dto.response.CustomFieldResponse;
import ermorg.erm.dto.response.SystemTableResponse;
import ermorg.erm.exception.ResourceNotFoundException;

public interface FieldService {
	

	

	public List<CategoryListResponse> getAllCategories(Long moduleId)throws ResourceNotFoundException;

	public CategoryResponse getCategory(Long categoryId)throws ResourceNotFoundException;

	public List<CustomFieldResponse> getCustomFieldResponse(long moduleId, String tableName) throws ResourceNotFoundException;

	public List<SystemTableResponse> getSystemTables(Long moduleId) throws ResourceNotFoundException;

	public SystemTableResponse getSystemTableByName(String tableName) throws ResourceNotFoundException;

}
