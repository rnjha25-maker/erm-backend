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

import com.erm.erm_command_organization.dto.requestDTO.ViewFieldRequest;
import com.erm.erm_command_organization.dto.responseDTO.GeneralResponse;
import com.erm.erm_command_organization.dto.responseDTO.ModuleResponse;
import com.erm.erm_command_organization.dto.responseDTO.ResponseStatus;
import com.erm.erm_command_organization.dto.responseDTO.SystemViewFieldResponse;
import com.erm.erm_command_organization.dto.responseDTO.SystemViewResponse;
import com.erm.erm_command_organization.dto.responseDTO.ViewCategoryResponse;
import com.erm.erm_command_organization.exception.ResourceNotFoundException;
import com.erm.erm_command_organization.service.IViewService;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/view")
//@CrossOrigin
public class ViewController {
	
	@Autowired
	private IViewService viewService;
	
	@PostMapping("/save")
	public GeneralResponse<ModuleResponse> saveFileds(@Valid @RequestBody ViewFieldRequest request) throws ResourceNotFoundException{
		
		GeneralResponse<ModuleResponse> response = new GeneralResponse<>();
		ModuleResponse saveFields = viewService.saveField(request);
		
		response.setData(saveFields);
		response.setStatus(ResponseStatus.SUCCESS);
		response.setMessage("Fields added");
		
		return response;
		
	}
	
	@GetMapping("/get-system-fields/{viewId:[\\d]+}")
	public GeneralResponse<List<SystemViewFieldResponse>> getSystemFileds(@PathVariable Long viewId) throws ResourceNotFoundException{
		
		GeneralResponse<List<SystemViewFieldResponse>> response = new GeneralResponse<>();
		List<SystemViewFieldResponse> systemFields = viewService.getSystemViewFields(viewId);
		
		response.setData(systemFields);
		response.setStatus(ResponseStatus.SUCCESS);
		response.setMessage("Fields added");
		
		return response;
		
	}
	
	@GetMapping("/get-system-tables")
	public GeneralResponse<List<SystemViewResponse>> getSystemTables() throws ResourceNotFoundException{
		
		GeneralResponse<List<SystemViewResponse>> response = new GeneralResponse<>();
		List<SystemViewResponse> systemTables = viewService.getSystemViews();
		
		response.setData(systemTables);
		response.setStatus(ResponseStatus.SUCCESS);
		response.setMessage("Fields added");
		
		return response;
		
	}
	
	@GetMapping("/get-all-view-category-fields/{id:[\\d]+}")
	public GeneralResponse<List<ViewCategoryResponse>> getAllCategories(@PathVariable("id") Long moduleId) throws ResourceNotFoundException{
		
		GeneralResponse<List<ViewCategoryResponse>> response = new GeneralResponse<>();
		List<ViewCategoryResponse> systemTables = viewService.getAllCategories(moduleId);
		
		response.setData(systemTables);
		response.setStatus(ResponseStatus.SUCCESS);
		response.setMessage("Fields added");
		
		return response;
		
	}
	
	@GetMapping("/get-veiw-category/{id:[\\d]+}")
	public GeneralResponse<ViewCategoryResponse> getCategory(@PathVariable("id") Long categoryId) throws ResourceNotFoundException{
		
		GeneralResponse<ViewCategoryResponse> response = new GeneralResponse<>();
		ViewCategoryResponse systemTables = viewService.getCategory(categoryId);
		
		response.setData(systemTables);
		response.setStatus(ResponseStatus.SUCCESS);
		response.setMessage("Fields added");
		
		return response;
		
	}
	
	
	@DeleteMapping("/delete-view-category/{id:[\\d]+}")
	public GeneralResponse<List<ViewCategoryResponse>> deleteCategory(@PathVariable("id") Long categoryId) throws ResourceNotFoundException{
		
		GeneralResponse<List<ViewCategoryResponse>> response = new GeneralResponse<>();
		viewService.deleteCategory(categoryId);
		
		response.setStatus(ResponseStatus.SUCCESS);
		response.setMessage("Fields added");
		
		return response;
		
	}

}
