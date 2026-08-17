package ermorg.erm.util.mapper;

import java.util.Collections;
import java.util.Collection;
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
            "kpakpireview", ModuleType.KPA_KPI_REVIEW,
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
                ? fields.stream().filter(field -> Boolean.TRUE.equals(field.getShowGridColumn())).toList()
                : fields;

        if (filteredFields.isEmpty()) {
            return Collections.emptyList();
        }

        ModuleType moduleType = resolveModuleType(tableName);

        // ✅ strategy-based mapping
        if (genericFieldMapper != null) {

            List<CustomFieldConfig> configs = filteredFields.stream()
                    .map(CustomFieldConfig::new)
                    .toList();

            Map<String, Object> fieldValues =
                    genericFieldMapper.mapFields(object, configs, moduleType);

            return filteredFields.stream()
                    .map(field -> {
                        CustomFieldConfig config = new CustomFieldConfig(field);
                        Object value = resolveFieldValue(fieldValues, config, field);
                        if (value != null) {
                            return buildResponse(field, value);
                        }

                        CustomResponse fallback = mapFallback(object, field, tableName);
                        if (fallback != null && fallback.getValue() != null) {
                            return fallback;
                        }

                        return buildResponse(field, null);
                    })
                    .collect(Collectors.toList());
        }

        // ✅ fallback mapping
        return filteredFields.stream()
                .map(field -> mapFallback(object, field, tableName))
                .filter(Objects::nonNull)
                .collect(Collectors.toList());
    }

    private Object resolveFieldValue(Map<String, Object> fieldValues, CustomFieldConfig config, CustomFieldResponse field) {
        if (fieldValues == null || config == null) {
            return null;
        }

        String canonicalKey = config.normalizedKey();
        if (canonicalKey != null && !canonicalKey.isBlank() && fieldValues.containsKey(canonicalKey)) {
            return fieldValues.get(canonicalKey);
        }

        if (field != null && field.getFieldName() != null && fieldValues.containsKey(field.getFieldName())) {
            return fieldValues.get(field.getFieldName());
        }

        if (field != null && field.getSystemFieldName() != null) {
            String systemFieldKey = CustomFieldConfig.normalizeKey(field.getSystemFieldName());
            if (!systemFieldKey.isBlank() && fieldValues.containsKey(systemFieldKey)) {
                return fieldValues.get(systemFieldKey);
            }
            if (fieldValues.containsKey(field.getSystemFieldName())) {
                return fieldValues.get(field.getSystemFieldName());
            }
        }

        return null;
    }

    private CustomResponse buildResponse(CustomFieldResponse field, Object value) {
        CustomResponse response = new CustomResponse();
        response.setFieldName(field.getFieldName());
        response.setFieldType(field.getFieldType());
        response.setValue(fieldMapperUtils.stringify(resolveOptionLabels(field, value)));
        return response;
    }

    private Object resolveOptionLabels(CustomFieldResponse field, Object value) {
        if (value == null || field == null || field.getOptions() == null || field.getOptions().isEmpty()) {
            return value;
        }

        if (value instanceof Collection<?> collection) {
            return collection.stream()
                    .map(item -> resolveOptionLabel(field, item))
                    .filter(Objects::nonNull)
                    .toList();
        }

        String text = value.toString().trim();
        if (text.isEmpty()) {
            return value;
        }

        if (looksLikeMultiValue(text)) {
            List<String> labels = splitMultiValue(text).stream()
                    .map(item -> resolveOptionLabel(field, item))
                    .filter(Objects::nonNull)
                    .toList();
            return labels.isEmpty() ? value : labels;
        }

        String label = resolveOptionLabel(field, text);
        return label != null ? label : value;
    }

    private String resolveOptionLabel(CustomFieldResponse field, Object rawValue) {
        if (rawValue == null) {
            return null;
        }

        String value = cleanOptionValue(rawValue.toString());
        if (value.isBlank()) {
            return null;
        }

        return field.getOptions().stream()
                .filter(option -> value.equalsIgnoreCase(cleanOptionValue(option.getValue()))
                        || value.equalsIgnoreCase(cleanOptionValue(option.getLabel())))
                .map(CustomFieldResponse.FieldOptionItem::getLabel)
                .filter(label -> label != null && !label.isBlank())
                .findFirst()
                .orElse(value);
    }

    private boolean looksLikeMultiValue(String value) {
        return value.contains(",") || (value.startsWith("[") && value.endsWith("]"));
    }

    private List<String> splitMultiValue(String value) {
        String text = value.trim();
        if (text.startsWith("[") && text.endsWith("]")) {
            text = text.substring(1, text.length() - 1);
        }
        return java.util.Arrays.stream(text.split(","))
                .map(this::cleanOptionValue)
                .filter(item -> !item.isBlank())
                .toList();
    }

    private String cleanOptionValue(String value) {
        if (value == null) {
            return "";
        }
        String text = value.trim();
        if ((text.startsWith("\"") && text.endsWith("\""))
                || (text.startsWith("'") && text.endsWith("'"))) {
            text = text.substring(1, text.length() - 1).trim();
        }
        return text;
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
