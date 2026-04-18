package ermorg.erm.erm_command_organization.service;

import java.util.List;

import ermorg.erm.erm_command_organization.dto.requestDTO.ViewFieldRequest;
import ermorg.erm.erm_command_organization.dto.responseDTO.ModuleResponse;
import ermorg.erm.erm_command_organization.dto.responseDTO.SystemViewFieldResponse;
import ermorg.erm.erm_command_organization.dto.responseDTO.SystemViewResponse;
import ermorg.erm.erm_command_organization.dto.responseDTO.ViewCategoryResponse;
import ermorg.erm.erm_command_organization.exception.ResourceNotFoundException;

public interface IViewService {

	List<SystemViewResponse> getSystemViews();

	List<SystemViewFieldResponse> getSystemViewFields(Long viewId) throws ResourceNotFoundException;

	ModuleResponse saveField(ViewFieldRequest request) throws ResourceNotFoundException;

	List<ViewCategoryResponse> getAllCategories(Long moduleId) throws ResourceNotFoundException;

	ViewCategoryResponse getCategory(Long categoryId) throws ResourceNotFoundException;

	void deleteCategory(Long categoryId) throws ResourceNotFoundException;

}
