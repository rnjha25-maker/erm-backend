package ermorg.erm.serviceimpl;

import java.util.List;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import ermorg.erm.dto.response.CategoryListResponse;
import ermorg.erm.dto.response.CategoryResponse;
import ermorg.erm.dto.response.CustomFieldResponse;
import ermorg.erm.dto.response.SystemTableResponse;
import ermorg.erm.exception.ResourceNotFoundException;
import ermorg.erm.model.Category;
import ermorg.erm.model.ModuleOrganization;
import ermorg.erm.model.Organization;
import ermorg.erm.repository.CategoryRepository;
import ermorg.erm.repository.CustomFieldRepository;
import ermorg.erm.repository.ModuleRepository;
import ermorg.erm.repository.OrgModuleRepository;
import ermorg.erm.repository.OrganizationRepository;
import ermorg.erm.repository.SystemFieldRepository;
import ermorg.erm.repository.SystemTableRepository;
import ermorg.erm.service.IFieldService;
import ermorg.erm.util.OrganizationContext;

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

	@Autowired
	private OrgModuleRepository orgModuleRepository;
	@Autowired 
	private OrganizationRepository organizationRepository;
	
	
	@Transactional(readOnly = true)
	public List<CategoryListResponse> getAllCategories(Long moduleId) throws ResourceNotFoundException {

	    Long orgId = OrganizationContext.getOrganization().getId();
	    if (orgId == null) {
			throw new ResourceNotFoundException("Organization not found");
		}
	    Organization organization = organizationRepository.findById(orgId)
	            .orElseThrow(() -> new ResourceNotFoundException("Organization not found"));
		
	    List<Long> categoryIds = organization.getModules().stream()
	            .filter(module -> !module.getDeleted() && module.getModuleId().equals(moduleId))
	            .map(module -> module.getCategoryId())
	            .collect(Collectors.toList());

	    List<Category> categories = categoryRepository.findAllById(categoryIds);

	    return categories.stream()
	            .filter(category -> !category.getDeleted())
	            .map(CategoryListResponse::new)
	            .collect(Collectors.toList());
	}

	@Override
	public CategoryResponse getCategory(Long categoryId) throws ResourceNotFoundException {
		Category category = categoryRepository.findById(categoryId)
				.orElseThrow(() -> new ResourceNotFoundException("Category not found"));

		return new CategoryResponse(category);
	}

	@Override
	@Transactional(readOnly = true)
	public List<CustomFieldResponse> getCustomFieldResponse(long moduleId, String tableName)
	        throws ResourceNotFoundException {

	    Long orgId = OrganizationContext.getOrganization().getId();

	    if (orgId == null) {
	        throw new ResourceNotFoundException("Organization not found");
	    }

	    List<ModuleOrganization> orgModules =
	            orgModuleRepository.findByOrganizationIdAndModuleId(orgId, moduleId);

	    List<Long> categoryIds = orgModules.stream()
	            .filter(module -> !module.getDeleted())
	            .map(ModuleOrganization::getCategoryId)
	            .collect(Collectors.toList());

	    List<Category> categories = categoryRepository.findAllById(categoryIds);

	    Category category = categories.stream()
	            .filter(cat -> !cat.getDeleted()
	                    && cat.getMappedWithTable().equalsIgnoreCase(tableName))
	            .findAny()
	            .orElseThrow(() -> new ResourceNotFoundException("No category mapped."));

	    return category.getFields().stream()
	            .filter(field -> !field.getDeleted())
	            .map(CustomFieldResponse::new)
	            .collect(Collectors.toList());
	}

	@Override
	public List<SystemTableResponse> getSystemTables(Long moduleId) throws ResourceNotFoundException {
		List<ermorg.erm.model.SystemTable> systemTables = systemTableRepository.findAllByModuleId(moduleId);
		
		// Return all system tables without filtering based on mapping status
		return systemTables.stream()
				.filter(table -> !table.getDeleted())
				.map(table -> new SystemTableResponse(table))
				.collect(Collectors.toList());
	}

	@Override
	public SystemTableResponse getSystemTableByName(String tableName) throws ResourceNotFoundException {
		ermorg.erm.model.SystemTable systemTable = systemTableRepository.findByTableName(tableName);
		
		if (systemTable == null || systemTable.getDeleted()) {
			throw new ResourceNotFoundException("System table not found: " + tableName);
		}
		
		// Return system table with all fields without filtering based on mapping status
		return new SystemTableResponse(systemTable);
	}

}
