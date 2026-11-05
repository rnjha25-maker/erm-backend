package ermorg.erm.evaluation;
import ermorg.erm.dto.request.*;
import ermorg.erm.serviceimpl.PeriodEvaluationService;
import ermorg.erm.serviceimpl.DashboardChartService;
import ermorg.erm.repository.DashboardChartRepository;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;
import java.nio.file.*;
import java.time.LocalDate;
import java.util.*;
import com.fasterxml.jackson.databind.*;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.condition.EnabledIfEnvironmentVariable;
import org.springframework.jdbc.core.*;
import org.springframework.jdbc.core.namedparam.*;
import org.springframework.jdbc.datasource.*;
import org.springframework.transaction.interceptor.TransactionProxyFactoryBean;
import ermorg.erm.dashboard.*;
import ermorg.erm.model.*;
import ermorg.erm.util.*;
import static ermorg.erm.dashboard.ChartPeriod.*;

@EnabledIfEnvironmentVariable(named="ERM_CHART_DB_TEST",matches="true")
class PeriodEvaluationMySqlTest {
    SingleConnectionDataSource source;JdbcTemplate jdbc;PeriodEvaluationService service;
    ChartScope.Resolver resolver;ObjectMapper json=new ObjectMapper().findAndRegisterModules();
    @BeforeEach void setup() throws Exception {
        source=new SingleConnectionDataSource("jdbc:mysql://127.0.0.1:3308/erm_chart_verification","root","",true);
        jdbc=new JdbcTemplate(source);
        for(String table:List.of("risk","risk_controls","risk_risponse_treatment","risk_reviews","sub_risk","risk_sub_control","control_tags","approval","kri_kpi_reviews","kpa_kpi_review","user","user_detail"))
            jdbc.execute("CREATE TEMPORARY TABLE "+table+" LIKE erm_verification."+table);
        for(String table:List.of("kri_kpi_reviews","kpa_kpi_review"))jdbc.execute("ALTER TABLE "+table+" ADD reporting_year INT NULL,ADD reporting_year_type VARCHAR(20) NULL");
        String migration=Files.readString(Path.of("../database/period-evaluation.sql"))
            .replace("CREATE TABLE","CREATE TEMPORARY TABLE").replaceAll("(?m)^    CONSTRAINT[^\\r\\n]*","").replaceAll(",\\s*\\)",")");
        for(String statement:migration.split(";"))if(!statement.isBlank())jdbc.execute(statement);
        resolver=mock(ChartScope.Resolver.class);
        when(resolver.resolve(20L,null,null)).thenReturn(new ChartScope(10L,20L,null,null,null,false,null,null));
        var target=new PeriodEvaluationService(new NamedParameterJdbcTemplate(source),json,resolver);
        var proxy=new TransactionProxyFactoryBean();proxy.setTarget(target);proxy.setTransactionManager(new DataSourceTransactionManager(source));
        var attributes=new Properties();attributes.setProperty("*","PROPAGATION_REQUIRED");proxy.setTransactionAttributes(attributes);proxy.afterPropertiesSet();
        service=(PeriodEvaluationService)proxy.getObject();
        var company=new Company();company.setId(20L);CompanyContext.setCompany(company);
        var org=new Organization();org.setId(10L);OrganizationContext.setOrganization(org);
        var user=new User();user.setId(30L);UserContext.seetUser(user);
        jdbc.update("INSERT INTO risk(id,organization_id,company_id,deleted,created_by_id,branch_id,business_function,risk_title) VALUES(100,10,20,false,30,40,50,'Original risk')");
        jdbc.update("INSERT INTO risk_reviews(id,risk_id,organization_id,company_id,deleted,created_by_id,review_type,annual_loss_expectancy,currency,value_unit) VALUES(200,100,10,20,false,30,'Quantitative',100,'INR','CRORES')");
        jdbc.update("INSERT INTO sub_risk(id,risk_id,organization_id,company_id,deleted,risk_review_id,sub_risk) VALUES(300,100,10,20,false,200,'Original subrisk')");
        jdbc.update("INSERT INTO approval(id,organization_id,company_id,deleted,source_module,source_record_id,status) VALUES(400,10,20,false,'riskReview','200','APPROVED')");
    }
    @AfterEach void close(){CompanyContext.clear();OrganizationContext.clear();UserContext.clear();if(source!=null)source.destroy();}
    StartPeriodEvaluationRequest start(int quarter){return new StartPeriodEvaluationRequest(EvaluationModule.REVIEW,200,2026,YearType.CALENDAR,Frequency.QUARTERLY,quarter);}
    long id(Map<String,Object> result){return ((Number)result.get("id")).longValue();}
    void finish(long id,long revision,String date) throws Exception {
        service.edit(id,new UpdatePeriodEvaluationRequest(revision,Map.of("review_type",json.valueToTree("Quantitative"),"annual_loss_expectancy",json.valueToTree(50),"currency",json.valueToTree("INR"),"value_unit",json.valueToTree("CRORES"))));
        service.complete(id,new CompletePeriodEvaluationRequest(revision+1,LocalDate.parse(date)));
    }
    @Test void nextPeriodPreservesBaselineLinksApprovalAndCompletedValues() throws Exception {
        var first=service.start(start(1));long firstId=id(first);
        assertThat(((JsonNode)first.get("source_snapshot")).path("subRisks").get(0).path("id").asLong()).isEqualTo(300);
        finish(firstId,0,"2026-02-01");
        var second=service.start(start(2));
        assertThat(second.get("previous_evaluation_id")).isEqualTo(firstId);
        assertThat(((JsonNode)second.get("evaluation_values")).isEmpty()).isTrue();
        assertThatThrownBy(()->service.edit(firstId,new UpdatePeriodEvaluationRequest(2,Map.of("status",json.valueToTree("changed"))))).hasMessageContaining("immutable");
        jdbc.update("UPDATE risk_reviews SET annual_loss_expectancy=999 WHERE id=200");
        assertThat(((JsonNode)service.get(firstId).get("source_snapshot")).path("record").path("annual_loss_expectancy").asDouble()).isEqualTo(100);
        assertThat(jdbc.queryForObject("SELECT risk_review_id FROM sub_risk WHERE id=300",Long.class)).isEqualTo(200);
        assertThat(jdbc.queryForObject("SELECT status FROM approval WHERE id=400",String.class)).isEqualTo("APPROVED");
        assertThat(jdbc.queryForObject("SELECT COUNT(*) FROM risk_reviews",Long.class)).isEqualTo(1);
        assertThat(service.history(EvaluationModule.REVIEW,200,0,20)).hasSize(2);
    }
    @Test void duplicateStaleUnsafeAndOutOfPeriodUpdatesFailWithoutChangingHistory() throws Exception {
        long id=id(service.start(start(1)));
        assertThatThrownBy(()->service.start(start(1))).hasMessageContaining("draft");
        assertThatThrownBy(()->service.edit(id,new UpdatePeriodEvaluationRequest(0,Map.of("risk_id",json.valueToTree(999))))).isInstanceOf(IllegalArgumentException.class);
        service.edit(id,new UpdatePeriodEvaluationRequest(0,Map.of("review_type",json.valueToTree("Qualitative"),"residual_risk_rating",json.valueToTree("Low"))));
        assertThatThrownBy(()->service.edit(id,new UpdatePeriodEvaluationRequest(0,Map.of("status",json.valueToTree("x"))))).hasMessageContaining("reload");
        assertThatThrownBy(()->service.complete(id,new CompletePeriodEvaluationRequest(1,LocalDate.of(2026,4,1)))).isInstanceOf(IllegalArgumentException.class);
        service.complete(id,new CompletePeriodEvaluationRequest(1,LocalDate.of(2026,3,31)));
        assertThatThrownBy(()->service.start(start(1))).hasMessageContaining("overlaps");
        assertThat(jdbc.queryForObject("SELECT COUNT(*) FROM period_evaluation",Long.class)).isEqualTo(1);
    }
    @Test void differentTenantAndBasicUserCannotReadOrEdit() throws Exception {
        long id=id(service.start(start(1)));
        when(resolver.resolve(20L,null,null)).thenReturn(new ChartScope(11L,20L,null,null,null,false,null,null));
        assertThatThrownBy(()->service.get(id)).hasMessageContaining("not found");
        when(resolver.resolve(20L,null,null)).thenReturn(new ChartScope(10L,20L,99L,null,null,false,null,null));
        assertThatThrownBy(()->service.get(id)).hasMessageContaining("outside user scope");
        assertThatThrownBy(()->service.edit(id,new UpdatePeriodEvaluationRequest(0,Map.of("status",json.valueToTree("x"))))).hasMessageContaining("outside user scope");
        // Assignment grants read-only access to the specific evaluation, never draft editing.
        var user=new User();user.setId(99L);UserContext.seetUser(user);
        jdbc.update("INSERT INTO approval(id,organization_id,company_id,deleted,source_module,source_record_id,status,approver_id) VALUES(401,10,20,false,'periodEvaluation',?,'PENDING',99)",Long.toString(id));
        assertThat(service.get(id).get("id")).isEqualTo(id);
        assertThatThrownBy(()->service.edit(id,new UpdatePeriodEvaluationRequest(0,Map.of("status",json.valueToTree("x"))))).hasMessageContaining("outside user scope");
    }
    @Test void evidenceReferencesAndControlChildrenRemainWithOriginalRecords() throws Exception {
        String document="dc615167-19a8-460f-aea0-2e3bf21b1e34";
        jdbc.update("INSERT INTO risk_risponse_treatment(id,risk_id,organization_id,company_id,deleted,created_by_id,supporting_evidence_document) VALUES(201,100,10,20,false,30,?)",document);
        jdbc.update("INSERT INTO risk_controls(id,risk_id,organization_id,company_id,deleted,created_by_id,control_title) VALUES(202,100,10,20,false,30,'Control')");
        jdbc.update("INSERT INTO risk_sub_control(id,risk_control_id,organization_id,company_id,deleted,sub_control_title) VALUES(303,202,10,20,false,'Subcontrol')");
        var treatment=service.start(new StartPeriodEvaluationRequest(EvaluationModule.TREATMENT,201,2026,YearType.CALENDAR,Frequency.QUARTERLY,1));
        assertThat((List<?>)treatment.get("evidence")).hasSize(1);assertThat(service.retainsDocument(document)).isTrue();
        assertThatThrownBy(()->service.guardTreatmentEvidenceDeletion(201,document)).hasMessageContaining("retained in evaluation history");
        assertThatThrownBy(()->service.guardTreatmentEvidenceDeletion(201,"aa615167-19a8-460f-aea0-2e3bf21b1e34")).hasMessageContaining("not associated");
        var control=service.start(new StartPeriodEvaluationRequest(EvaluationModule.CONTROL,202,2026,YearType.CALENDAR,Frequency.QUARTERLY,1));
        assertThat(((JsonNode)control.get("source_snapshot")).path("subControls")).hasSize(1);
        assertThat(jdbc.queryForObject("SELECT risk_control_id FROM risk_sub_control WHERE id=303",Long.class)).isEqualTo(202);
    }
    @Test void reassignedLegacyRiskCannotMixTwoRisksInOneHistory() throws Exception {
        long first=id(service.start(start(1)));finish(first,0,"2026-02-01");
        jdbc.update("INSERT INTO risk(id,organization_id,company_id,deleted,created_by_id) VALUES(101,10,20,false,30)");
        jdbc.update("UPDATE risk_reviews SET risk_id=101 WHERE id=200");
        assertThatThrownBy(()->service.start(start(2))).hasMessageContaining("Source risk changed");
        assertThat(service.history(EvaluationModule.REVIEW,200,0,20)).hasSize(1);
    }
    @Test void historyFiltersAssignedApprovalsBeforePagination() throws Exception {
        long first=id(service.start(start(1)));finish(first,0,"2026-02-01");
        service.start(start(2));
        var user=new User();user.setId(99L);UserContext.seetUser(user);
        when(resolver.resolve(20L,null,null)).thenReturn(new ChartScope(10L,20L,99L,null,null,false,null,null));
        jdbc.update("INSERT INTO approval(id,organization_id,company_id,deleted,source_module,source_record_id,status,approver_id) VALUES(401,10,20,false,'periodEvaluation',?,'PENDING',99)",Long.toString(first));
        assertThat(service.history(EvaluationModule.REVIEW,200,0,1)).hasSize(1).allSatisfy(item->assertThat(item.get("id")).isEqualTo(first));
        assertThat(service.history(EvaluationModule.REVIEW,200,1,1)).isEmpty();
    }
    @Test void completedReviewReachesDashboardButDraftDoesNot() throws Exception {
        jdbc.update("UPDATE risk_reviews SET last_evaluation_date='2026-02-01',created_at='2026-02-01' WHERE id=200");
        var charts=new DashboardChartRepository(new NamedParameterJdbcTemplate(source));
        var scope=new ChartScope(10L,20L,null,null,null,false,null,null);
        long id=id(service.start(start(1)));
        assertThat(((Number)charts.exposure(scope,ChartPeriod.of(2026,YearType.CALENDAR,1),true).get(0).get("total")).doubleValue()).isEqualTo(100);
        finish(id,0,"2026-02-01");
        assertThat(((Number)charts.exposure(scope,ChartPeriod.of(2026,YearType.CALENDAR,null),true).get(0).get("total")).doubleValue()).isEqualTo(50);
        service.start(start(2));
        assertThat(charts.exposure(scope,ChartPeriod.of(2026,YearType.CALENDAR,2),true)).isEmpty();
    }
    @Test void kriAndKpiPeriodActualsReachChartsWithoutEditingAnnualSource() throws Exception {
        jdbc.update("INSERT INTO user(id,deleted,organization_id,company_id,branch_id) VALUES(30,false,10,20,40)");
        jdbc.update("INSERT INTO kri_kpi_reviews(id,risk_id,organization_id,company_id,deleted,created_by_id,reporting_year,reporting_year_type,kri_evaluation_frequency,reporting_frequency,target_value,q1) VALUES(201,100,10,20,false,30,2026,'CALENDAR','Quarterly','Quarterly','100','70')");
        jdbc.update("INSERT INTO kpa_kpi_review(id,kpa,key_performance_indicator,owner_id,kpi_evaluation_by,organization_id,company_id,is_deleted,created_by_id,reporting_year,reporting_year_type,kpi_evaluation_frequency,reporting_frequency,status,target_value,q1) VALUES(202,'KRA','KPI',30,30,10,20,false,30,2026,'CALENDAR','Quarterly','Quarterly','Active',100,70)");
        var charts=new DashboardChartService(new DashboardChartRepository(new NamedParameterJdbcTemplate(source)),resolver);
        for(EvaluationModule module:List.of(EvaluationModule.KRI,EvaluationModule.KPI)) {
            long original=module==EvaluationModule.KRI?201:202;
            var request=new StartPeriodEvaluationRequest(module,original,2026,YearType.CALENDAR,Frequency.QUARTERLY,2);
            long id=id(service.start(request));
            service.edit(id,new UpdatePeriodEvaluationRequest(0,Map.of("actual_value",json.valueToTree(80))));
            service.complete(id,new CompletePeriodEvaluationRequest(1,LocalDate.of(2026,5,1)));
            assertThat(jdbc.queryForObject("SELECT q2 FROM "+module.table+" WHERE id="+original,String.class)).isNull();
            jdbc.update("UPDATE "+module.table+" SET target_value=999,q1=888 WHERE id="+original);
            var series=charts.performance(ChartPeriod.of(2026,YearType.CALENDAR,null),Indicator.valueOf(module.name()),Frequency.QUARTERLY,20L,null,null,0,20).series().get(0);
            assertThat(series.target()).isEqualByComparingTo("100");
            assertThat(series.actuals().get(0)).isEqualByComparingTo("70");
            assertThat(series.actuals().get(1)).isEqualByComparingTo("80");
            assertThat(series.yearBasis()).isEqualTo("PERIOD_HISTORY");
        }
    }
}
