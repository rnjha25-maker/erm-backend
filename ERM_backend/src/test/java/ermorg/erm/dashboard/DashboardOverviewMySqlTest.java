package ermorg.erm.dashboard;

import static org.assertj.core.api.Assertions.*;
import java.util.*;
import java.time.LocalDate;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.condition.EnabledIfEnvironmentVariable;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.jdbc.datasource.SingleConnectionDataSource;
import ermorg.erm.repository.*;

/** Disposable local schema only. MySQL temporary tables cannot be reused through multiple CTE references. */
@EnabledIfEnvironmentVariable(named="ERM_CHART_DB_TEST",matches="true")
class DashboardOverviewMySqlTest {
    SingleConnectionDataSource source;
    JdbcTemplate jdbc;
    DashboardOverviewRepository repository;
    String schema;
    ChartScope scope=new ChartScope(10L,20L,null,null,null,false,null,null);
    LocalDate start=LocalDate.of(2026,1,1),end=LocalDate.of(2027,1,1);
    @BeforeEach void setup() {
        source=new SingleConnectionDataSource("jdbc:mysql://127.0.0.1:3308/erm_chart_verification","root","",true);
        jdbc=new JdbcTemplate(source);
        schema="erm_overview_test_"+UUID.randomUUID().toString().replace("-","");
        jdbc.execute("CREATE DATABASE "+schema);
        jdbc.execute("USE "+schema);
        for(String table:List.of("risk","risk_assessment","risk_reviews","user","user_detail","branch","department","company",
            "erm_maturity_assessments","erm_maturity_scores","erm_maturity_department_ids"))
            jdbc.execute("CREATE TABLE "+table+" LIKE erm_verification."+table);
        jdbc.execute("CREATE TABLE period_evaluation(id BIGINT,module VARCHAR(20),organization_id BIGINT,company_id BIGINT,risk_id BIGINT,state VARCHAR(20),effective_date DATE,created_at DATETIME,evaluation_values JSON)");
        var named=new NamedParameterJdbcTemplate(source);
        repository=new DashboardOverviewRepository(named,new DashboardChartRepository(named));
        jdbc.update("INSERT INTO risk(id,organization_id,company_id,deleted,created_at,branch_id,business_function,created_by_id,risk_title) VALUES(1,10,20,false,'2025-01-01',40,50,30,'Old risk'),(2,10,21,false,'2026-01-01',41,51,31,'Other company'),(3,11,20,false,'2026-01-01',40,50,30,'Other org'),(4,10,20,true,'2026-01-01',40,50,30,'Deleted'),(5,10,20,false,'2027-01-01',40,50,30,'Future')");
    }
    @AfterEach void close() {
        try {if(schema!=null && schema.matches("erm_overview_test_[a-f0-9]{32}"))jdbc.execute("DROP DATABASE "+schema);}
        finally {if(source!=null)source.destroy();}
    }
    @Test void olderRiskRemainsLatestAssessmentAndReviewHistoryWin() {
        jdbc.update("INSERT INTO risk_assessment(id,risk_id,organization_id,company_id,deleted,created_at,likelihood,gross_impact_score,risk_rating) VALUES(10,1,10,20,false,'2025-02-01','3','15','Medium'),(11,1,10,20,false,'2026-03-01','4','20','High'),(12,1,10,20,false,'2027-02-01','5','25','Critical')");
        jdbc.update("INSERT INTO risk_reviews(id,risk_id,organization_id,company_id,deleted,created_at,last_evaluation_date,residual_risk_rating,annual_loss_expectancy) VALUES(20,1,10,20,false,'2026-02-01','2026-02-01','High',200)");
        jdbc.update("INSERT INTO period_evaluation VALUES(30,'REVIEW',10,20,1,'COMPLETED','2026-10-01','2026-10-01',JSON_OBJECT('residual_risk_rating','Low','review_type','Quantitative','annual_loss_expectancy',NULL)),(31,'REVIEW',10,20,1,'DRAFT','2026-11-01','2026-11-01',JSON_OBJECT('residual_risk_rating','Critical'))");
        var rows=repository.risks(scope,start,end);
        assertThat(rows).hasSize(1);
        assertThat(((Number)rows.get(0).get("assessment_id")).longValue()).isEqualTo(11);
        assertThat(rows.get(0).get("residual_risk_rating")).isEqualTo("Low");
        assertThat(rows.get(0).get("annual_loss_expectancy")).isNull();
        var quarter=repository.risks(scope,LocalDate.of(2026,4,1),LocalDate.of(2026,7,1));
        assertThat(quarter).hasSize(1);assertThat(quarter.get(0).get("review_id")).isNull();
    }
    @Test void scopePredicatesApplyToAllRiskRows() {
        assertThat(repository.risks(new ChartScope(10L,null,null,null,null,false,null,null),start,end)).hasSize(2);
        assertThat(repository.risks(new ChartScope(10L,20L,null,99L,null,false,null,null),start,end)).isEmpty();
        assertThat(repository.risks(new ChartScope(10L,20L,null,null,null,true,null,null),start,end)).isEmpty();
        assertThat(repository.risks(new ChartScope(10L,20L,99L,null,null,false,null,null),start,end)).isEmpty();
    }
    @Test void maturityHonorsBranchDepartmentAndTenant() {
        jdbc.update("INSERT INTO company(id,organization_id,deleted,name) VALUES(20,10,false,'Company')");
        jdbc.update("INSERT INTO department(id,organization_id,branch_id,deleted,name) VALUES(50,10,40,false,'ERM'),(51,10,41,false,'HR')");
        jdbc.update("INSERT INTO erm_maturity_assessments(id,organization_id,company_id,deleted,created_at,erm_maturity_id) VALUES(100,10,20,false,'2026-02-01','20_50'),(101,10,20,false,'2026-02-01','20')");
        jdbc.update("INSERT INTO erm_maturity_department_ids(maturity_assessment_id,department_id) VALUES(100,50)");
        jdbc.update("INSERT INTO erm_maturity_scores(id,maturity_assessment_id,deleted,marks_achieved) VALUES(200,100,false,'40'),(201,100,true,'99')");
        var rows=repository.maturities(new ChartScope(10L,20L,null,40L,50L,false,null,null),start,end);
        assertThat(rows).hasSize(1);assertThat(rows.get(0).get("marks_achieved")).isEqualTo("40");
        assertThat(repository.maturities(new ChartScope(10L,20L,null,41L,null,false,null,null),start,end)).isEmpty();
    }
}
