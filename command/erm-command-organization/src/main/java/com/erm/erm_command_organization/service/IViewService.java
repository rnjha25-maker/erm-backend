package com.erm.erm_command_organization.service;

import java.util.List;

import com.erm.erm_command_organization.dto.requestDTO.ViewFieldRequest;
import com.erm.erm_command_organization.dto.responseDTO.ModuleResponse;
import com.erm.erm_command_organization.dto.responseDTO.SystemViewFieldResponse;
import com.erm.erm_command_organization.dto.responseDTO.SystemViewResponse;
import com.erm.erm_command_organization.dto.responseDTO.ViewCategoryResponse;
import com.erm.erm_command_organization.exception.ResourceNotFoundException;

public interface IViewService {

	List<SystemViewResponse> getSystemViews();

	List<SystemViewFieldResponse> getSystemViewFields(Long viewId) throws ResourceNotFoundException;

	ModuleResponse saveField(ViewFieldRequest request) throws ResourceNotFoundException;

	List<ViewCategoryResponse> getAllCategories(Long moduleId) throws ResourceNotFoundException;

	ViewCategoryResponse getCategory(Long categoryId) throws ResourceNotFoundException;

	void deleteCategory(Long categoryId) throws ResourceNotFoundException;

}
