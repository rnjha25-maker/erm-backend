package ermorg.erm.controller;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import ermorg.erm.dto.ResponseStatus;
import ermorg.erm.dto.response.CategoryListResponse;
import ermorg.erm.dto.response.CategoryResponse;
import ermorg.erm.dto.response.SystemTableResponse;
import ermorg.erm.exception.ResourceNotFoundException;
import ermorg.erm.response.GeneralResponse;
import ermorg.erm.service.IFieldService;

//@CrossOrigin
@RestController
@RequestMapping("/field")
public class FieldController {
	
	@Autowired
	private IFieldService fieldService;
	
	
//	@GetMapping("/get-system-fields/{id:[\\d]+}")
//	public GeneralResponse<List<SystemFieldResponse>> getSystemFileds(@PathVariable Long tableId) throws ResourceNotFoundException{
//		
//		GeneralResponse<List<SystemFieldResponse>> response = new GeneralResponse<>();
//		List<SystemFieldResponse> systemFields = fieldService.getSystemFields(tableId);
//		
//		response.setData(systemFields);
//		response.setStatus(ResponseStatus.SUCCESS);
//		response.setMessage("Fields added");
//		
//		return response;
//		
//	}
	
	@GetMapping("/get-system-tables/{moduleId:[\\d]+}")
	public GeneralResponse<List<SystemTableResponse>> getSystemTables(@PathVariable Long moduleId) throws ResourceNotFoundException{
		
		GeneralResponse<List<SystemTableResponse>> response = new GeneralResponse<>();
		List<SystemTableResponse> systemTables = fieldService.getSystemTables(moduleId);
		
		response.setData(systemTables);
		response.setStatus(ResponseStatus.SUCCESS);
		response.setMessage("Fields added");
		
		return response;
		
	}
	
//	@GetMapping("/get-system-table-by-id/{tableId:[\\d]+}")
//	public GeneralResponse<SystemTableResponse> getSystemTable(@PathVariable Long tableId) throws ResourceNotFoundException{
//		
//		GeneralResponse<SystemTableResponse> response = new GeneralResponse<>();
//		SystemTableResponse systemTables = fieldService.getSystemTableById(tableId);
//		
//		response.setData(systemTables);
//		response.setStatus(ResponseStatus.SUCCESS);
//		response.setMessage("Fields added");
//		
//		return response;
//		
//	}
	
	@GetMapping("/get-system-table-by-name/{tableName}")
	public GeneralResponse<SystemTableResponse> getSystemTableByName(@PathVariable String tableName) throws ResourceNotFoundException{
		
		GeneralResponse<SystemTableResponse> response = new GeneralResponse<>();
		SystemTableResponse systemTables = fieldService.getSystemTableByName(tableName);
		
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
	

}
