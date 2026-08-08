package ermorg.erm.service;

import java.util.List;

import ermorg.erm.dto.response.CategoryListResponse;
import ermorg.erm.dto.response.CategoryResponse;
import ermorg.erm.dto.response.CustomFieldResponse;
import ermorg.erm.dto.response.SystemFieldResponse;
import ermorg.erm.dto.response.SystemTableResponse;
import ermorg.erm.exception.ResourceNotFoundException;

public interface IFieldService {

    List<CategoryListResponse> getAllCategories(Long moduleId) throws ResourceNotFoundException;

    CategoryResponse getCategory(Long categoryId) throws ResourceNotFoundException;

    List<CustomFieldResponse> getCustomFieldResponse(long moduleId, String tableName)
            throws ResourceNotFoundException;

    List<SystemTableResponse> getSystemTables(Long moduleId) throws ResourceNotFoundException;

    SystemTableResponse getSystemTableByName(String tableName) throws ResourceNotFoundException;

    /**
     * Returns active dropdown options for a given system field name and table.
     * Enables fully metadata-driven dropdown rendering — no Java enum knowledge needed.
     */
    List<SystemFieldResponse.FieldOptionResponse> getFieldOptions(
            String fieldName, String tableName) throws ResourceNotFoundException;
}
