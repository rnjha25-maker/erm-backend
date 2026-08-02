package ermorg.erm.mapping;

import ermorg.erm.dto.response.RiskAssessmentResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

import static ermorg.erm.mapping.CustomFieldConfig.normalizeKey;

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

        map.put(normalizeKey("riskAssessmentBy"),
                r -> fieldMapperUtils.resolveUserFromObject(r.getRiskAssessmentBy()));
        map.put(normalizeKey("riskReporting"),
                r -> fieldMapperUtils.resolveUserFromObject(r.getRiskReporting()));
        map.put(normalizeKey("riskTitle"),  RiskAssessmentResponse::getRiskTitle);
        map.put(normalizeKey("subRiskIds"), RiskAssessmentResponse::getSubRiskName);
        map.put(normalizeKey("riskId"),     RiskAssessmentResponse::getRiskTitle);
        map.put(normalizeKey("valueUnit"),
                r -> r.getValueUnit() != null ? r.getValueUnit().getLabel() : null);
        map.put(normalizeKey("currency"),   RiskAssessmentResponse::getCurrency);

        return map;
    }
}
