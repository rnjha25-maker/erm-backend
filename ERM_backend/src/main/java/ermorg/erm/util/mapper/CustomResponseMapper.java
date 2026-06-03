package ermorg.erm.util.mapper;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import ermorg.erm.dto.response.CustomFieldResponse;
import ermorg.erm.dto.response.CustomResponse;
import ermorg.erm.exception.ResourceNotFoundException;
import ermorg.erm.mapping.CustomFieldConfig;
import ermorg.erm.mapping.GenericFieldMapper;
import ermorg.erm.mapping.ModuleType;
import ermorg.erm.mapping.FieldMapperUtils;
import ermorg.erm.service.IFieldService;

import lombok.extern.slf4j.Slf4j;

@Component
@Slf4j
public class CustomResponseMapper {

    private static final Map<String, ModuleType> TABLE_TO_MODULE = Map.of(
            "risk", ModuleType.RISK,
            "riskassessment", ModuleType.RISK_ASSESSMENT,
            "riskcontrol", ModuleType.RISK_CONTROL,
            "risktreatment", ModuleType.RISK_TREATMENT,
            "riskreview", ModuleType.RISK_REVIEW,
            "krikpireview", ModuleType.KRI_KPI_REVIEW,
            "ermmaturity", ModuleType.ERM_MATURITY,
            "escalation", ModuleType.ESCALATION
    );

    @Autowired
    private IFieldService fieldService;

    @Autowired
    private GenericFieldMapper genericFieldMapper;

    @Autowired
    private FieldMapperUtils fieldMapperUtils;

    public List<CustomResponse> map(String tableName, Long moduleId, Object object, boolean isGrid) {

        if (object == null) {
            return Collections.emptyList();
        }

        List<CustomFieldResponse> fields = getFields(moduleId, tableName);
        if (fields == null || fields.isEmpty()) {
            return Collections.emptyList();
        }

        // ✅ filter once (avoid multiple streams)
        List<CustomFieldResponse> filteredFields = isGrid
                ? fields.stream().filter(CustomFieldResponse::getShowGridColumn).toList()
                : fields;

        if (filteredFields.isEmpty()) {
            return Collections.emptyList();
        }

        ModuleType moduleType = resolveModuleType(tableName);

        // ✅ strategy-based mapping
        if (moduleType != null && genericFieldMapper.hasStrategy(moduleType)) {

            List<CustomFieldConfig> configs = filteredFields.stream()
                    .map(CustomFieldConfig::new)
                    .toList();

            Map<String, Object> fieldValues =
                    genericFieldMapper.mapFields(object, configs, moduleType);

            return filteredFields.stream()
                    .map(field -> buildResponse(field, fieldValues.get(field.getFieldName())))
                    .collect(Collectors.toList());
        }

        // ✅ fallback mapping
        return filteredFields.stream()
                .map(field -> mapFallback(object, field, tableName))
                .filter(Objects::nonNull)
                .collect(Collectors.toList());
    }

    private CustomResponse buildResponse(CustomFieldResponse field, Object value) {
        CustomResponse response = new CustomResponse();
        response.setFieldName(field.getFieldName());
        response.setFieldType(field.getFieldType());
        response.setValue(fieldMapperUtils.stringify(value));
        return response;
    }

    private CustomResponse mapFallback(Object object, CustomFieldResponse field, String tableName) {
        try {
            return CustomResponseMapperUtil.map(object, field, tableName);
        } catch (IllegalArgumentException | IllegalAccessException e) {
            log.error("Error mapping field: {}", field.getFieldName(), e);
            return null;
        }
    }

    private List<CustomFieldResponse> getFields(Long moduleId, String tableName) {
        try {
            return fieldService.getCustomFieldResponse(moduleId, tableName);
        } catch (ResourceNotFoundException e) {
            log.error("Error fetching custom fields for moduleId: {}, tableName: {}", moduleId, tableName, e);
            throw new RuntimeException(e);
        }
    }

    private ModuleType resolveModuleType(String tableName) {
        if (tableName == null || tableName.isBlank()) {
            return null;
        }
        String normalized = tableName.trim()
                .toLowerCase()
                .replaceAll("[^a-z0-9]", "");

        return TABLE_TO_MODULE.get(normalized);
    }
}