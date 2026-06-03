package ermorg.erm.mapping;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

import org.springframework.stereotype.Component;

import ermorg.erm.dto.response.RiskResponse;
import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class RiskStrategyConfig implements FieldStrategy {

    private final FieldMapperUtils fieldMapperUtils;

    @SuppressWarnings({"rawtypes", "unchecked"})
    private final Map<String, Function<Object, Object>> strategies =
            (Map) AutoStrategyBuilder.build(RiskResponse.class, this::buildCustomStrategies);

    @Override
    public ModuleType getModuleType() {
        return ModuleType.RISK;
    }

    @Override
    public Map<String, Function<Object, Object>> getStrategies() {
        return strategies;
    }

    private Map<String, Function<RiskResponse, Object>> buildCustomStrategies() {
        Map<String, Function<RiskResponse, Object>> map = new HashMap<>();

        map.put(n("subRiskName"),
                r -> fieldMapperUtils.stringify(fieldMapperUtils.resolveSubList(r.getSubRisk())));

        map.put(n("riskOwner"),
                r -> fieldMapperUtils.resolveUser(r.getRiskOwnerId()));

        map.put(n("riskChampionId"),
                RiskResponse::getRiskChampionName);

        map.put(n("businessVertical"), RiskResponse::getBusinessVerticalName);
        map.put(n("businessSegment"), RiskResponse::getBusinessSegmentName);
        map.put(n("function"), RiskResponse::getFunctionName);
        map.put(n("riskOwnerId"),RiskResponse::getRiskOwnerName);
        map.put(n("branchId"),RiskResponse::getBranchName);

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
