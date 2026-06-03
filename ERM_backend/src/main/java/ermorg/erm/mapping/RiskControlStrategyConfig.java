package ermorg.erm.mapping;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

import org.springframework.stereotype.Component;

import ermorg.erm.dto.response.RiskControlResponse;
import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class RiskControlStrategyConfig implements FieldStrategy {

    private final FieldMapperUtils fieldMapperUtils;

    @SuppressWarnings({"rawtypes", "unchecked"})
    private final Map<String, Function<Object, Object>> strategies =
            (Map) AutoStrategyBuilder.build(RiskControlResponse.class, this::buildCustomStrategies);

    @Override
    public ModuleType getModuleType() {
        return ModuleType.RISK_CONTROL;
    }

    @Override
    public Map<String, Function<Object, Object>> getStrategies() {
        return strategies;
    }

    private Map<String, Function<RiskControlResponse, Object>> buildCustomStrategies() {
        Map<String, Function<RiskControlResponse, Object>> map = new HashMap<>();

        map.put(n("subRiskName"),
                r -> fieldMapperUtils.stringify(fieldMapperUtils.resolveSubList(r.getRiskSubs())));

        map.put(n("primaryResponsible"),
                r -> fieldMapperUtils.resolveUser(r.getPrimaryResponsible()));

        map.put(n("approver"),
                r -> fieldMapperUtils.resolveUser(r.getApprover()));

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
