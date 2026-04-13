package com.org_setup_command.service;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.org_setup_command.constants.FieldConstants;
import com.org_setup_command.dto.request.FieldRequestDTO;
import com.org_setup_command.dto.response.CustomFieldRequest;
import com.org_setup_command.dto.response.ModuleResponse;
import com.org_setup_command.dto.response.OrganizationFieldsDTO;
import com.org_setup_command.exception.ResourceNotFoundException;
import com.org_setup_command.modal.Category;
import com.org_setup_command.modal.CustomField;
import com.org_setup_command.modal.Modules;
import com.org_setup_command.modal.Organization;
import com.org_setup_command.repository.CategoryRepository;
import com.org_setup_command.repository.ModuleRepository;
import com.org_setup_command.repository.OrganizationFieldsReqpository;
import com.org_setup_command.repository.OrganizationRepository;
import com.org_setup_command.util.OrganizationContext;

@Service
public class IFieldService implements FieldService {

	@Autowired
	private OrganizationFieldsReqpository fieldRepository;
	
	@Autowired
	private ModuleRepository moduleRepository;
	
	@Autowired
	private OrganizationRepository organizationRepository;
	
	@Autowired
	private CategoryRepository categoryRepository;
		
	@Override
	public ModuleResponse saveField(FieldRequestDTO request) throws ResourceNotFoundException {
		
		
		Modules module = moduleRepository.findById(request.getModuleId())
				.orElseThrow(()->new RuntimeException("Module not found."));	
		
		
		List<Category> categories = module.getCategories();
		
		Category category = categories.stream().filter(category1-> category1.getCategoryName().equals(request.getCategory()))
		.findAny()
		.orElse(new Category());		
		
		List<CustomField> fields = new ArrayList<>();
		for(CustomFieldRequest customRequest : request.getFields()) {
			CustomField  customField = new CustomField();
			customRequest.setFieldName(customRequest.getFieldName());
			customRequest.setFieldType(customRequest.getFieldType());
			customField.setMappedWith(customRequest.getMappedWith());
			customField.setRequired(customRequest.isRequired());
			customField.setCategory(category);
			
			fields.add(customField);
			
		}
		
		category.setCategoryName(request.getCategory());
		category.setMappedWithTable(request.getTableName());
		category.setFields(fields);
		category.setModule(module);
		
		categories.add(category);
		
		module.setCategories(categories);
		
		Modules savedModule = moduleRepository.save(module);
		
		ModuleResponse moduleResponse = new ModuleResponse(savedModule);
		
		return moduleResponse;
	}

	@Override
	public void deleteField(Long fieldId) throws ResourceNotFoundException {
		CustomField field = fieldRepository.findById(fieldId).orElseThrow(()-> new ResourceNotFoundException("Field not found"));
         
		fieldRepository.delete(field);
	}

	@Override
	public OrganizationFieldsDTO getField(Long fieldId) throws ResourceNotFoundException {
		
		
		return null;
	}

	@Override
	public List<OrganizationFieldsDTO> getAllField(Long organizationId) {
		
//		List<Modules> modules = OrganizationContext.getOrganization().getModules();
		
//		List<OrganizationFieldsDTO> fields = modules.stream()
//		.map(module -> module.getFields())
//		.map(m -> new OrganizationFieldsDTO(m))
//		.collect(Collectors.toList());

		
		return null;
		
	}

	@Override
	public List<String> getSystemFields(long organizationId) throws ResourceNotFoundException {
		
		List<String> fields = new FieldConstants().getFields();
		Organization organization = organizationRepository.findById(organizationId).orElseThrow(()-> new ResourceNotFoundException("Organization not found."));
//		List<Modules> modules = organization.getModules();
		
////		List<String> orgFields = modules.stream()
////		.map(module -> module.getFields())
////		.flatMap(m1 -> m1.stream())
//		.map(m -> m.getFieldName())
//		.collect(Collectors.toList());
//		
//		fields.removeAll(orgFields);
		
		 
		return null;
	}
	
	

}
