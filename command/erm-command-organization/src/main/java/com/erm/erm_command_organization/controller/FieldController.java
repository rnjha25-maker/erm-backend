package com.erm.erm_command_organization.controller;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.erm.erm_command_organization.dto.requestDTO.FieldRequestDTO;
import com.erm.erm_command_organization.dto.responseDTO.CategoryListResponse;
import com.erm.erm_command_organization.dto.responseDTO.CategoryResponse;
import com.erm.erm_command_organization.dto.responseDTO.GeneralResponse;
import com.erm.erm_command_organization.dto.responseDTO.ModuleResponse;
import com.erm.erm_command_organization.dto.responseDTO.ResponseStatus;
import com.erm.erm_command_organization.dto.responseDTO.SystemFieldResponse;
import com.erm.erm_command_organization.dto.responseDTO.SystemTableResponse;
import com.erm.erm_command_organization.exception.ResourceNotFoundException;
import com.erm.erm_command_organization.service.FieldService;

import jakarta.validation.Valid;

//@CrossOrigin
@RestController
@RequestMapping("/field")
public class FieldController {
	
	@Autowired
	private FieldService fieldService;
	
	@PostMapping("/save")
	public GeneralResponse<ModuleResponse> saveFileds(@Valid @RequestBody FieldRequestDTO request) throws ResourceNotFoundException{
		
		GeneralResponse<ModuleResponse> response = new GeneralResponse<>();
		ModuleResponse saveFields = fieldService.saveField(request);
		
		response.setData(saveFields);
		response.setStatus(ResponseStatus.SUCCESS);
		response.setMessage("Fields added");
		
		return response;
		
	}
	
	@GetMapping("/get-system-fields/{id:[\\d]+}")
	public GeneralResponse<List<SystemFieldResponse>> getSystemFileds(@PathVariable Long tableId) throws ResourceNotFoundException{
		
		GeneralResponse<List<SystemFieldResponse>> response = new GeneralResponse<>();
		List<SystemFieldResponse> systemFields = fieldService.getSystemFields(tableId);
		
		response.setData(systemFields);
		response.setStatus(ResponseStatus.SUCCESS);
		response.setMessage("Fields added");
		
		return response;
		
	}
	
	@GetMapping("/get-system-tables/{moduleId:[\\d]+}")
	public GeneralResponse<List<SystemTableResponse>> getSystemTables(@PathVariable Long moduleId) throws ResourceNotFoundException{
		
		GeneralResponse<List<SystemTableResponse>> response = new GeneralResponse<>();
		List<SystemTableResponse> systemTables = fieldService.getSystemTables(moduleId);
		
		response.setData(systemTables);
		response.setStatus(ResponseStatus.SUCCESS);
		response.setMessage("Fields added");
		
		return response;
		
	}
	
	@GetMapping("/get-system-table-by-id/{tableId:[\\d]+}")
	public GeneralResponse<SystemTableResponse> getSystemTable(@PathVariable Long tableId) throws ResourceNotFoundException{
		
		GeneralResponse<SystemTableResponse> response = new GeneralResponse<>();
		SystemTableResponse systemTables = fieldService.getSystemTableById(tableId);
		
		response.setData(systemTables);
		response.setStatus(ResponseStatus.SUCCESS);
		response.setMessage("Fields added");
		
		return response;
		
	}
	
	@GetMapping("/get-system-table-by-name/{taleName}")
	public GeneralResponse<SystemTableResponse> getSystemTable(@PathVariable String taleName) throws ResourceNotFoundException{
		
		GeneralResponse<SystemTableResponse> response = new GeneralResponse<>();
		SystemTableResponse systemTables = fieldService.getSystemTableByName(taleName);
		
		response.setData(systemTables);
		response.setStatus(ResponseStatus.SUCCESS);
		response.setMessage("Fields added");
		
		return response;
		
	}
	
	@GetMapping("/get-all-category-fields/{id:[\\d]+}")
	public GeneralResponse<List<CategoryListResponse>> getAllCategories(@PathVariable("id") Long moduleId) throws ResourceNotFoundException{
		
		GeneralResponse<List<CategoryListResponse>> response = new GeneralResponse<>();
		List<CategoryListResponse> systemTables = fieldService.getAllCategories(moduleId);
		
		response.setData(systemTables);
		response.setStatus(ResponseStatus.SUCCESS);
		response.setMessage("Fields added");
		
		return response;
		
	}
	
	@GetMapping("/get-category/{id:[\\d]+}")
	public GeneralResponse<CategoryResponse> getCategory(@PathVariable("id") Long categoryId) throws ResourceNotFoundException{
		
		GeneralResponse<CategoryResponse> response = new GeneralResponse<>();
		CategoryResponse systemTables = fieldService.getCategory(categoryId);
		
		response.setData(systemTables);
		response.setStatus(ResponseStatus.SUCCESS);
		response.setMessage("Fields added");
		
		return response;
		
	}
	
	@DeleteMapping("/delete-field/{id:[\\d]+}")
	public GeneralResponse<List<CategoryListResponse>> deleteField(@PathVariable("id") Long fieldId) throws ResourceNotFoundException{
		
		GeneralResponse<List<CategoryListResponse>> response = new GeneralResponse<>();
		fieldService.deleteField(fieldId);
		
		response.setStatus(ResponseStatus.SUCCESS);
		response.setMessage("Fields added");
		
		return response;
		
	}
	
	@DeleteMapping("/delete-category/{id:[\\d]+}")
	public GeneralResponse<List<CategoryListResponse>> deleteCategory(@PathVariable("id") Long categoryId) throws ResourceNotFoundException{
		
		GeneralResponse<List<CategoryListResponse>> response = new GeneralResponse<>();
		fieldService.deleteCategory(categoryId);
		
		response.setStatus(ResponseStatus.SUCCESS);
		response.setMessage("Fields added");
		
		return response;
		
	}

}
