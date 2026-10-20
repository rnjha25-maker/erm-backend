package ermorg.erm.dto.request;

import java.util.Map;
import com.fasterxml.jackson.databind.JsonNode;

@io.swagger.v3.oas.annotations.media.Schema(name="EditPeriodEvaluation",example="{\"revision\":0,\"values\":{\"review_type\":\"Quantitative\",\"annual_loss_expectancy\":50,\"currency\":\"INR\",\"value_unit\":\"CRORES\"}}")
public record UpdatePeriodEvaluationRequest(@io.swagger.v3.oas.annotations.media.Schema(description="Latest revision from GET; stale writes return 409",minimum="0") long revision,
        @io.swagger.v3.oas.annotations.media.Schema(description="Allowed snake_case scalar fields from editableFields; omitted keys retained, null clears") Map<String,JsonNode> values) {}
