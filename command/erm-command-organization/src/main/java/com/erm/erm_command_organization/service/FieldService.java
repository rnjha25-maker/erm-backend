package com.erm.erm_command_organization.service;

import java.util.List;

import com.erm.erm_command_organization.dto.requestDTO.FieldRequestDTO;
import com.erm.erm_command_organization.dto.responseDTO.CategoryListResponse;
import com.erm.erm_command_organization.dto.responseDTO.CategoryResponse;
import com.erm.erm_command_organization.dto.responseDTO.ModuleResponse;
import com.erm.erm_command_organization.dto.responseDTO.SystemFieldResponse;
import com.erm.erm_command_organization.dto.responseDTO.SystemTableResponse;
import com.erm.erm_command_organization.exception.ResourceNotFoundException;

public interface FieldService {
	
	public ModuleResponse saveField(FieldRequestDTO request) throws ResourceNotFoundException;

	public List<SystemFieldResponse> getSystemFields(Long tableId) throws ResourceNotFoundException;
	
	public List<SystemTableResponse>  getSystemTables(Long moduleId) throws ResourceNotFoundException;

	public List<CategoryListResponse> getAllCategories(Long moduleId)throws ResourceNotFoundException;

	public void deleteField(Long fieldId)throws ResourceNotFoundException;

	public void deleteCategory(Long categoryId) throws ResourceNotFoundException;

	public CategoryResponse getCategory(Long categoryId)throws ResourceNotFoundException;

	public SystemTableResponse getSystemTableById(Long tableId)throws ResourceNotFoundException;

	public SystemTableResponse getSystemTableByName(String taleName) throws ResourceNotFoundException;

}
