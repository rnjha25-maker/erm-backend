package ermorg.erm.mapping;

import ermorg.erm.dto.response.KpaKpiReviewResponseDTO;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

import static ermorg.erm.mapping.CustomFieldConfig.normalizeKey;

@Component
@RequiredArgsConstructor
public class KpaKpiReviewStrategyConfig implements FieldStrategy {

    private final FieldMapperUtils fieldMapperUtils;

    @SuppressWarnings({"rawtypes", "unchecked"})
    private final Map<String, Function<Object, Object>> strategies =
            (Map) AutoStrategyBuilder.build(KpaKpiReviewResponseDTO.class, this::buildCustomStrategies);

    @Override
    public ModuleType getModuleType() {
        return ModuleType.KPA_KPI_REVIEW;
    }

    @Override
    public Map<String, Function<Object, Object>> getStrategies() {
        return strategies;
    }

    private Map<String, Function<KpaKpiReviewResponseDTO, Object>> buildCustomStrategies() {
        Map<String, Function<KpaKpiReviewResponseDTO, Object>> map = new HashMap<>();

        map.put(normalizeKey("businessFunctionalOwner"),
                KpaKpiReviewResponseDTO::getOwnerName);
        map.put(normalizeKey("functionalOwner"),
                KpaKpiReviewResponseDTO::getOwnerName);
        map.put(normalizeKey("functionalOwnerName"),
                KpaKpiReviewResponseDTO::getOwnerName);
        map.put(normalizeKey("ownerId"),
                KpaKpiReviewResponseDTO::getOwnerName);
        map.put(normalizeKey("owner"),
                KpaKpiReviewResponseDTO::getOwnerName);
        map.put(normalizeKey("businessFunction"),
                KpaKpiReviewResponseDTO::getDepartmentName);
        map.put(normalizeKey("departmentFunction"),
                KpaKpiReviewResponseDTO::getDepartmentName);
        map.put(normalizeKey("departmentName"),
                KpaKpiReviewResponseDTO::getDepartmentName);
        map.put(normalizeKey("department"),
                KpaKpiReviewResponseDTO::getDepartmentName);
        map.put(normalizeKey("stakeholderDepartments"),
                KpaKpiReviewResponseDTO::getDepartmentName);
        map.put(normalizeKey("evaluationBy"),
                KpaKpiReviewResponseDTO::getEvaluationByName);
        map.put(normalizeKey("evaluationByNo"),
                KpaKpiReviewResponseDTO::getEvaluationByName);
        map.put(normalizeKey("evaluationByName"),
                KpaKpiReviewResponseDTO::getEvaluationByName);
        map.put(normalizeKey("kpiEvaluationBy"),
                KpaKpiReviewResponseDTO::getEvaluationByName);
        map.put(normalizeKey("kpiEvaluationByName"),
                KpaKpiReviewResponseDTO::getEvaluationByName);
        map.put(normalizeKey("targets"),  KpaKpiReviewResponseDTO::getTargetValue);
        map.put(normalizeKey("kpa"), KpaKpiReviewResponseDTO::getKpa);
        map.put(normalizeKey("keyPerformanceArea"), KpaKpiReviewResponseDTO::getKpa);
        map.put(normalizeKey("kpi"), KpaKpiReviewResponseDTO::getKeyPerformanceIndicator);
        map.put(normalizeKey("keyPerformanceIndicator"), KpaKpiReviewResponseDTO::getKeyPerformanceIndicator);
        map.put(normalizeKey("keyPerformanceIndicators"), KpaKpiReviewResponseDTO::getKeyPerformanceIndicator);
        map.put(normalizeKey("levelOfMeasurementLevel"),
                KpaKpiReviewResponseDTO::getUnitOfMeasurement);
        map.put(normalizeKey("valueUnit"),
                KpaKpiReviewResponseDTO::getUnitOfMeasurement);
        map.put(normalizeKey("unitOfMeasurement"),
                KpaKpiReviewResponseDTO::getUnitOfMeasurement);
        map.put(normalizeKey("currency"), KpaKpiReviewResponseDTO::getCurrency);

        return map;
    }
}
