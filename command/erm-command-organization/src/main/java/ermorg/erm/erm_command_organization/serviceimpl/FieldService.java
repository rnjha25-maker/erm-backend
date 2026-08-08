package ermorg.erm.erm_command_organization.serviceimpl;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import ermorg.erm.erm_command_organization.dto.requestDTO.CustomFieldRequest;
import ermorg.erm.erm_command_organization.dto.requestDTO.FieldRequestDTO;
import ermorg.erm.erm_command_organization.dto.responseDTO.CategoryListResponse;
import ermorg.erm.erm_command_organization.dto.responseDTO.CategoryResponse;
import ermorg.erm.erm_command_organization.dto.responseDTO.ModuleResponse;
import ermorg.erm.erm_command_organization.dto.responseDTO.SystemFieldResponse;
import ermorg.erm.erm_command_organization.dto.responseDTO.SystemTableResponse;
import ermorg.erm.erm_command_organization.exception.ResourceNotFoundException;
import ermorg.erm.erm_command_organization.model.Category;
import ermorg.erm.erm_command_organization.model.CustomField;
import ermorg.erm.erm_command_organization.model.Modules;
import ermorg.erm.erm_command_organization.model.SystemField;
import ermorg.erm.erm_command_organization.model.SystemTable;
import ermorg.erm.erm_command_organization.repository.CategoryRepository;
import ermorg.erm.erm_command_organization.repository.CustomFieldRepository;
import ermorg.erm.erm_command_organization.repository.ModuleRepository;
import ermorg.erm.erm_command_organization.repository.SystemFieldRepository;
import ermorg.erm.erm_command_organization.repository.SystemTableRepository;
import ermorg.erm.erm_command_organization.service.IFieldService;
import jakarta.transaction.Transactional;

@Service
public class FieldService implements IFieldService {

	@Autowired
	private ModuleRepository moduleRepository;

	@Autowired
	private SystemTableRepository tableReposity;

	@Autowired
	private SystemFieldRepository systemFieldRepository;

	@Autowired
	private CategoryRepository categoryRepository;

	@Autowired
	private CustomFieldRepository customFieldRepository;
	
	@Autowired
	private SystemTableRepository systemTableRepository;

