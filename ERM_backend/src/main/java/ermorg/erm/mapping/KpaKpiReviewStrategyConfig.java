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
                r -> fieldMapperUtils.resolveUser(r.getBusinessFunctionalOwner()));
        map.put(normalizeKey("evaluationBy"),
                r -> fieldMapperUtils.resolveUser(r.getEvaluationBy()));
        map.put(normalizeKey("targets"),  KpaKpiReviewResponseDTO::getTargetValue);
        map.put(normalizeKey("valueUnit"),
                r -> r.getValueUnit() != null ? r.getValueUnit().getLabel() : null);
        map.put(normalizeKey("currency"), KpaKpiReviewResponseDTO::getCurrency);

        return map;
    }
}
