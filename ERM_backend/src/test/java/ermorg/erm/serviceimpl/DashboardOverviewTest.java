package ermorg.erm.serviceimpl;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;
import java.util.*;
import java.math.BigDecimal;
import org.junit.jupiter.api.Test;
import ermorg.erm.dashboard.*;
import ermorg.erm.constant.ErmDashboardPeriodType;
import ermorg.erm.mapping.FieldMapperUtils;
import ermorg.erm.repository.DashboardOverviewRepository;

class DashboardOverviewTest {
    final ChartScope.Resolver scopes=mock(ChartScope.Resolver.class);
    final DashboardOverviewRepository repository=mock(DashboardOverviewRepository.class);
    final DashboardChartService charts=mock(DashboardChartService.class);
    final FieldMapperUtils mapper=new FieldMapperUtils(null,null);
    final DashboardOverviewService service=new DashboardOverviewService(scopes,repository,charts,new ErmRiskHeatmapBuilder(mapper),mapper);
    Map<String,Object> risk(long id,String rating,String appetite) {
        var row=new HashMap<String,Object>();
        row.put("id",id);row.put("assessment_id",id);row.put("risk_title","Risk "+id);
        row.put("risk_rating",rating);row.put("likelihood","4");row.put("gross_impact_score","20");
        row.put("risk_analysis_type","Qualitative");row.put("risk_category",2);row.put("risk_source","external");
        row.put("risk_priority","High");row.put("baseline_appetite",appetite);row.put("risk_status","APPROVED");
        row.put("risk_treatment_strategy","accept");return row;
    }
    @Test void usesRatingFallbackNormalizesAndKeepsUnknownStatus() {
        var one=risk(1,"4","Appetite Breached but within Tolerance");
        var two=risk(2,"3","Risk Tolerance Breached");two.put("risk_treatment_strategy","Accept");
        var response=service.assemble(List.of(one,two),0,1);
        assertThat(response.getTotalRisks()).isEqualTo(2);
        assertThat(response.getWithinRiskAppetite()).isZero();
        assertThat(response.getRiskToleranceBreached()).isEqualTo(1);
        assertThat(response.getRiskLevelSummary()).anySatisfy(c->{assertThat(c.getKey()).isEqualTo("HIGH");assertThat(c.getCount()).isEqualTo(1);});
        assertThat(response.getTreatmentStrategy()).anySatisfy(c->{assertThat(c.getKey()).isEqualTo("ACCEPT");assertThat(c.getCount()).isEqualTo(2);});
        assertThat(response.getRiskByCategory().get(0).getKey()).isEqualTo("FINANCIAL");
        assertThat(response.getRatingWiseStatus()).anySatisfy(c->{assertThat(c.label()).isEqualTo("High");assertThat(c.unknown()).isEqualTo(1);});
        assertThat(response.getRiskRegister()).hasSize(1);assertThat(response.getRiskRegisterPage().totalElements()).isEqualTo(2);
        assertThat(response.getRiskHeatmap().getCells()).hasSize(25);
        assertThat(response.getRiskHeatmap().getPlottedRisks()).isEqualTo(2);
    }
    @Test void emptyDataHasStableBucketsAndPageOverflowIsSafe() {
        var response=service.assemble(List.of(),Integer.MAX_VALUE,100);
        assertThat(response.getRiskLevelSummary()).hasSize(5).allSatisfy(c->assertThat(c.getCount()).isZero());
        assertThat(response.getRiskByImpact()).hasSize(5);assertThat(response.getRiskHeatmap().getCells()).hasSize(25);
        assertThat(response.getRiskRegister()).isEmpty();
    }
    @Test void latestMissingResidualAleNeverFallsBackToBaselineAmount() {
        var row=risk(1,"4","Within Risk Appetite");row.put("review_id",4L);row.put("review_type","Quantitative");
        row.put("yearly_loss_expectancy",300);row.put("residual_risk_rating","Low");
        var response=service.assemble(List.of(row),0,20);
        assertThat(response.getFinancialExposureByRisk().get(0).annualLossExpectancy()).isNull();
        assertThat(response.getRiskRegister().get(0).rating()).isEqualTo("LOW");
    }
    @Test void financialAndHalfYearBoundsAreCorrect() {
        var h2=DashboardOverviewService.window(2026,ChartPeriod.YearType.FINANCIAL,ErmDashboardPeriodType.H2);
        assertThat(h2.start()).hasToString("2026-10-01");assertThat(h2.end()).hasToString("2027-04-01");
        assertThatThrownBy(()->DashboardOverviewService.window(0,ChartPeriod.YearType.CALENDAR,ErmDashboardPeriodType.YEAR)).isInstanceOf(IllegalArgumentException.class);
    }
    @Test void mixedSeriesStaySeparateAndHalfYearSlicesActuals() throws Exception {
        var scope=new ChartScope(10L,20L,null,null,null,false,null,null);
        when(scopes.resolve(null,null,null)).thenReturn(scope);
        var year=ChartPeriod.of(2026,ChartPeriod.YearType.FINANCIAL,null);
        var data=new DashboardChartService.PerformanceResponse(year,ChartPeriod.Indicator.KRI,ChartPeriod.Frequency.QUARTERLY,"EXPLICIT",year.labels(ChartPeriod.Frequency.QUARTERLY),0,20,false,
            List.of(new DashboardChartService.PerformanceSeries(1,"KRI 1","INR","CRORES","Money",BigDecimal.TEN,Arrays.asList(BigDecimal.ONE,null,BigDecimal.TEN,BigDecimal.ONE),List.of(),"EXPLICIT"),
                    new DashboardChartService.PerformanceSeries(2,"KRI 2","USD","MILLION","Money",BigDecimal.ONE,Collections.nCopies(4,BigDecimal.TEN),List.of(),"EXPLICIT")));
        when(charts.performanceForScope(year,ChartPeriod.Indicator.KRI,ChartPeriod.Frequency.QUARTERLY,scope,0,20)).thenReturn(data);
        var response=service.overview(2026,ChartPeriod.YearType.FINANCIAL,ErmDashboardPeriodType.H2,null,null,null,ChartPeriod.Indicator.KRI,ChartPeriod.Frequency.QUARTERLY,0,20,0,20,null);
        assertThat(response.getPerformance()).isEmpty();assertThat(response.getPerformanceSeries()).hasSize(2);
        assertThat(response.getPerformanceSeries().get(0).points()).extracting(p->p.period()).containsExactly("Q3","Q4");
        assertThat(response.getTotalKris()).isNull();assertThat(response.getUnavailableMetrics()).containsKey("totalKris");
        verify(scopes,times(1)).resolve(null,null,null);
    }
}
