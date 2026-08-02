package ermorg.erm.serviceimpl;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import ermorg.erm.dto.response.CategoryListResponse;
import ermorg.erm.dto.response.CategoryResponse;
import ermorg.erm.dto.response.CustomFieldResponse;
import ermorg.erm.dto.response.SystemFieldResponse;
import ermorg.erm.dto.response.SystemTableResponse;
import ermorg.erm.exception.ResourceNotFoundException;
import ermorg.erm.model.Category;
import ermorg.erm.model.FieldOption;
import ermorg.erm.model.ModuleOrganization;
import ermorg.erm.repository.CategoryRepository;
import ermorg.erm.repository.CustomFieldRepository;
import ermorg.erm.repository.FieldOptionRepository;
import ermorg.erm.repository.ModuleRepository;
import ermorg.erm.repository.OrgModuleRepository;
import ermorg.erm.repository.OrganizationRepository;
import ermorg.erm.repository.SystemFieldRepository;
import ermorg.erm.repository.SystemTableRepository;
import ermorg.erm.service.IFieldService;
import ermorg.erm.util.OrganizationContext;

@Service
public class FieldService implements IFieldService {

    @Autowired private ModuleRepository       moduleRepository;
    @Autowired private SystemTableRepository  systemTableRepository;
    @Autowired private SystemFieldRepository  systemFieldRepository;
    @Autowired private CategoryRepository     categoryRepository;
    @Autowired private CustomFieldRepository  customFieldRepository;
    @Autowired private OrgModuleRepository    orgModuleRepository;
    @Autowired private OrganizationRepository organizationRepository;
    @Autowired private FieldOptionRepository  fieldOptionRepository;

    // -------------------------------------------------------------------------
    // Categories
    // -------------------------------------------------------------------------

    @Override
    @Transactional(readOnly = true)
    public List<CategoryListResponse> getAllCategories(Long moduleId) throws ResourceNotFoundException {
        Long orgId = OrganizationContext.getOrganization().getId();
        if (orgId == null) {
            throw new ResourceNotFoundException("Organization not found");
        }
        return categoryRepository.findAllByOrgAndModule(orgId, moduleId).stream()
                .map(CategoryListResponse::new)
                .collect(Collectors.toList());
    }

    @Override
    public CategoryResponse getCategory(Long categoryId) throws ResourceNotFoundException {
        Category category = categoryRepository.findById(categoryId)
                .orElseThrow(() -> new ResourceNotFoundException("Category not found"));
        return new CategoryResponse(category);
    }

    // -------------------------------------------------------------------------
    // Custom fields (used by dynamic response mapper)
    // -------------------------------------------------------------------------

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
                .filter(m -> Boolean.FALSE.equals(m.getDeleted()) && m.getCategoryId() != null)
                .map(ModuleOrganization::getCategoryId)
                .collect(Collectors.toList());

        List<Category> categories = categoryRepository.findAllById(categoryIds).stream()
                .filter(cat -> !Boolean.TRUE.equals(cat.getDeleted())
                        && cat.getMappedWithTable() != null
                        && cat.getMappedWithTable().equalsIgnoreCase(tableName))
                .collect(Collectors.toList());

        if (categories.isEmpty()) {
            categories = categoryRepository
                    .findAllByModuleIdAndMappedWithTableAndDeletedFalse(moduleId, tableName);
        }

        if (categories.isEmpty()) {
            throw new ResourceNotFoundException("No category mapped.");
        }

        // Collect all system-field IDs that are DROPDOWN type so we can batch-load options
        List<CustomFieldResponse> responses = categories.stream()
                .flatMap(cat -> cat.getFields().stream())
                .filter(field -> !Boolean.TRUE.equals(field.getDeleted()))
                .map(CustomFieldResponse::new)
                .collect(Collectors.toList());

        enrichWithOptions(responses, tableName);
        return responses;
    }

    /**
     * Batch-loads dropdown options for all DROPDOWN/MULTI_SELECT fields in one query
     * per unique system-field name, then enriches each response.
     * No N+1 — one query per distinct field name that has options.
     */
    private void enrichWithOptions(List<CustomFieldResponse> responses, String tableName) {
        // Collect distinct system field names that are dropdown type
        List<String> dropdownFieldNames = responses.stream()
                .filter(r -> isDropdownType(r.getFieldType()))
                .map(CustomFieldResponse::getSystemFieldName)
                .filter(name -> name != null && !name.isBlank())
                .distinct()
                .collect(Collectors.toList());

        if (dropdownFieldNames.isEmpty()) {
            return;
        }

        // Build a map: systemFieldName -> List<FieldOption>
        Map<String, List<FieldOption>> optionsByField = dropdownFieldNames.stream()
                .collect(Collectors.toMap(
                        name -> name,
                        name -> fieldOptionRepository.findActiveByFieldNameAndTable(name, tableName)
                ));

        // Enrich each response
        responses.forEach(r -> {
            if (isDropdownType(r.getFieldType()) && r.getSystemFieldName() != null) {
                List<FieldOption> opts = optionsByField.getOrDefault(
                        r.getSystemFieldName(), Collections.emptyList());
                if (!opts.isEmpty()) {
                    r.setOptionsFromFieldOptions(opts);
                }
            }
        });
    }

    private boolean isDropdownType(String fieldType) {
        if (fieldType == null) return false;
        String t = fieldType.toLowerCase().replaceAll("[^a-z]", "");
        return t.contains("dropdown") || t.contains("multiselect") || t.contains("select");
    }

    // -------------------------------------------------------------------------
    // System tables & fields (used by field-configuration UI)
    // -------------------------------------------------------------------------

    @Override
    @Transactional(readOnly = true)
    public List<SystemTableResponse> getSystemTables(Long moduleId) throws ResourceNotFoundException {
        return systemTableRepository.findAllByModuleId(moduleId).stream()
                .filter(table -> !table.getDeleted())
                .map(SystemTableResponse::new)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public SystemTableResponse getSystemTableByName(String tableName) throws ResourceNotFoundException {
        ermorg.erm.model.SystemTable systemTable = systemTableRepository.findByTableName(tableName);
        if (systemTable == null || systemTable.getDeleted()) {
            throw new ResourceNotFoundException("System table not found: " + tableName);
        }
        return new SystemTableResponse(systemTable);
    }

    // -------------------------------------------------------------------------
    // Field options API (new — enables metadata-driven dropdown rendering)
    // -------------------------------------------------------------------------

    @Override
    @Transactional(readOnly = true)
    public List<SystemFieldResponse.FieldOptionResponse> getFieldOptions(
            String fieldName, String tableName) throws ResourceNotFoundException {

        List<FieldOption> options =
                fieldOptionRepository.findActiveByFieldNameAndTable(fieldName, tableName);
        return options.stream()
                .map(SystemFieldResponse.FieldOptionResponse::new)
                .collect(Collectors.toList());
    }
}
