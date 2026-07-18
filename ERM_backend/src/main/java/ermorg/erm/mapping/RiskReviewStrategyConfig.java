package ermorg.erm.mapping;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

import org.springframework.stereotype.Component;

import ermorg.erm.dto.response.RiskReviewResponseDtoResponse;
import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class RiskReviewStrategyConfig implements FieldStrategy {

    private final FieldMapperUtils fieldMapperUtils;

    @SuppressWarnings({"rawtypes", "unchecked"})
    private final Map<String, Function<Object, Object>> strategies =
            (Map) AutoStrategyBuilder.build(RiskReviewResponseDtoResponse.class, this::buildCustomStrategies);

    @Override
    public ModuleType getModuleType() {
        return ModuleType.RISK_REVIEW;
    }

    @Override
    public Map<String, Function<Object, Object>> getStrategies() {
        return strategies;
    }

    private Map<String, Function<RiskReviewResponseDtoResponse, Object>> buildCustomStrategies() {
        Map<String, Function<RiskReviewResponseDtoResponse, Object>> map = new HashMap<>();

        map.put(n("subRiskName"),
                r -> fieldMapperUtils.stringify(fieldMapperUtils.resolveSubList(r.getSubRiskResponses())));
        map.put(n("subRiskIds"),
                r -> fieldMapperUtils.stringify(fieldMapperUtils.resolveSubList(r.getSubRiskResponses())));

        map.put(n("riskEvaluationBy"),
                r -> fieldMapperUtils.resolveUserFromObject(r.getRiskEvaluationBy()));

        map.put(n("riskReporting"),
                r -> fieldMapperUtils.resolveUser(r.getRiskReporting()));

        map.put(n("riskId"), RiskReviewResponseDtoResponse::getRiskTitle);
        map.put(n("risktolerancestatus"), RiskReviewResponseDtoResponse::getRiskToleranceStatus);
        map.put(n("riskappetitestatus"), RiskReviewResponseDtoResponse::getRiskAppetiteStatus);

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
