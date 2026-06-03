package ermorg.erm.mapping;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

import org.springframework.stereotype.Component;

import ermorg.erm.dto.response.EscalationResponse;
import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class EscalationStrategyConfig implements FieldStrategy {

    private final FieldMapperUtils fieldMapperUtils;

    @SuppressWarnings({"rawtypes", "unchecked"})
    private final Map<String, Function<Object, Object>> strategies =
            (Map) AutoStrategyBuilder.build(EscalationResponse.class, this::buildCustomStrategies);

    @Override
    public ModuleType getModuleType() {
        return ModuleType.ESCALATION;
    }

    @Override
    public Map<String, Function<Object, Object>> getStrategies() {
        return strategies;
    }

    private Map<String, Function<EscalationResponse, Object>> buildCustomStrategies() {
        Map<String, Function<EscalationResponse, Object>> map = new HashMap<>();

        map.put(n("escalationBy"),
                r -> fieldMapperUtils.resolveUserFromObject(r.getEscalationBy()));

        map.put(n("assignedToPrimaryResponsible"),
                r -> fieldMapperUtils.resolveUserFromObject(r.getAssignedToPrimaryResponsible()));

        map.put(n("reportingLevel"),
                r -> fieldMapperUtils.resolveUserFromObject(r.getReportingLevel()));

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
