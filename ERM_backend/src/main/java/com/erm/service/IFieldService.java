package com.erm.service;

import java.util.List;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.erm.dto.response.CategoryListResponse;
import com.erm.dto.response.CategoryResponse;
import com.erm.dto.response.CustomFieldResponse;
import com.erm.dto.response.SystemTableResponse;
import com.erm.exception.ResourceNotFoundException;
import com.erm.model.Category;
import com.erm.model.ModuleOrganization;
import com.erm.model.Organization;
import com.erm.repository.CategoryRepository;
import com.erm.repository.CustomFieldRepository;
import com.erm.repository.ModuleRepository;
import com.erm.repository.OrgModuleRepository;
import com.erm.repository.SystemFieldRepository;
import com.erm.repository.SystemTableRepository;
import com.erm.util.OrganizationContext;

@Service
public class IFieldService implements FieldService {

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
	@Override
	@Transactional(readOnly = true)
	public List<CategoryListResponse> getAllCategories(Long moduleId) throws ResourceNotFoundException {

		Organization organization = OrganizationContext.getOrganization();

		if (organization == null) {
			throw new ResourceNotFoundException("Organization not found");
		}

		List<Long> categoryIds = organization.getModules().stream()
				.filter(module -> !module.getDeleted() && module.getModuleId().equals(moduleId))
				.map(module -> module.getCategoryId()).collect(Collectors.toList());

		List<Category> categoriries = categoryRepository.findAllById(categoryIds);

		return categoriries.stream().filter(category -> !category.getDeleted())
				.map(category -> new CategoryListResponse(category)).collect(Collectors.toList());

	}

	@Override
	public CategoryResponse getCategory(Long categoryId) throws ResourceNotFoundException {
		Category category = categoryRepository.findById(categoryId)
				.orElseThrow(() -> new ResourceNotFoundException("Category not found"));

		return new CategoryResponse(category);
	}

	@Override
	public List<CustomFieldResponse> getCustomFieldResponse(long moduleId, String tableName)
			throws ResourceNotFoundException {
		Organization organization = OrganizationContext.getOrganization();

		if (organization == null) {
			throw new ResourceNotFoundException("Organization not found");
		}

		List<ModuleOrganization> ordModules = orgModuleRepository.findByOrganizationIdAndModuleId(organization.getId(), moduleId);
		List<Long> categoryIds = ordModules.stream()
				.filter(module -> !module.getDeleted() && module.getModuleId().equals(moduleId))
				.map(module -> module.getCategoryId()).collect(Collectors.toList());

		List<Category> categoriries = categoryRepository.findAllById(categoryIds);

		Category category = categoriries.stream().filter(cat -> cat.getMappedWithTable().equalsIgnoreCase(tableName))
				.findAny().orElseThrow(() -> new ResourceNotFoundException("No category mapped."));

		return category.getFields().stream()
				.filter(field -> !field.getDeleted())
				.map(field -> new CustomFieldResponse(field)).collect(Collectors.toList());
	}

	@Override
	public List<SystemTableResponse> getSystemTables(Long moduleId) throws ResourceNotFoundException {
		List<com.erm.model.SystemTable> systemTables = systemTableRepository.findAllByModuleId(moduleId);
		
		// Return all system tables without filtering based on mapping status
		return systemTables.stream()
				.filter(table -> !table.getDeleted())
				.map(table -> new SystemTableResponse(table))
				.collect(Collectors.toList());
	}

	@Override
	public SystemTableResponse getSystemTableByName(String tableName) throws ResourceNotFoundException {
		com.erm.model.SystemTable systemTable = systemTableRepository.findByTableName(tableName);
		
		if (systemTable == null || systemTable.getDeleted()) {
			throw new ResourceNotFoundException("System table not found: " + tableName);
		}
		
		// Return system table with all fields without filtering based on mapping status
		return new SystemTableResponse(systemTable);
	}

}
