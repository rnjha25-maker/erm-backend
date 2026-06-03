package ermorg.erm.mapping;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

import org.springframework.stereotype.Component;

import ermorg.erm.dto.response.ErmMaturityResponse;
import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class ErmMaturityStrategyConfig implements FieldStrategy {

    private final FieldMapperUtils fieldMapperUtils;

    @SuppressWarnings({"rawtypes", "unchecked"})
    private final Map<String, Function<Object, Object>> strategies =
            (Map) AutoStrategyBuilder.build(ErmMaturityResponse.class, this::buildCustomStrategies);

    @Override
    public ModuleType getModuleType() {
        return ModuleType.ERM_MATURITY;
    }

    @Override
    public Map<String, Function<Object, Object>> getStrategies() {
        return strategies;
    }

    private Map<String, Function<ErmMaturityResponse, Object>> buildCustomStrategies() {
        Map<String, Function<ErmMaturityResponse, Object>> map = new HashMap<>();

        map.put(n("assessedBy"),
                r -> fieldMapperUtils.resolveUserFromObject(r.getAssessedBy()));

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
