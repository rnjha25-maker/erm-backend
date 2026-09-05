package ermorg.erm.mapping;

import ermorg.erm.dto.response.KriKpiReviewResponseDTO;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

import static ermorg.erm.mapping.CustomFieldConfig.normalizeKey;

@Component
@RequiredArgsConstructor
public class KriKpiReviewStrategyConfig implements FieldStrategy {

    private final FieldMapperUtils fieldMapperUtils;

    @SuppressWarnings({"rawtypes", "unchecked"})
    private final Map<String, Function<Object, Object>> strategies =
            (Map) AutoStrategyBuilder.build(KriKpiReviewResponseDTO.class, this::buildCustomStrategies);

    @Override
    public ModuleType getModuleType() {
        return ModuleType.KRI_KPI_REVIEW;
    }

    @Override
    public Map<String, Function<Object, Object>> getStrategies() {
        return strategies;
    }

    private Map<String, Function<KriKpiReviewResponseDTO, Object>> buildCustomStrategies() {
        Map<String, Function<KriKpiReviewResponseDTO, Object>> map = new HashMap<>();

        map.put(normalizeKey("subRiskName"),
                r -> fieldMapperUtils.stringify(fieldMapperUtils.resolveSubList(r.getSubRiskIds())));
        map.put(normalizeKey("subRiskIds"),
                r -> fieldMapperUtils.stringify(fieldMapperUtils.resolveSubList(r.getSubRiskIds())));
        map.put(normalizeKey("riskOwner"),
                KriKpiReviewResponseDTO::getRiskOwnerName);
        map.put(normalizeKey("riskOwnerName"),
                KriKpiReviewResponseDTO::getRiskOwnerName);
        map.put(normalizeKey("businessFunction"),
                r -> fieldMapperUtils.resolveDepartmentFromObject(r.getDepartmentName()));
        map.put(normalizeKey("departmentFunction"),
                r -> fieldMapperUtils.resolveDepartmentFromObject(r.getDepartmentName()));
        map.put(normalizeKey("departmentName"),
                r -> fieldMapperUtils.resolveDepartmentFromObject(r.getDepartmentName()));
        map.put(normalizeKey("stakeholderDepartments"),
                r -> fieldMapperUtils.resolveDepartmentFromObject(r.getDepartmentName()));
        map.put(normalizeKey("reporting"),
                r -> fieldMapperUtils.resolveUser(r.getReporting()));
        map.put(normalizeKey("kriEvaluationBy"),
                KriKpiReviewResponseDTO::getKriEvaluationByName);
        map.put(normalizeKey("kriEvaluationByName"),
                KriKpiReviewResponseDTO::getKriEvaluationByName);
        map.put(normalizeKey("riskId"),    KriKpiReviewResponseDTO::getRiskTitle);
        map.put(normalizeKey("risk"),      KriKpiReviewResponseDTO::getRiskTitle);
        map.put(normalizeKey("riskTitle"), KriKpiReviewResponseDTO::getRiskTitle);
        map.put(normalizeKey("riskAssessmentId"), KriKpiReviewResponseDTO::getRiskTitle);
        map.put(normalizeKey("riskAssessment"),   KriKpiReviewResponseDTO::getRiskTitle);
        map.put(normalizeKey("kri"), KriKpiReviewResponseDTO::getKeyRiskIndicatorKri);
        map.put(normalizeKey("keyRiskIndicator"), KriKpiReviewResponseDTO::getKeyRiskIndicatorKri);
        map.put(normalizeKey("keyRiskIndicatorKri"), KriKpiReviewResponseDTO::getKeyRiskIndicatorKri);
        map.put(normalizeKey("riskAppetite"), KriKpiReviewResponseDTO::getRiskAppetite);
        map.put(normalizeKey("riskAppetiteStatus"), KriKpiReviewResponseDTO::getRiskAppetiteStatus);
        map.put(normalizeKey("valueUnit"),
                KriKpiReviewResponseDTO::getUnitOfMeasurement);
        map.put(normalizeKey("unitOfMeasurement"),
                KriKpiReviewResponseDTO::getUnitOfMeasurement);
        map.put(normalizeKey("levelOfMeasurementLevel"),
                KriKpiReviewResponseDTO::getUnitOfMeasurement);
        map.put(normalizeKey("currency"),  KriKpiReviewResponseDTO::getCurrency);

        return map;
    }
}
