package ermorg.erm.controller;

import org.springframework.web.bind.annotation.*;
import org.springframework.http.ResponseEntity;
import ermorg.erm.constant.ErmDashboardPeriodType;
import ermorg.erm.dashboard.ChartPeriod;
import ermorg.erm.dto.ResponseStatus;
import ermorg.erm.dto.response.ErmDashboardOverviewResponse;
import ermorg.erm.exception.ResourceNotFoundException;
import ermorg.erm.response.GeneralResponse;
import ermorg.erm.serviceimpl.DashboardOverviewService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;

@RestController
@RequestMapping("/dashboard")
@Tag(name="ERM dashboard overview",description="Combined UI data contract. Existing dashboard endpoints remain available.")
public class DashboardOverviewController {
    private final DashboardOverviewService service;
    public DashboardOverviewController(DashboardOverviewService service) {this.service=service;}
    @GetMapping("/overview")
    @Operation(summary="Combined ERM dashboard for the new UI",description="Unique risks as of period end. Latest review within period, including completed period evaluations. Monthly/quarterly series remain separate by record and unit. Null headline metrics have reasons in unavailableMetrics. Omit scope IDs for all permitted records; zero is invalid. Financial year starts in April. Pagination affects only its respective table/series, never chart counts.")
    public GeneralResponse<ErmDashboardOverviewResponse> overview(
        @RequestParam int year,
        @RequestParam(defaultValue="CALENDAR") ChartPeriod.YearType yearType,
        @RequestParam(defaultValue="YEAR") ErmDashboardPeriodType periodType,
        @RequestParam(required=false) Long companyId,@RequestParam(required=false) Long branchId,
        @RequestParam(required=false) Long functionId,
        @RequestParam(defaultValue="KRI") ChartPeriod.Indicator indicator,
        @RequestParam(defaultValue="MONTHLY") ChartPeriod.Frequency frequency,
        @RequestParam(defaultValue="0") int page,@RequestParam(defaultValue="20") int size,
        @RequestParam(defaultValue="0") int performancePage,@RequestParam(defaultValue="20") int performanceSize,
        @RequestParam(required=false) Long performanceRecordId) throws ResourceNotFoundException {
        var response=new GeneralResponse<ErmDashboardOverviewResponse>();
        response.setData(service.overview(year,yearType,periodType,companyId,branchId,functionId,indicator,frequency,
            page,size,performancePage,performanceSize,performanceRecordId));
        response.setStatus(ResponseStatus.SUCCESS);return response;
    }
    @ExceptionHandler({IllegalArgumentException.class,org.springframework.web.method.annotation.MethodArgumentTypeMismatchException.class,
        org.springframework.web.bind.MissingServletRequestParameterException.class})
    public ResponseEntity<GeneralResponse<Object>> invalid(Exception ex) {
        var response=new GeneralResponse<Object>();response.setStatus(ResponseStatus.FAILED);
        response.setMessage(ex instanceof IllegalArgumentException?ex.getMessage():
            "Invalid overview parameters: year, CALENDAR/FINANCIAL, YEAR/H1/H2/Q1-Q4, MONTHLY/QUARTERLY, KRI/KPI.");
        return ResponseEntity.badRequest().body(response);
    }
}
