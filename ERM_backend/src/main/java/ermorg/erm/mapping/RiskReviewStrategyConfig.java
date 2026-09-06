package ermorg.erm.mapping;

import ermorg.erm.dto.response.RiskReviewResponseDtoResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

import static ermorg.erm.mapping.CustomFieldConfig.normalizeKey;

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

        map.put(normalizeKey("subRiskName"),
                r -> fieldMapperUtils.stringify(fieldMapperUtils.resolveSubList(r.getSubRiskResponses())));
        map.put(normalizeKey("subRiskIds"),
                r -> fieldMapperUtils.stringify(fieldMapperUtils.resolveSubList(r.getSubRiskResponses())));
        map.put(normalizeKey("riskEvaluationBy"),
                r -> fieldMapperUtils.resolveUserFromObject(r.getRiskEvaluationBy()));
        map.put(normalizeKey("riskReporting"),
                r -> r.getRiskReportingName() != null ? r.getRiskReportingName()
                        : fieldMapperUtils.resolveUser(r.getRiskReporting()));
        map.put(normalizeKey("riskId"),              RiskReviewResponseDtoResponse::getRiskTitle);
        map.put(normalizeKey("riskToleranceStatus"), RiskReviewResponseDtoResponse::getRiskToleranceStatus);
        map.put(normalizeKey("riskAppetiteStatus"),  RiskReviewResponseDtoResponse::getRiskAppetiteStatus);
        map.put(normalizeKey("residualRiskRating"), r -> fieldMapperUtils.resolveRatingLabel(r.getResidualRiskRating()));
        map.put(normalizeKey("residualRiskRatingCriteria"), r -> fieldMapperUtils.resolveRatingLabel(r.getResidualRiskRating()));
        map.put(normalizeKey("residualRiskCriteria"), r -> fieldMapperUtils.resolveRatingLabel(r.getResidualRiskRating()));
        map.put(normalizeKey("riskRating"), r -> fieldMapperUtils.resolveRatingLabel(r.getResidualRiskRating()));
        map.put(normalizeKey("2riskacceptancelevel"), RiskReviewResponseDtoResponse::getRiskAcceptanceLevel);
        map.put(normalizeKey("valueUnit"),
                r -> r.getValueUnit() != null ? r.getValueUnit().getLabel() : null);
        map.put(normalizeKey("currency"), RiskReviewResponseDtoResponse::getCurrency);

        return map;
    }
}
