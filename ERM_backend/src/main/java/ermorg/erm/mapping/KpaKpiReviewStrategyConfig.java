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
        map.put(normalizeKey("riskOwner"),
                KpaKpiReviewResponseDTO::getOwnerName);
        map.put(normalizeKey("riskOwnerName"),
                KpaKpiReviewResponseDTO::getOwnerName);
        map.put(normalizeKey("businessFunction"),
                r -> fieldMapperUtils.resolveDepartmentFromObject(r.getDepartmentName()));
        map.put(normalizeKey("departmentFunction"),
                r -> fieldMapperUtils.resolveDepartmentFromObject(r.getDepartmentName()));
        map.put(normalizeKey("departmentName"),
                r -> fieldMapperUtils.resolveDepartmentFromObject(r.getDepartmentName()));
        map.put(normalizeKey("department"),
                r -> fieldMapperUtils.resolveDepartmentFromObject(r.getDepartmentName()));
        map.put(normalizeKey("stakeholderDepartments"),
                r -> fieldMapperUtils.resolveDepartmentFromObject(r.getDepartmentName()));
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
        map.put(normalizeKey("kriEvaluationBy"),
                KpaKpiReviewResponseDTO::getEvaluationByName);
        map.put(normalizeKey("kriEvaluationByName"),
                KpaKpiReviewResponseDTO::getEvaluationByName);
        map.put(normalizeKey("keyRiskEvaluationBy"),
                KpaKpiReviewResponseDTO::getEvaluationByName);
        map.put(normalizeKey("kpiEvaluationFrequency"),
                KpaKpiReviewResponseDTO::getKpiEvaluationFrequency);
        map.put(normalizeKey("kriEvaluationFrequency"),
                KpaKpiReviewResponseDTO::getKpiEvaluationFrequency);
        map.put(normalizeKey("keyRiskEvaluationFrequency"),
                KpaKpiReviewResponseDTO::getKpiEvaluationFrequency);
        map.put(normalizeKey("reporting"),
                KpaKpiReviewResponseDTO::getReportingName);
        map.put(normalizeKey("reportingName"),
                KpaKpiReviewResponseDTO::getReportingName);
        map.put(normalizeKey("reportingId"),
                KpaKpiReviewResponseDTO::getReportingName);
        map.put(normalizeKey("target"), this::displayTarget);
        map.put(normalizeKey("targets"), this::displayTarget);
        map.put(normalizeKey("targetValue"), this::displayTarget);
        map.put(normalizeKey("kpa"), KpaKpiReviewResponseDTO::getKpa);
        map.put(normalizeKey("keyPerformanceArea"), KpaKpiReviewResponseDTO::getKpa);
        map.put(normalizeKey("riskTitle"), KpaKpiReviewResponseDTO::getRiskTitle);
        map.put(normalizeKey("risk"), KpaKpiReviewResponseDTO::getRiskTitle);
        map.put(normalizeKey("riskId"), KpaKpiReviewResponseDTO::getRiskTitle);
        map.put(normalizeKey("riskSubTitle"), KpaKpiReviewResponseDTO::getRiskSubTitle);
        map.put(normalizeKey("riskSubTitleName"), KpaKpiReviewResponseDTO::getRiskSubTitle);
        map.put(normalizeKey("kpi"), KpaKpiReviewResponseDTO::getKeyPerformanceIndicator);
        map.put(normalizeKey("keyPerformanceIndicator"), KpaKpiReviewResponseDTO::getKeyPerformanceIndicator);
        map.put(normalizeKey("keyPerformanceIndicators"), KpaKpiReviewResponseDTO::getKeyPerformanceIndicator);
        map.put(normalizeKey("kri"), KpaKpiReviewResponseDTO::getKeyRiskIndicator);
        map.put(normalizeKey("keyRiskIndicator"), KpaKpiReviewResponseDTO::getKeyRiskIndicator);
        map.put(normalizeKey("keyRiskIndicatorKri"), KpaKpiReviewResponseDTO::getKeyRiskIndicator);
        map.put(normalizeKey("typesOfKeyRiskIndicator"), KpaKpiReviewResponseDTO::getTypesOfKeyRiskIndicator);
        map.put(normalizeKey("typesOfKeyRiskIndicatorKri"), KpaKpiReviewResponseDTO::getTypesOfKeyRiskIndicator);
        map.put(normalizeKey("riskAppetite"), KpaKpiReviewResponseDTO::getRiskAppetite);
        map.put(normalizeKey("riskAppetiteStatus"), KpaKpiReviewResponseDTO::getRiskAppetiteStatus);
        map.put(normalizeKey("riskAppetiteLevel"), KpaKpiReviewResponseDTO::getRiskAppetiteLevel);
        map.put(normalizeKey("riskAcceptanceLevel"), KpaKpiReviewResponseDTO::getRiskAppetiteLevel);
        map.put(normalizeKey("riskToleranceRangeMinValue"), KpaKpiReviewResponseDTO::getRiskToleranceRangeMinValue);
        map.put(normalizeKey("riskToleranceRangeMaxValue"), KpaKpiReviewResponseDTO::getRiskToleranceRangeMaxValue);
        map.put(normalizeKey("levelOfMeasurementLevel"),
                KpaKpiReviewResponseDTO::getUnitOfMeasurement);
        map.put(normalizeKey("valueUnit"),
                KpaKpiReviewResponseDTO::getUnitOfMeasurement);
        map.put(normalizeKey("unitOfMeasurement"),
                KpaKpiReviewResponseDTO::getUnitOfMeasurement);
        map.put(normalizeKey("currency"), KpaKpiReviewResponseDTO::getCurrency);

        return map;
    }

    private Object displayTarget(KpaKpiReviewResponseDTO response) {
        if (response.getTargetValue() != null) {
            return response.getTargetValue();
        }
        if (response.getTargets() != null && !response.getTargets().isBlank()) {
            return response.getTargets();
        }
        return response.getTarget();
    }
}
