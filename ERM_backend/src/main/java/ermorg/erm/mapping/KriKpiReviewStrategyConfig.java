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
                r -> fieldMapperUtils.resolveUser(r.getRiskOwner()));
        map.put(normalizeKey("reporting"),
                r -> fieldMapperUtils.resolveUser(r.getReporting()));
        map.put(normalizeKey("kriEvaluationBy"),
                r -> fieldMapperUtils.resolveUser(r.getKriEvaluationBy()));
        map.put(normalizeKey("riskId"),    KriKpiReviewResponseDTO::getRiskTitle);
        map.put(normalizeKey("valueUnit"),
                r -> r.getValueUnit() != null ? r.getValueUnit().getLabel() : null);
        map.put(normalizeKey("currency"),  KriKpiReviewResponseDTO::getCurrency);

        return map;
    }
}
