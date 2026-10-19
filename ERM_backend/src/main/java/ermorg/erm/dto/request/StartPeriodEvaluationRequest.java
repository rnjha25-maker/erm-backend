package ermorg.erm.dto.request;

import ermorg.erm.dashboard.ChartPeriod;
import ermorg.erm.evaluation.EvaluationModule;

@io.swagger.v3.oas.annotations.media.Schema(name="StartPeriodEvaluation",example="{\"module\":\"REVIEW\",\"sourceId\":123,\"year\":2026,\"yearType\":\"FINANCIAL\",\"frequency\":\"QUARTERLY\",\"periodNumber\":2}")
public record StartPeriodEvaluationRequest(EvaluationModule module,
        @io.swagger.v3.oas.annotations.media.Schema(description="Original category record ID, never a prior evaluation ID",minimum="1") long sourceId,
        @io.swagger.v3.oas.annotations.media.Schema(description="Starting year; FINANCIAL 2026 means April 2026-March 2027",minimum="1900",maximum="2200") int year,
        ChartPeriod.YearType yearType,ChartPeriod.Frequency frequency,
        @io.swagger.v3.oas.annotations.media.Schema(description="1-12 for Monthly, 1-4 for Quarterly",minimum="1",maximum="12") int periodNumber) {}
