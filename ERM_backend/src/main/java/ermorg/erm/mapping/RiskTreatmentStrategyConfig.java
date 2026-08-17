package ermorg.erm.mapping;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

import org.springframework.stereotype.Component;

import ermorg.erm.dto.response.RiskResponseTreatmentResponse;
import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class RiskTreatmentStrategyConfig implements FieldStrategy {

    private final FieldMapperUtils fieldMapperUtils;

    @SuppressWarnings({"rawtypes", "unchecked"})
    private final Map<String, Function<Object, Object>> strategies =
            (Map) AutoStrategyBuilder.build(RiskResponseTreatmentResponse.class, this::buildCustomStrategies);

    @Override
    public ModuleType getModuleType() {
        return ModuleType.RISK_TREATMENT;
    }

    @Override
    public Map<String, Function<Object, Object>> getStrategies() {
        return strategies;
    }

    private Map<String, Function<RiskResponseTreatmentResponse, Object>> buildCustomStrategies() {
        Map<String, Function<RiskResponseTreatmentResponse, Object>> map = new HashMap<>();

        map.put(n("subRiskName"),
                r -> fieldMapperUtils.stringify(fieldMapperUtils.resolveSubList(r.getSubRisk())));
        map.put(n("subRiskIds"),
                r -> fieldMapperUtils.stringify(fieldMapperUtils.resolveSubList(r.getSubRisk())));
        map.put(n("riskSubIds"),
                r -> fieldMapperUtils.stringify(fieldMapperUtils.resolveSubList(r.getSubRisk())));
        
        map.put(n("controlEvaluationBy"),
                r -> fieldMapperUtils.resolveUserFromObject(r.getControlEvaluationBy()));

        map.put(n("riskReporting"),
                r -> fieldMapperUtils.resolveUser(r.getRiskReporting()));
        map.put(n("riskId"), RiskResponseTreatmentResponse::getRiskTitle);

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
