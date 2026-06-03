package ermorg.erm.mapping;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

import org.springframework.stereotype.Component;

import ermorg.erm.dto.response.RiskAssessmentResponse;
import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class RiskAssessmentStrategyConfig implements FieldStrategy {

    private final FieldMapperUtils fieldMapperUtils;

    @SuppressWarnings({"rawtypes", "unchecked"})
    private final Map<String, Function<Object, Object>> strategies =
            (Map) AutoStrategyBuilder.build(RiskAssessmentResponse.class, this::buildCustomStrategies);

    @Override
    public ModuleType getModuleType() {
        return ModuleType.RISK_ASSESSMENT;
    }

    @Override
    public Map<String, Function<Object, Object>> getStrategies() {
        return strategies;
    }

    private Map<String, Function<RiskAssessmentResponse, Object>> buildCustomStrategies() {
        Map<String, Function<RiskAssessmentResponse, Object>> map = new HashMap<>();

        map.put(n("riskAssessmentBy"),
                r -> fieldMapperUtils.resolveUserFromObject(r.getRiskAssessmentBy()));

        map.put(n("riskReporting"),
                r -> fieldMapperUtils.resolveUserFromObject(r.getRiskReporting()));
        map.put(n("riskTitle"),RiskAssessmentResponse::getRiskTitle);
        map.put(n("subRiskIds"),RiskAssessmentResponse::getSubRiskName);
        map.put(n("riskId"), RiskAssessmentResponse::getRiskTitle);
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
