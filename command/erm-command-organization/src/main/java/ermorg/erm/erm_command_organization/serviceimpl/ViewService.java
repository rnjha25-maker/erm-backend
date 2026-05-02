package ermorg.erm.erm_command_organization.serviceimpl;

import java.util.List;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import ermorg.erm.erm_command_organization.dto.requestDTO.CustomFieldRequest;
import ermorg.erm.erm_command_organization.dto.requestDTO.CustomViewFieldRequest;
import ermorg.erm.erm_command_organization.dto.requestDTO.ViewFieldRequest;
import ermorg.erm.erm_command_organization.dto.responseDTO.ModuleResponse;
import ermorg.erm.erm_command_organization.dto.responseDTO.SystemViewFieldResponse;
import ermorg.erm.erm_command_organization.dto.responseDTO.SystemViewResponse;
import ermorg.erm.erm_command_organization.dto.responseDTO.ViewCategoryResponse;
import ermorg.erm.erm_command_organization.exception.ResourceNotFoundException;
import ermorg.erm.erm_command_organization.model.Category;
import ermorg.erm.erm_command_organization.model.CustomField;
import ermorg.erm.erm_command_organization.model.CustomViewFields;
import ermorg.erm.erm_command_organization.model.Modules;
import ermorg.erm.erm_command_organization.model.SystemField;
import ermorg.erm.erm_command_organization.model.SystemTable;
import ermorg.erm.erm_command_organization.model.SystemView;
import ermorg.erm.erm_command_organization.model.SystemViewField;
import ermorg.erm.erm_command_organization.model.ViewCategory;
import ermorg.erm.erm_command_organization.repository.CustomViewFieldRepository;
import ermorg.erm.erm_command_organization.repository.ModuleRepository;
import ermorg.erm.erm_command_organization.repository.SystemViewFieldRepository;
import ermorg.erm.erm_command_organization.repository.SystemViewRepository;
import ermorg.erm.erm_command_organization.repository.ViewCategoryRepository;
import ermorg.erm.erm_command_organization.service.IViewService;
import jakarta.transaction.Transactional;

@Service
public class ViewService implements IViewService {

	@Autowired
	private SystemViewRepository viewRepository;
	@Autowired
	private SystemViewFieldRepository fieldRepository;
	
	@Autowired
	private ModuleRepository moduleRepository;
	
	@Autowired
	private ViewCategoryRepository viewCategoryRepository;

    @Autowired
	private CustomViewFieldRepository customViewFieldRepository;
	
	@Override
	public List<SystemViewResponse> getSystemViews() {
		List<SystemView> systemViews = viewRepository.findAll();
		
		return systemViews.stream()
		.filter(view-> !view.getDeleted())
		.map(view-> new SystemViewResponse(view))
		.collect(Collectors.toList());
	}

	@Override
	public List<SystemViewFieldResponse> getSystemViewFields(Long viewId) throws ResourceNotFoundException {
		SystemView systemView = viewRepository.findById(viewId)
		.filter(view-> !view.getDeleted())
		.orElseThrow(() -> new ResourceNotFoundException("No system view found."));
		
		
		List<SystemViewFieldResponse> fileds = systemView.getFields().stream()
		.filter(field-> !field.getDeleted())
		.map(field-> new SystemViewFieldResponse(field))
		.collect(Collectors.toList());
		
		return fileds;
	}

	@Transactional
	@Override
	public ModuleResponse saveField(ViewFieldRequest request) throws ResourceNotFoundException {

		Modules module = moduleRepository.findById(request.getModuleId()).filter(module1 -> !module1.getDeleted())
		        .orElseThrow(() -> new RuntimeException("Module not found."));

		List<ViewCategory> categories = module.getViewCategories();

		ViewCategory category = categories.stream()
		        .filter(category1 -> category1.getCategoryName().equals(request.getViewCategory()) || category1.getId().equals(request.getViewCategoryId())).findAny()
		        .orElse(new ViewCategory());

		SystemView systemTable = viewRepository.findById(request.getViewId())
				.filter(table -> !table.getDeleted())
				.orElseThrow(()-> new ResourceNotFoundException("Table not found."));
		// Delete existing fields
		customViewFieldRepository.deleteAll(category.getFields());
		category.getFields().clear();
		
		customViewFieldRepository.flush();

		category.setDeleted(false);
		// Update category fields
		for (CustomViewFieldRequest customRequest : request.getFields()) {
		    CustomViewFields customField = new CustomViewFields();
		    customField.setFieldName(customRequest.getViewFieldName());
		    SystemViewField systemField = systemTable.getFields().stream()
		            .filter(field -> !field.getDeleted())
		            .filter(field -> field.getId().equals(customRequest.getMappedWithColumnId()))
		            .findAny() 
		            .orElseThrow(() -> new ResourceNotFoundException("System field not avaliable." + customRequest.getMappedWithColumnId()));
		    customField.setSystemViewField(systemField);
		    customField.setCategory(category);
		   
		    category.getFields().add(customField);
		}

		// Update category
		category.setCategoryName(request.getViewCategory());
		category.setMappedWithTable(request.getViewName());
		category.setModule(module);
		categories.add(category);
		// Update module
		module.setViewCategories(categories);
		
		Modules savedModule = moduleRepository.save(module);

		ModuleResponse moduleResponse = new ModuleResponse(savedModule);

		return moduleResponse;
	
	}

	@Override
	public List<ViewCategoryResponse> getAllCategories(Long moduleId) throws ResourceNotFoundException {
		
		Modules module = moduleRepository.findById(moduleId).filter(module1 -> !module1.getDeleted())
		.orElseThrow(() -> new RuntimeException("Module not found."));
		
		List<ViewCategoryResponse> categories = module.getViewCategories().stream()
		.filter(category-> !category.getDeleted())
		.map(category-> new ViewCategoryResponse(category))
		.collect(Collectors.toList());
		
		return categories;
	}

	@Override
	public ViewCategoryResponse getCategory(Long categoryId) throws ResourceNotFoundException {
		ViewCategory viewCategory = viewCategoryRepository.findById(categoryId).filter(category-> !category.getDeleted())
		.orElseThrow(() -> new ResourceNotFoundException("Category not found."));
		return new ViewCategoryResponse(viewCategory);
	}

	@Override
	public void deleteCategory(Long categoryId) throws ResourceNotFoundException {
		ViewCategory viewCategory = viewCategoryRepository.findById(categoryId).filter(category-> !category.getDeleted())
				.orElseThrow(() -> new ResourceNotFoundException("Category not found."));
		customViewFieldRepository.deleteAll(viewCategory.getFields());
		viewCategory.getFields().clear();
		
		customViewFieldRepository.flush();
		viewCategory.setDeleted(true);
		viewCategoryRepository.save(viewCategory);
		
	}
	
	

}
