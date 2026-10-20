package ermorg.erm.dashboard;
import ermorg.erm.repository.DashboardChartRepository;

import static org.assertj.core.api.Assertions.*;
import java.util.*;
import java.math.BigDecimal;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.condition.EnabledIfEnvironmentVariable;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.jdbc.datasource.SingleConnectionDataSource;
import static ermorg.erm.dashboard.ChartPeriod.*;

/** Local-only integration tests. Session temporary tables disappear when the connection closes. */
@EnabledIfEnvironmentVariable(named="ERM_CHART_DB_TEST",matches="true")
class DashboardChartMySqlTest {
    SingleConnectionDataSource source;
    JdbcTemplate jdbc;
    DashboardChartRepository repository;
    ChartScope scope=new ChartScope(10L,20L,null,null,null,false,null,null);
    ChartPeriod annual=ChartPeriod.of(2026,YearType.CALENDAR,null);
    @BeforeEach void setup() {
        source=new SingleConnectionDataSource("jdbc:mysql://127.0.0.1:3308/erm_chart_verification","root","",true);
        jdbc=new JdbcTemplate(source);
        // LIKE validates the repository against the real Hibernate-created local schema.
        for(String table:List.of("risk","risk_assessment","risk_reviews","kri_kpi_reviews","kpa_kpi_review","user","user_detail"))
            jdbc.execute("CREATE TEMPORARY TABLE "+table+" LIKE erm_verification."+table);
        for(String table:List.of("kri_kpi_reviews","kpa_kpi_review"))
            jdbc.execute("ALTER TABLE "+table+" ADD COLUMN reporting_year INT NULL, ADD COLUMN reporting_year_type VARCHAR(20) NULL");
        repository=new DashboardChartRepository(new NamedParameterJdbcTemplate(source));
        jdbc.execute("CREATE TEMPORARY TABLE period_evaluation(id BIGINT,module VARCHAR(20),source_id BIGINT,organization_id BIGINT,company_id BIGINT,risk_id BIGINT,state VARCHAR(20),effective_date DATE,created_at DATETIME,reporting_year INT,year_type VARCHAR(20),frequency VARCHAR(20),period_number INT,period_start DATE,evaluation_values JSON,source_snapshot JSON)");
        risk(1,10,20,false);risk(2,10,20,false);risk(3,11,20,false);risk(4,10,21,false);risk(5,10,20,true);
    }
    @AfterEach void close() { if(source!=null) source.destroy(); }
    @Test void kpiBranchFilterAcceptsLegacyProfileOrganizationButRejectsDifferentOrganization() {
        jdbc.update("INSERT INTO user_detail(id,organization_id) VALUES(300,10)");
        jdbc.update("INSERT INTO user(id,deleted,organization_id,company_id,branch_id,user_detail_id) VALUES(30,false,NULL,20,40,300)");
        jdbc.update("INSERT INTO kpa_kpi_review(id,kpa,key_performance_indicator,owner_id,kpi_evaluation_by,organization_id,company_id,is_deleted,created_at,reporting_frequency,status,target_value,q1,business_function) VALUES(1,'KRA','KPI',30,30,10,20,false,'2026-02-01','Quarterly','Active',100,22,'50')");
        var branch=new ChartScope(10L,20L,null,40L,50L,false,null,null);
        assertThat(repository.performance(branch,annual,Indicator.KPI,Frequency.QUARTERLY,0,20)).hasSize(1);
        jdbc.update("UPDATE user_detail SET organization_id=11 WHERE id=300");
        assertThat(repository.performance(branch,annual,Indicator.KPI,Frequency.QUARTERLY,0,20)).isEmpty();
        jdbc.update("UPDATE user_detail SET organization_id=10 WHERE id=300");
        jdbc.update("UPDATE user SET organization_id=11 WHERE id=30");
        assertThat(repository.performance(branch,annual,Indicator.KPI,Frequency.QUARTERLY,0,20)).isEmpty();
    }
    @Test void organizationWidePerformanceHistoryStillRespectsTenantAndSelectedSourceIds() {
        jdbc.update("INSERT INTO period_evaluation(id,module,source_id,organization_id,company_id,state,reporting_year,year_type,frequency,period_number,period_start,evaluation_values,source_snapshot) VALUES(1,'KRI',100,10,20,'COMPLETED',2026,'CALENDAR','MONTHLY',1,'2026-01-01','{}','{}'),(2,'KRI',101,10,21,'COMPLETED',2026,'CALENDAR','MONTHLY',1,'2026-01-01','{}','{}'),(3,'KRI',100,11,20,'COMPLETED',2026,'CALENDAR','MONTHLY',1,'2026-01-01','{}','{}'),(4,'KRI',999,10,20,'COMPLETED',2026,'CALENDAR','MONTHLY',1,'2026-01-01','{}','{}')");
        var all=new ChartScope(10L,null,null,null,null,false,null,null);
        assertThat(repository.performanceHistory(all,annual,Indicator.KRI,Frequency.MONTHLY,List.of(100L,101L))).hasSize(2);
        assertThat(repository.performanceHistory(scope,annual,Indicator.KRI,Frequency.MONTHLY,List.of(100L,101L))).hasSize(1);
        assertThat(repository.performanceHistory(all,annual,Indicator.KRI,Frequency.MONTHLY,List.of())).isEmpty();
    }
    void risk(long id,long org,long company,boolean deleted) {
        jdbc.update("INSERT INTO risk(id,organization_id,company_id,deleted,created_at,branch_id,business_function,created_by_id,risk_title) VALUES(?,?,?,?, '2025-01-01',40,50,30,?)",id,org,company,deleted,"Risk "+id);
    }
    void assessment(long id,long risk,String date,Double value,String type,boolean deleted) {
        jdbc.update("INSERT INTO risk_assessment(id,risk_id,organization_id,company_id,deleted,created_at,last_evaluation_date,yearly_loss_expectancy,risk_analysis_type,currency,value_unit) VALUES(?,?,10,20,?, ?, ?,?,?, 'INR','CRORES')",id,risk,deleted,date,date,value,type);
    }
    @Test void annualUsesOneLatestAssessmentPerRiskWithStableTieBreakAndScope() {
        assessment(101,1,"2026-02-01",100d,"Quantitative",false);
        assessment(102,1,"2026-05-01",200d,"Quantitative",false);
        assessment(103,1,"2026-08-01",300d,"Quantitative",false);
        assessment(104,1,"2026-11-01",400d,"Quantitative",false);
        assessment(105,1,"2026-11-01",450d,"Quantitative",false);
        assessment(106,1,"2026-12-01",900d,"Qualitative",false);
        assessment(107,1,"2026-12-02",900d,"Quantitative",true);
        assessment(201,2,"2026-08-01",50d,"Quantitative",false);
        assessment(301,3,"2026-08-01",999d,"Quantitative",false);
        assessment(401,4,"2026-08-01",999d,"Quantitative",false);
        assessment(501,5,"2026-08-01",999d,"Quantitative",false);
        var rows=repository.exposure(scope,annual,false);
        assertThat(rows).hasSize(1);
        assertThat(new BigDecimal(rows.get(0).get("total").toString())).isEqualByComparingTo("500");
        assertThat(((Number)rows.get(0).get("risk_count")).longValue()).isEqualTo(2);
        var q2=repository.exposure(scope,ChartPeriod.of(2026,YearType.CALENDAR,2),false);
        assertThat(new BigDecimal(q2.get(0).get("total").toString())).isEqualByComparingTo("200");
    }
    @Test void latestMissingAleIsNotReplacedWithOlderValueAndFinancialDatesWork() {
        assessment(101,1,"2026-05-01",100d,"Quantitative",false);
        assessment(102,1,"2027-02-01",null,"Quantitative",false);
        assessment(103,1,"2027-04-01",300d,"Quantitative",false);
        var row=repository.exposure(scope,ChartPeriod.of(2026,YearType.FINANCIAL,null),false).get(0);
        assertThat(row.get("total")).isNull();
        assertThat(((Number)row.get("valued_count")).longValue()).isZero();
        jdbc.update("INSERT INTO risk_reviews(id,risk_id,organization_id,company_id,deleted,created_at,annual_loss_expectancy,review_type,currency,value_unit) VALUES(601,1,10,20,false,'2027-02-01',25,'Quantitative','INR','CRORES')");
        var residual=repository.exposure(scope,ChartPeriod.of(2026,YearType.FINANCIAL,4),true);
        assertThat(new BigDecimal(residual.get(0).get("total").toString())).isEqualByComparingTo("25");
    }
    @Test void trendAndScatterIncludeCurrentAssessmentsOfOldRisks() {
        assessment(101,1,"2026-02-01",100d,"Quantitative",false);
        assessment(102,1,"2026-05-01",200d,"Quantitative",false);
        assertThat(repository.trend(scope,annual,false)).isEmpty();
        assertThat(repository.trend(scope,annual,true)).hasSize(2);
        var points=repository.scatter(scope,annual,0,20);
        assertThat(points).hasSize(1);
        assertThat(((Number)points.get(0).get("assessment_id")).longValue()).isEqualTo(102);
        assertThat(repository.scatter(new ChartScope(10L,20L,null,99L,null,false,null,null),annual,0,20)).isEmpty();
    }
    @Test void baselineRemainsAcrossQuartersAndYearsButReviewIsPeriodSpecific() {
        assessment(101,1,"2025-02-01",100d,"Quantitative",false);
        assessment(201,2,"2025-03-01",null,"Qualitative",false);
        assessment(301,3,"2025-04-01",999d,"Quantitative",false); // other organization
        assessment(501,5,"2025-04-01",999d,"Quantitative",false); // deleted risk
        assessment(102,1,"2027-01-01",900d,"Quantitative",false); // future value
        for (int quarter=1;quarter<=4;quarter++) {
            var period=ChartPeriod.of(2026,YearType.CALENDAR,quarter);
            var totals=repository.exposure(scope,period,false);
            assertThat(totals).hasSize(1);
            assertThat(new BigDecimal(totals.get(0).get("total").toString())).isEqualByComparingTo("100");
            assertThat(repository.scatter(scope,period,0,20)).hasSize(2)
                .anySatisfy(point->assertThat(((Number)point.get("assessment_id")).longValue()).isEqualTo(201));
        }
        jdbc.update("INSERT INTO risk_reviews(id,risk_id,organization_id,company_id,deleted,created_at,last_evaluation_date,annual_loss_expectancy,review_type,currency,value_unit) VALUES(601,1,10,20,false,'2026-02-01','2026-02-01',25,'Quantitative','INR','CRORES'),(602,1,10,20,false,'2026-05-01','2026-05-01',20,'Quantitative','INR','CRORES')");
        assertThat(repository.exposure(scope,ChartPeriod.of(2026,YearType.CALENDAR,3),true)).isEmpty();
        var latest=repository.exposure(scope,annual,true);
        assertThat(new BigDecimal(latest.get(0).get("total").toString())).isEqualByComparingTo("20");
        // Fixed baselines must not masquerade as newly created assessments in each quarter.
        assertThat(repository.trend(scope,annual,true)).isEmpty();
    }
    @Test void performanceBothTablesFrequencyPagingAndSoftDelete() {
        jdbc.update("INSERT INTO user(id,deleted,organization_id,company_id,branch_id) VALUES(30,false,10,20,40)");
        for(int id=1;id<=3;id++) {
            jdbc.update("INSERT INTO kri_kpi_reviews(id,risk_id,organization_id,company_id,deleted,created_at,kri_evaluation_frequency,reporting_frequency,target_value,january,q1,business_function) VALUES(?,1,10,20,?,'2026-02-01','Monthly','Quarterly','100','0','22','50')",id,id==3);
            jdbc.update("INSERT INTO kpa_kpi_review(id,kpa,key_performance_indicator,owner_id,kpi_evaluation_by,organization_id,company_id,is_deleted,created_at,reporting_frequency,kpi_evaluation_frequency,status,target_value,q1,business_function) VALUES(?,'KRA','KPI',30,30,10,20,?,'2026-02-01','Quarterly',NULL,'Active',100,22,'50')",id,id==3);
        }
        var rows=repository.performance(scope,annual,Indicator.KRI,Frequency.MONTHLY,0,1);
        assertThat(rows).hasSize(2); // extra row for hasNext
        assertThat(rows.get(0).get("january")).isEqualTo("0");
        assertThat(repository.performance(scope,annual,Indicator.KRI,Frequency.QUARTERLY,0,20)).isEmpty();
        assertThat(repository.performance(scope,annual,Indicator.KPI,Frequency.QUARTERLY,1,1)).hasSize(1);
        assertThat(repository.performance(new ChartScope(10L,20L,null,99L,null,false,null,null),annual,Indicator.KPI,Frequency.QUARTERLY,0,20)).isEmpty();
        assertThat(repository.performance(new ChartScope(10L,20L,null,40L,50L,false,null,null),annual,Indicator.KPI,Frequency.QUARTERLY,0,20)).hasSize(2);
        // Explicit year survives late creation and does not appear in a different year basis.
        jdbc.update("UPDATE kpa_kpi_review SET reporting_year=2025,reporting_year_type='FINANCIAL' WHERE id=1");
        assertThat(repository.performance(scope,annual,Indicator.KPI,Frequency.QUARTERLY,0,20)).hasSize(1);
        var historical=repository.performance(scope,ChartPeriod.of(2025,YearType.FINANCIAL,null),Indicator.KPI,Frequency.QUARTERLY,0,20);
        assertThat(historical).hasSize(2); // explicit FY entry + legacy created-date fallback
        assertThat(historical).anySatisfy(row->assertThat(row.get("reporting_year")).isEqualTo(2025));
        jdbc.update("UPDATE kpa_kpi_review SET business_function='50-invalid' WHERE id=2");
        assertThat(repository.performance(new ChartScope(10L,20L,null,null,50L,false,null,null),annual,Indicator.KPI,Frequency.QUARTERLY,0,20)).isEmpty();
    }
}
