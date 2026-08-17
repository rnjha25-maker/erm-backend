package ermorg.erm.mapping;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

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

        map.put(n("riskSubs"),
                r -> fieldMapperUtils.stringify(fieldMapperUtils.resolveSubList(r.getRiskSubs())));

        map.put(n("primaryResponsible"),
                r -> fieldMapperUtils.resolveUser(r.getPrimaryResponsible()));

        map.put(n("approver"),
                r -> fieldMapperUtils.resolveUser(r.getApprover()));
        map.put(n("controlSubTitle"),
                r -> stringifyControlSubTitles(r.getControlSubTitle()));


        return map;
    }

    private String stringifyControlSubTitles(List<ermorg.erm.dto.riskDTO.RiskSubControlDto> controls) {
        if (controls == null || controls.isEmpty()) {
            return null;
        }
        return controls.stream()
                .map(ermorg.erm.dto.riskDTO.RiskSubControlDto::getControlSubTitle)
                .filter(value -> value != null && !value.isBlank())
                .collect(Collectors.joining(", "));
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
