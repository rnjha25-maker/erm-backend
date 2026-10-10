package ermorg.erm.dto.request;

import java.time.LocalDate;

@io.swagger.v3.oas.annotations.media.Schema(name="CompletePeriodEvaluation",example="{\"revision\":1,\"effectiveDate\":\"2026-08-15\"}")
public record CompletePeriodEvaluationRequest(long revision,@io.swagger.v3.oas.annotations.media.Schema(description="Evaluation date within the selected period, ISO YYYY-MM-DD") LocalDate effectiveDate) {}