	@Override
	@Transactional
	public ModuleResponse saveField(FieldRequestDTO request) throws ResourceNotFoundException {

	    // 1. Fetch Module
	    Modules module = moduleRepository.findById(request.getModuleId())
	            .filter(m -> !m.getDeleted())
	            .orElseThrow(() -> new ResourceNotFoundException("Module not found"));

	    // 2. Fetch or create Category
	    Category category = module.getCategories().stream()
	            .filter(c -> c.getCategoryName().equals(request.getCategory())
	                    || c.getId().equals(request.getCategoryId()))
	            .findAny().orElse(new Category());

	    // 3. Fetch System Table
	    SystemTable systemTable = systemTableRepository.findById(request.getTableId())
	            .filter(t -> !t.getDeleted())
	            .orElseThrow(() -> new ResourceNotFoundException("System table not found"));

	    // 4. Soft-delete existing fields via a direct bulk UPDATE query, then flush.
	    //    This avoids Hibernate cascade re-inserting them when the category is saved.
	    if (category.getId() != null) {
	        customFieldRepository.softDeleteByCategoryId(category.getId());
	        customFieldRepository.flush();
	    }

	    // 5. Validate duplicates within the incoming request
	    Set<String> fieldNames = new HashSet<>();
	    Set<Long> mappedColumnIds = new HashSet<>();
	    for (CustomFieldRequest customRequest : request.getFields()) {
	        if (!fieldNames.add(customRequest.getFieldName()))
	            throw new RuntimeException("Duplicate field name in request: " + customRequest.getFieldName());
	        if (!mappedColumnIds.add(customRequest.getMappedWithColumnId()))
	            throw new RuntimeException("Duplicate mapped column id in request: " + customRequest.getMappedWithColumnId());
	    }

	    // 6. Update category metadata and save it first to get an ID if new
	    category.setCategoryName(request.getCategory());
	    category.setMappedWithTable(request.getTableName());
	    category.setModule(module);
	    category.setDeleted(false);
	    category.setDisplayOrder(request.getDisplayOrder());
	    Category savedCategory = categoryRepository.save(category);
	    categoryRepository.flush();

	    // 7. Build and save new fields directly — bypasses cascade entirely
	    List<CustomField> newFields = new java.util.ArrayList<>();
	    for (CustomFieldRequest customRequest : request.getFields()) {
	        SystemField systemField = systemTable.getFields().stream()
	                .filter(f -> !f.getDeleted())
	                .filter(f -> f.getId().equals(customRequest.getMappedWithColumnId()))
	                .findFirst()
	                .orElseThrow(() -> new ResourceNotFoundException(
	                        "System field not found for id: " + customRequest.getMappedWithColumnId()));

	        CustomField customField = new CustomField();
	        customField.setFieldName(customRequest.getFieldName());
	        customField.setFieldType(customRequest.getFieldType());
	        customField.setDeleted(false);
	        customField.setFieldOrder(customRequest.getFieldOrder());
	        customField.setShowGridColumn(customRequest.isShowGridColumn());
	        customField.setShowInView(customRequest.isShowInView());
	        customField.setDisabled(customRequest.isDisabled());
	        customField.setRequired(customRequest.isRequired());
	        customField.setSystemField(systemField);
	        customField.setCategory(savedCategory);
	        newFields.add(customField);
	    }
	    customFieldRepository.saveAll(newFields);

	    // 8. Build response from saved data directly — avoids stale Hibernate cache
	    CategoryResponse categoryResponse = new CategoryResponse(savedCategory);
	    categoryResponse.setCustomFields(newFields);

	    ModuleResponse moduleResponse = new ModuleResponse(module);
	    moduleResponse.getCategories().stream()
	            .filter(c -> c.getCategoryId() == savedCategory.getId())
	            .findFirst()
	            .ifPresentOrElse(
	                c -> c.setCustomFields(newFields),
	                () -> moduleResponse.getCategories().add(categoryResponse)
	            );
	    return moduleResponse;
	}
	public ModuleResponse updateField(FieldRequestDTO request) throws ResourceNotFoundException {

		Modules module = moduleRepository.findById(request.getModuleId()).filter(module1 -> !module1.getDeleted())
				.orElseThrow(() -> new RuntimeException("Module not found."));

		List<Category> categories = module.getCategories();

		Category category = categories.stream()
				.filter(category1 -> category1.getId().equals(request.getCategoryId())).findAny()
				.orElse(new Category());
		
		Set<CustomField> categoryFields = category.getFields();
		
		categoryFields. forEach(categoryField -> customFieldRepository.delete(categoryField));

		categoryFields.clear();
		category.setDeleted(false);
		SystemTable systemTable = systemTableRepository.findById(request.getTableId())
		.filter(table -> !table.getDeleted())
		.orElseThrow(()-> new ResourceNotFoundException("Table not found."));
		category.setDeleted(false);
		for (CustomFieldRequest customRequest : request.getFields()) {
			category.getFields()
			.stream()
			.forEach(field ->{
				if(field.getId().equals(customRequest.getFieldId())) {
					field.setFieldName(customRequest.getFieldName());
					field.setFieldType(customRequest.getFieldType());
					field.setDeleted(false);
					field.setFieldOrder(customRequest.getFieldOrder());
					field.setShowGridColumn(customRequest.isShowGridColumn());
					field.setShowInView(customRequest.isShowInView());
					field.setDisabled(customRequest.isDisabled());
					SystemField systemField = null;
					try {
						systemField = systemTable.getFields().stream()
						.filter(field1 -> !field1.getDeleted())
						.filter(field2 -> field2.getId().equals(customRequest.getMappedWithColumnId()))
						.findAny()
						.orElseThrow(()-> new ResourceNotFoundException("System field not avaliable."));
					} catch (ResourceNotFoundException e) {
						
						e.printStackTrace();
					}
					field.setSystemField(systemField);
					field.setRequired(customRequest.isRequired());
					field.setCategory(category);
					
					categoryFields.add(field);
				}
			});

		}

		category.setCategoryName(request.getCategory());
		category.setMappedWithTable(request.getTableName());
		category.setFields(categoryFields);
		category.setModule(module);

		Modules savedModule = moduleRepository.save(module);

		ModuleResponse moduleResponse = new ModuleResponse(savedModule);

		return moduleResponse;
	}

	@Override
	public List<SystemFieldResponse> getSystemFields(Long tableId) throws ResourceNotFoundException {
		SystemTable systemTable = tableReposity.findById(tableId).filter(field1 -> !field1.getDeleted())
				.orElseThrow(() -> new ResourceNotFoundException("No system table found."));
		
		List<SystemField> fields = systemTable.getFields();
		List<CustomField> customFields = customFieldRepository.findAll();
		
		List<SystemFieldResponse> systemFieldResponse = fields.stream()
		.filter(field -> 
			customFields.stream()
			.noneMatch(cf-> cf.getSystemField().getId().equals(field.getId()))
		)
		.map(field -> new SystemFieldResponse(field))
		.collect(Collectors.toList());
		
//		List<SystemField> systemFields = fields.stream()
//		.filter(field -> {
//			long count = customFields.stream().filter(cf -> cf.getSystemField().getId().equals(field.getId())).count();
//		return count == 0;
//		})
//		.collect(Collectors.toList());
//		return systemFields.stream().map(field -> new SystemFieldResponse(field))
//				.collect(Collectors.toList());
		
		return systemFieldResponse;
	}

