package ermorg.erm.controller;

import org.springframework.web.bind.annotation.*;
import ermorg.erm.dashboard.*;
import ermorg.erm.dto.ResponseStatus;
import ermorg.erm.exception.ResourceNotFoundException;
import ermorg.erm.response.GeneralResponse;

/** Dedicated chart endpoints; existing /dashboard/erm-summary remains unchanged. */
@RestController
@RequestMapping("/dashboard/charts")
@io.swagger.v3.oas.annotations.tags.Tag(name="Dashboard charts",description="Period-aware chart data with tenant/role scope. Only completed period evaluations contribute; completion does not imply approval.")
public class DashboardChartController {
    private final DashboardChartService service;
    public DashboardChartController(DashboardChartService service) { this.service=service; }
    @GetMapping("/performance")
    @io.swagger.v3.oas.annotations.Operation(summary="KRI or KPI target versus actual",description="One constant annual target per series; monthly/quarterly actuals. Null means missing, not zero. Calendar or April-March financial year. Page size at most 100.")
    public GeneralResponse<DashboardChartService.PerformanceResponse> performance(
        @RequestParam int year, @RequestParam(defaultValue="CALENDAR") ChartPeriod.YearType yearType,
        @RequestParam(required=false) Integer quarter, @RequestParam ChartPeriod.Indicator indicator,
        @RequestParam ChartPeriod.Frequency frequency, @RequestParam(required=false) Long companyId,
        @RequestParam(required=false) Long branchId, @RequestParam(required=false) Long departmentId,
        @RequestParam(defaultValue="0") int page,@RequestParam(defaultValue="20") int size) throws ResourceNotFoundException {
        return ok(service.performance(ChartPeriod.of(year,yearType,quarter),indicator,frequency,companyId,branchId,departmentId,page,size));
    }
    @GetMapping("/exposure")
    @io.swagger.v3.oas.annotations.Operation(summary="Quantitative inherent versus residual exposure",description="Inherent baseline as of period end; latest residual review within the period per risk. Separate currency/unit groups; no summing repeated quarter snapshots.")
    public GeneralResponse<DashboardChartService.ExposureResponse> exposure(
        @RequestParam int year,@RequestParam(defaultValue="CALENDAR") ChartPeriod.YearType yearType,
        @RequestParam(required=false) Integer quarter,@RequestParam(required=false) Long companyId,
        @RequestParam(required=false) Long branchId,@RequestParam(required=false) Long departmentId) throws ResourceNotFoundException {
        return ok(service.exposure(ChartPeriod.of(year,yearType,quarter),companyId,branchId,departmentId));
    }
    @GetMapping("/trend")
    @io.swagger.v3.oas.annotations.Operation(summary="Created-risk and assessment counts by month")
    public GeneralResponse<DashboardChartService.TrendResponse> trend(
        @RequestParam int year,@RequestParam(defaultValue="CALENDAR") ChartPeriod.YearType yearType,
        @RequestParam(required=false) Integer quarter,@RequestParam(required=false) Long companyId,
        @RequestParam(required=false) Long branchId,@RequestParam(required=false) Long departmentId) throws ResourceNotFoundException {
        return ok(service.trend(ChartPeriod.of(year,yearType,quarter),companyId,branchId,departmentId));
    }
    @GetMapping("/scatter")
    @io.swagger.v3.oas.annotations.Operation(summary="Latest baseline assessment per risk",description="Raw likelihood, impact and rating; no invented normalized coordinates.")
    public GeneralResponse<DashboardChartService.ScatterResponse> scatter(
        @RequestParam int year,@RequestParam(defaultValue="CALENDAR") ChartPeriod.YearType yearType,
        @RequestParam(required=false) Integer quarter,@RequestParam(required=false) Long companyId,
        @RequestParam(required=false) Long branchId,@RequestParam(required=false) Long departmentId,
        @RequestParam(defaultValue="0") int page,@RequestParam(defaultValue="20") int size) throws ResourceNotFoundException {
        return ok(service.scatter(ChartPeriod.of(year,yearType,quarter),companyId,branchId,departmentId,page,size));
    }
    private static <T> GeneralResponse<T> ok(T data) {
        GeneralResponse<T> response=new GeneralResponse<>();
        response.setData(data);response.setStatus(ResponseStatus.SUCCESS);return response;
    }
    // Keep request-binding errors local so existing controllers retain their error contract.
    @ExceptionHandler({org.springframework.web.method.annotation.MethodArgumentTypeMismatchException.class,
        org.springframework.web.bind.MissingServletRequestParameterException.class})
    public org.springframework.http.ResponseEntity<GeneralResponse<Object>> invalidParameter(Exception ex) {
        GeneralResponse<Object> response = new GeneralResponse<>();
        response.setStatus(ResponseStatus.FAILED);
        response.setMessage("Invalid or missing chart parameter. Use year, CALENDAR/FINANCIAL, quarter 1-4, KRI/KPI and MONTHLY/QUARTERLY as applicable.");
        return org.springframework.http.ResponseEntity.badRequest().body(response);
    }
}
