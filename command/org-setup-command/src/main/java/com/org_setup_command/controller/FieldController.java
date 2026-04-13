package com.org_setup_command.controller;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.org_setup_command.dto.request.FieldRequestDTO;
import com.org_setup_command.dto.response.GeneralResponse;
import com.org_setup_command.dto.response.OrganizationFieldsDTO;
import com.org_setup_command.dto.response.ResponseStatus;
import com.org_setup_command.exception.ResourceNotFoundException;
import com.org_setup_command.service.FieldService;
 
@RestController
@RequestMapping("/fields")
public class FieldController {
	
	@Autowired
	private FieldService fieldService;
	
	@PostMapping("/save")
	public GeneralResponse<OrganizationFieldsDTO> saveField(@RequestBody FieldRequestDTO request) throws ResourceNotFoundException{
		GeneralResponse<OrganizationFieldsDTO> response = new GeneralResponse<>();
		
//		OrganizationFieldsDTO field = fieldService.saveField(request);
		
//		response.setData(field);
		response.setMessage("Field Save");
		response.setStatus(ResponseStatus.SUCCESS);
		
		
		return response;
	}
	
	@PostMapping("/delete/{id:[\\d]+}")
	public GeneralResponse<OrganizationFieldsDTO> deleteField(@PathVariable Long id) throws ResourceNotFoundException{
		GeneralResponse<OrganizationFieldsDTO> response = new GeneralResponse<>();
		
		fieldService.deleteField(id);
		
		
		response.setMessage("Field deleted");
		response.setStatus(ResponseStatus.SUCCESS);
		
		
		return response;
	}
	
	@PostMapping("/{id:[\\d]+}")
	public GeneralResponse<OrganizationFieldsDTO> getField(@PathVariable Long id) throws ResourceNotFoundException{
		GeneralResponse<OrganizationFieldsDTO> response = new GeneralResponse<>();
		
		OrganizationFieldsDTO field = fieldService.getField(id);
		
		response.setData(field);
		response.setMessage("Field deleted");
		response.setStatus(ResponseStatus.SUCCESS);
		
		
		return response;
	}
	
	@GetMapping("/get-system-fields/{id:[\\d]+}")
	public GeneralResponse<List<String>> getSystemFields(@PathVariable Long id) throws ResourceNotFoundException{
		GeneralResponse<List<String>> response = new GeneralResponse<>();
		
		List<String> field = fieldService.getSystemFields(id);
		
		response.setData(field);
		response.setMessage("Field deleted");
		response.setStatus(ResponseStatus.SUCCESS);
		
		
		return response;
	}
	
//	@PostMapping("delete/{id:[\\\\d]+}")
//	public GeneralResponse<List<OrganizationFieldsDTO>> getAllField(@PathVariable Long id){
//		GeneralResponse<List<OrganizationFieldsDTO>> response = new GeneralResponse<>();
//		
//		List<OrganizationFieldsDTO> field = fieldService.getAllField(id);
//		response.setData(field);
//		response.setMessage("Field deleted");
//		response.setStatus(ResponseStatus.SUCCESS);
//		
//		
//		return response;
//	}
	
	@PostMapping("/modules")
	public GeneralResponse<Map<String, List<OrganizationFieldsDTO>>> getModules(@PathVariable Long id){
		GeneralResponse<Map<String, List<OrganizationFieldsDTO>>> response = new GeneralResponse<>();
		
		List<OrganizationFieldsDTO> fields = fieldService.getAllField(id);
		
		Map<String, List<OrganizationFieldsDTO>> groupedFields = fields.stream()
		.collect(Collectors.groupingBy(field->field.getModuleName()));
		response.setData(groupedFields);
		response.setMessage("Field deleted");
		response.setStatus(ResponseStatus.SUCCESS);
		
		
		return response;
	}
	

}
