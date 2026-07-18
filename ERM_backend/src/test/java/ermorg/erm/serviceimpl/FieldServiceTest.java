package ermorg.erm.serviceimpl;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import ermorg.erm.dto.response.CustomFieldResponse;
import ermorg.erm.model.Category;
import ermorg.erm.model.CustomField;
import ermorg.erm.model.ModuleOrganization;
import ermorg.erm.model.Modules;
import ermorg.erm.model.Organization;
import ermorg.erm.model.SystemField;
import ermorg.erm.repository.CategoryRepository;
import ermorg.erm.repository.CustomFieldRepository;
import ermorg.erm.repository.ModuleRepository;
import ermorg.erm.repository.OrgModuleRepository;
import ermorg.erm.repository.OrganizationRepository;
import ermorg.erm.repository.SystemFieldRepository;
import ermorg.erm.repository.SystemTableRepository;
import ermorg.erm.util.OrganizationContext;

class FieldServiceTest {

    private final ModuleRepository moduleRepository = mock(ModuleRepository.class);
    private final SystemTableRepository tableRepository = mock(SystemTableRepository.class);
    private final SystemFieldRepository systemFieldRepository = mock(SystemFieldRepository.class);
    private final CategoryRepository categoryRepository = mock(CategoryRepository.class);
    private final CustomFieldRepository customFieldRepository = mock(CustomFieldRepository.class);
    private final OrgModuleRepository orgModuleRepository = mock(OrgModuleRepository.class);
    private final OrganizationRepository organizationRepository = mock(OrganizationRepository.class);

    private final FieldService fieldService = new FieldService();

    @AfterEach
    void tearDown() {
        OrganizationContext.clear();
    }

    @Test
    void getCustomFieldResponse_shouldIncludeFieldsFromAllMatchingCategoriesForTheTable() throws Exception {
        injectDependencies();

        Organization organization = new Organization();
        organization.setId(10L);
        OrganizationContext.setOrganization(organization);

        ModuleOrganization orgModule = new ModuleOrganization();
        orgModule.setDeleted(false);
        orgModule.setCategoryId(100L);

        Category categoryFromOrgModule = new Category();
        categoryFromOrgModule.setId(100L);
        categoryFromOrgModule.setDeleted(false);
        categoryFromOrgModule.setMappedWithTable("kriKpiReview");
        Modules module = new Modules();
        module.setId(1L);
        categoryFromOrgModule.setModule(module);
        CustomField oldField = buildField(1L, "oldField", categoryFromOrgModule);
        categoryFromOrgModule.setFields(new HashSet<>(Set.of(oldField)));

        Category categoryLinkedToModule = new Category();
        categoryLinkedToModule.setId(200L);
        categoryLinkedToModule.setDeleted(false);
        categoryLinkedToModule.setMappedWithTable("kriKpiReview");
        categoryLinkedToModule.setModule(module);
        CustomField newField = buildField(2L, "newField", categoryLinkedToModule);
        categoryLinkedToModule.setFields(new HashSet<>(Set.of(newField)));

        when(orgModuleRepository.findByOrganizationIdAndModuleId(10L, 1L)).thenReturn(List.of(orgModule));
        when(categoryRepository.findAllByOrgAndModule(10L, 1L)).thenReturn(List.of(categoryFromOrgModule));
        when(categoryRepository.findAllByModuleIdAndMappedWithTableAndDeletedFalse(1L, "kriKpiReview"))
                .thenReturn(List.of(categoryLinkedToModule));

        List<CustomFieldResponse> response = fieldService.getCustomFieldResponse(1L, "kriKpiReview");

        assertEquals(2, response.size());
        assertEquals(Set.of("oldField", "newField"), response.stream().map(CustomFieldResponse::getFieldName).collect(java.util.stream.Collectors.toSet()));
    }

    private void injectDependencies() {
//        org.mockito.ReflectionTestUtils.setField(fieldService, "moduleRepository", moduleRepository);
//        org.mockito.ReflectionTestUtils.setField(fieldService, "tableReposity", tableRepository);
//        org.mockito.ReflectionTestUtils.setField(fieldService, "systemFieldRepository", systemFieldRepository);
//        org.mockito.ReflectionTestUtils.setField(fieldService, "categoryRepository", categoryRepository);
//        org.mockito.ReflectionTestUtils.setField(fieldService, "customFieldRepository", customFieldRepository);
//        org.mockito.ReflectionTestUtils.setField(fieldService, "systemTableRepository", tableRepository);
//        org.mockito.ReflectionTestUtils.setField(fieldService, "orgModuleRepository", orgModuleRepository);
//        org.mockito.ReflectionTestUtils.setField(fieldService, "organizationRepository", organizationRepository);
    }

    private CustomField buildField(Long id, String fieldName, Category category) {
        CustomField field = new CustomField();
        field.setId(id);
        field.setFieldName(fieldName);
        field.setDeleted(false);
        field.setCategory(category);
        SystemField systemField = new SystemField();
        systemField.setField(fieldName);
        field.setSystemField(systemField);
        return field;
    }
}
