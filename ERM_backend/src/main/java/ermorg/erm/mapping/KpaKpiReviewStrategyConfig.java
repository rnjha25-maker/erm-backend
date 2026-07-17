package ermorg.erm.mapping;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

import org.springframework.stereotype.Component;

import ermorg.erm.dto.response.KpaKpiReviewResponseDTO;
import lombok.RequiredArgsConstructor;

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

        map.put(n("businessFunctionalOwner"), r -> fieldMapperUtils.resolveUser(r.getBusinessFunctionalOwner()));
        map.put(n("evaluationBy"), r -> fieldMapperUtils.resolveUser(r.getEvaluationBy()));
        map.put(n("targets"), KpaKpiReviewResponseDTO::getTargetValue);

        return map;
    }

    private static String n(String value) {
        if (value == null) return "";
        StringBuilder sb = new StringBuilder(value.length());
        for (char c : value.toCharArray()) {
            if (Character.isLetterOrDigit(c)) {
                sb.append(Character.toLowerCase(c));
            }
        }
        return sb.toString();
    }
}
