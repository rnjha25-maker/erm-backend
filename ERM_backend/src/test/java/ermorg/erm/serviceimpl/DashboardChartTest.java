package ermorg.erm.serviceimpl;
import ermorg.erm.repository.DashboardChartRepository;
import ermorg.erm.dashboard.*;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.*;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import ermorg.erm.controller.DashboardChartController;
import ermorg.erm.controller.exceptionHandler.GlobalExceptionHandler;
import static ermorg.erm.dashboard.ChartPeriod.*;

class DashboardChartTest {
    private final DashboardChartRepository repository = mock(DashboardChartRepository.class);
    private final ChartScope.Resolver resolver = mock(ChartScope.Resolver.class);
    private final DashboardChartService service = new DashboardChartService(repository, resolver);
    private final ChartPeriod annual = ChartPeriod.of(2026,YearType.CALENDAR,null);

    @Test void financialYearAndQuarterCrossCalendarBoundary() {
        var period=ChartPeriod.of(2026,YearType.FINANCIAL,null);
        assertThat(period.start()).isEqualTo(LocalDate.of(2026,4,1));
        assertThat(period.end()).isEqualTo(LocalDate.of(2027,4,1));
        assertThat(period.labels(Frequency.MONTHLY)).hasSize(12).startsWith("2026-04").endsWith("2027-03");
        var q4=ChartPeriod.of(2026,YearType.FINANCIAL,4);
        assertThat(q4.columns(Frequency.MONTHLY)).containsExactly("january","february","march");
        assertThat(q4.labels(Frequency.MONTHLY)).containsExactly("2027-01","2027-02","2027-03");
        assertThat(q4.columns(Frequency.QUARTERLY)).containsExactly("q4");
        assertThat(ChartPeriod.of(2024,YearType.CALENDAR,1).end()).isEqualTo(LocalDate.of(2024,4,1));
    }
    @ParameterizedTest @ValueSource(ints={-1,0,5}) void rejectsInvalidQuarter(int quarter) {
        assertThatIllegalArgumentException().isThrownBy(()->ChartPeriod.of(2026,YearType.CALENDAR,quarter));
    }
    @Test void preservesMissingZeroAndInvalidActualsAndOneAnnualTarget() throws Exception {
        var row=new HashMap<String,Object>();
        row.put("id",1L);row.put("indicator_name","Loss events");row.put("target_value","100");
        row.put("january","0");row.put("february"," ");row.put("march","not numeric");
        when(repository.performance(any(),eq(annual),eq(Indicator.KRI),eq(Frequency.MONTHLY),eq(0),eq(1)))
            .thenReturn(List.of(row,row));
        var response=service.performance(annual,Indicator.KRI,Frequency.MONTHLY,null,null,null,0,1);
        assertThat(response.hasNext()).isTrue();
        assertThat(response.series()).hasSize(1);
        var series=response.series().get(0);
        assertThat(series.target()).isEqualByComparingTo("100");
        assertThat(series.actuals()).hasSize(12);
        assertThat(series.actuals().get(0)).isEqualByComparingTo("0");
        assertThat(series.actuals().subList(1,12)).containsOnlyNulls();
        assertThat(series.invalidFields()).containsExactly("march");
        verify(repository,times(1)).performance(any(),eq(annual),eq(Indicator.KRI),eq(Frequency.MONTHLY),eq(0),eq(1));
    }
    @Test void exposureKeepsUnitsSeparateAndMissingValuesVisible() throws Exception {
        var inr= new HashMap<String,Object>(Map.of("currency","INR","value_unit","CRORE","risk_count",2L,"valued_count",1L,"total",new BigDecimal("25")));
        var usd= new HashMap<String,Object>(Map.of("currency","USD","value_unit","MILLION","risk_count",1L,"valued_count",0L));
        when(repository.exposure(any(),eq(annual),eq(false))).thenReturn(List.of(inr,usd));
        when(repository.exposure(any(),eq(annual),eq(true))).thenReturn(List.of(Map.of("currency","INR","value_unit","CRORE","total",0,"risk_count",1L,"valued_count",1L)));
        var response=service.exposure(annual,null,null,null);
        assertThat(response.totals()).hasSize(2);
        var first=response.totals().get(0);
        assertThat(first.inherent()).isEqualByComparingTo("25");
        assertThat(first.residual()).isEqualByComparingTo("0");
        assertThat(first.missingInherentCount()).isEqualTo(1);
        assertThat(response.totals().get(1).inherent()).isNull();
        assertThat(response.totals().get(1).residual()).isNull();
    }
    @Test void trendFillsEmptyMonthsWithCountsOfZero() throws Exception {
        when(repository.trend(any(),eq(annual),eq(false))).thenReturn(List.of(Map.of("period","2026-02","total",3)));
        when(repository.trend(any(),eq(annual),eq(true))).thenReturn(List.of());
        var response=service.trend(annual,null,null,null);
        assertThat(response.risks()).hasSize(12).startsWith(0L,3L,0L);
        assertThat(response.assessments()).containsOnly(0L);
    }
    @Test void paginationValidatedBeforeDatabaseWork() {
        assertThatIllegalArgumentException().isThrownBy(()->service.scatter(annual,null,null,null,-1,20));
        assertThatIllegalArgumentException().isThrownBy(()->service.scatter(annual,null,null,null,0,101));
        assertThatIllegalArgumentException().isThrownBy(()->service.scatter(annual,null,null,null,0,0));
        verifyNoInteractions(repository,resolver);
    }
    @ParameterizedTest @ValueSource(strings={
        "/dashboard/charts/exposure", "/dashboard/charts/exposure?year=2026&yearType=OTHER",
        "/dashboard/charts/exposure?year=2026&quarter=5", "/dashboard/charts/exposure?year=1800",
        "/dashboard/charts/performance?year=2026&indicator=KRA&frequency=MONTHLY",
        "/dashboard/charts/performance?year=2026&indicator=KPI&frequency=ANNUALLY"})
    void badParametersReturn400WithExistingAdvice(String url) throws Exception {
        var mvc=MockMvcBuilders.standaloneSetup(new DashboardChartController(service))
            .setControllerAdvice(new GlobalExceptionHandler()).build();
        mvc.perform(get(url)).andExpect(status().isBadRequest()).andExpect(jsonPath("status").value("FAILED"));
        verifyNoInteractions(repository,resolver);
    }
}
