package ermorg.erm.mapping;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

import org.springframework.stereotype.Component;

import ermorg.erm.dto.response.KriKpiReviewResponseDTO;
import lombok.RequiredArgsConstructor;

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

        map.put(n("subRiskName"),
                r -> fieldMapperUtils.stringify(fieldMapperUtils.resolveSubList(r.getSubRiskIds())));

        map.put(n("riskOwner"),
                r -> fieldMapperUtils.resolveUser(r.getRiskOwner()));

        map.put(n("reporting"),
                r -> fieldMapperUtils.resolveUser(r.getReporting()));

        map.put(n("kriEvaluationBy"),
                r -> fieldMapperUtils.resolveUser(r.getKriEvaluationBy()));
        map.put(n("subRiskIds"),
                r -> fieldMapperUtils.stringify(fieldMapperUtils.resolveSubList(r.getSubRiskIds())));
        map.put(n("riskId"),
                KriKpiReviewResponseDTO::getRiskTitle);

        map.put(n("reporting"),
                KriKpiReviewResponseDTO::getReporting);

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