	@Override
	public List<SystemTableResponse> getSystemTables(Long moduleId) throws ResourceNotFoundException {
		
		Modules module = moduleRepository.findById(moduleId).filter(module1 -> !module1.getDeleted())
				.orElseThrow(() -> new ResourceNotFoundException("Module not found."));
		
		List<SystemTable> systemTables = tableReposity.findAllByModuleId(moduleId)
				.stream().filter(table -> !table.getDeleted()).collect(Collectors.toList());
//		List<SystemTableResponse> systemTableResponse = systemTables.stream()
//				.map(table -> new SystemTableResponse(table)).collect(Collectors.toList());

		List<Category> categories = categoryRepository.findAll().stream().filter(category -> !category.getDeleted())
				.collect(Collectors.toList());
//		
//		List<SystemTableResponse> systemTableResponse = systemTables.stream()
//				.peek(tble->System.out.println( "initial table" + tble.getTableName() + " " + tble.getModule().getId()))
//		.filter(systemTable-> categories
//				.stream()
//				.peek(cat->System.out.println( "cat to match " + cat.getMappedWithTable() + " " + cat.getModule().getId()))
//				.anyMatch(category -> !category.getMappedWithTable().equals(systemTable.getTableName()) && category.getModule() != null && systemTable.getModule() != null && !category.getModule().getId().equals(systemTable.getModule().getId())))
//		.peek(m -> System.out.println("Matched table " + m.getTableName()))
//		.map(table -> new SystemTableResponse(table)).collect(Collectors.toList());	
		
		List<SystemTableResponse> systemTableResponse = systemTables.stream()
                .peek(tble->System.out.println( "initial table" + tble.getTableName() + " " + tble.getModule().getId()))
                .filter(systemTable-> categories
                        .stream()
                        .peek(cat->System.out.println( "cat to match " + cat.getMappedWithTable() + " " + cat.getModule().getId()))
                        .noneMatch(category -> category.getMappedWithTable().equals(systemTable.getTableName()) && category.getModule().getId().equals(systemTable.getModule().getId())))
                .peek(m -> System.out.println("Matched table " + m.getTableName()))
                .map(table -> new SystemTableResponse(table)).collect(Collectors.toList());
		return systemTableResponse;
	}
	
	

	@Override
	public SystemTableResponse getSystemTableById(Long tableId) throws ResourceNotFoundException {
		SystemTable systemTable = systemTableRepository.findById(tableId)
		.filter(table -> !table.getDeleted())
		.orElseThrow(()-> new ResourceNotFoundException("Table not found."));
		return new SystemTableResponse(systemTable);
	}

	@Override
	public List<CategoryListResponse> getAllCategories(Long moduleId) throws ResourceNotFoundException {
		if (!moduleRepository.existsByIdAndDeletedFalse(moduleId)) {
			throw new ResourceNotFoundException("Module not found.");
		}

		return categoryRepository.findOrderedCategories(moduleId).stream().map(CategoryListResponse::new)
				.collect(Collectors.toList());
	}

	@Override
	public void deleteField(Long fieldId) throws ResourceNotFoundException {

		CustomField field = customFieldRepository.findById(fieldId)
				.orElseThrow(() -> new ResourceNotFoundException("Field not found"));

		field.setDeleted(true);

		customFieldRepository.save(field);
	}

	@Override
	public void deleteCategory(Long categoryId) throws ResourceNotFoundException {

		Category category = categoryRepository.findById(categoryId)
				.orElseThrow(() -> new ResourceNotFoundException("Category not found"));
		customFieldRepository.deleteAll(category.getFields());
		category.getFields().clear();
		
		customFieldRepository.flush();
		category.setDeleted(true);

		categoryRepository.save(category);

	}

	@Override
	public CategoryResponse getCategory(Long categoryId) throws ResourceNotFoundException {
		Category category = categoryRepository.findById(categoryId)
				.orElseThrow(() -> new ResourceNotFoundException("Category not found"));

		return new CategoryResponse(category);
	}

	@Override
	public SystemTableResponse getSystemTableByName(String tableName) throws ResourceNotFoundException {
		SystemTable systemTable = systemTableRepository.findByTableName(tableName);
		
		if(systemTable == null || systemTable.getDeleted()) throw new ResourceNotFoundException("Table not found.");
		return new SystemTableResponse(systemTable);
	}
	
	
	

}
