package ermorg.erm.repository;

import java.time.LocalDate;
import java.util.*;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Repository;
import ermorg.erm.dashboard.ChartPeriod;
import ermorg.erm.dashboard.ChartScope;

/** Scalar batch reads reuse the chart authorization predicates. No JPA graph or N+1 lookups. */
@Repository
public class DashboardOverviewRepository {
    private final NamedParameterJdbcTemplate jdbc;
    private final DashboardChartRepository charts;
    public DashboardOverviewRepository(NamedParameterJdbcTemplate jdbc, DashboardChartRepository charts) {
        this.jdbc=jdbc; this.charts=charts;
    }
    public List<Map<String,Object>> risks(ChartScope scope, LocalDate start, LocalDate end) {
        var f=charts.filter(scope,"r.organization_id","r.company_id","r.created_by_id","r.branch_id","r.business_function");
        f.params().addValue("start",start.toString()).addValue("end",end.toString()).addValue("viewer",scope.userId());
        String[] fields={"revised_likelihood","revise_impact_score","residual_risk_rating","residual_risk_score_range",
            "revised_velocity","risk_appetite_status","risk_tolerance_status","risk_acceptance_level",
            "risk_treatment_status","review_type","annual_loss_expectancy","currency","value_unit"};
        String legacy=String.join(",",Arrays.stream(fields).map(s->"v."+s).toList());
        String history=String.join(",",Arrays.stream(fields).map(s->
            "NULLIF(JSON_UNQUOTE(JSON_EXTRACT(e.evaluation_values,'$."+s+"')),'null') AS "+s).toList());
        String query="""
            WITH scoped AS (
                SELECT DISTINCT r.id,r.organization_id,r.company_id,r.created_by_id,r.created_at risk_created_at,
                    r.risk_title,r.risk_category,r.risk_source,r.risk_status,r.risk_appetite_status,
                    r.risk_acceptance_level,r.branch_id,r.business_function,r.owner_id
                FROM risk r WHERE r.deleted=false AND r.created_at<:end
            """+f.sql()+"""
                AND (UPPER(COALESCE(r.risk_status,''))<>'DRAFT'
                    OR (:viewer IS NOT NULL AND r.created_by_id=:viewer)
                    OR (:viewer IS NOT NULL AND EXISTS (SELECT 1 FROM approval ap
                        WHERE ap.deleted=false AND ap.organization_id=r.organization_id
                            AND ap.approver_id=:viewer
                            AND ap.source_record_id=CAST(r.id AS CHAR)
                            AND REGEXP_REPLACE(LOWER(ap.source_module),'[^a-z0-9]','') IN ('risk','riskregister'))))
            ), assessments AS (
                SELECT a.*, ROW_NUMBER() OVER(PARTITION BY a.risk_id ORDER BY
                    COALESCE(a.last_evaluation_date,DATE(a.created_at)) DESC,a.created_at DESC,a.id DESC) rn
                FROM risk_assessment a JOIN scoped r ON r.id=a.risk_id
                    AND a.organization_id=r.organization_id AND a.company_id=r.company_id
                WHERE a.deleted=false AND COALESCE(a.last_evaluation_date,DATE(a.created_at))<:end
            ), review_candidates AS (
                SELECT v.risk_id,v.id,COALESCE(v.last_evaluation_date,DATE(v.created_at)) evaluated,v.created_at,0 history_priority,
            """+legacy+"""
                FROM risk_reviews v JOIN scoped r ON r.id=v.risk_id
                    AND v.organization_id=r.organization_id AND v.company_id=r.company_id
                WHERE v.deleted=false AND COALESCE(v.last_evaluation_date,DATE(v.created_at))>=:start
                    AND COALESCE(v.last_evaluation_date,DATE(v.created_at))<:end
                UNION ALL
                SELECT e.risk_id,e.id,e.effective_date evaluated,e.created_at,1 history_priority,
            """+history+"""
                FROM period_evaluation e JOIN scoped r ON r.id=e.risk_id
                    AND e.organization_id=r.organization_id AND e.company_id=r.company_id
                WHERE e.module='REVIEW' AND e.state='COMPLETED' AND e.effective_date>=:start AND e.effective_date<:end
            ), reviews AS (
                SELECT v.*,ROW_NUMBER() OVER(PARTITION BY risk_id ORDER BY evaluated DESC,history_priority DESC,created_at DESC,id DESC) rn
                FROM review_candidates v
            )
            SELECT r.id,r.created_by_id,r.risk_created_at,r.company_id,c.name company_name,
                r.risk_title,r.risk_category,r.risk_source,r.risk_status,r.risk_appetite_status AS baseline_appetite,
                r.risk_acceptance_level,r.branch_id,r.business_function,r.owner_id,b.name branch_name,d.name department_name,
                CASE WHEN u.organization_id=r.organization_id OR ud.organization_id=r.organization_id
                    THEN COALESCE(NULLIF(TRIM(CONCAT_WS(' ',ud.first_name,ud.last_name)),''),u.email) END owner_name,
                a.id assessment_id,a.risk_analysis_type,a.likelihood,a.gross_impact_score,a.risk_rating,a.risk_rating_score,
                a.velocity,a.risk_priority,a.risk_treatment_strategy,a.risk_appetite_status assessment_appetite,
                a.yearly_loss_expectancy,a.currency assessment_currency,a.value_unit assessment_value_unit,
                v.id review_id,
            """+String.join(",",Arrays.stream(fields).map(s->"v."+s).toList())+"\n"+"""
            FROM scoped r LEFT JOIN assessments a ON a.risk_id=r.id AND a.rn=1
                LEFT JOIN reviews v ON v.risk_id=r.id AND v.rn=1
                LEFT JOIN branch b ON b.id=r.branch_id AND b.organization_id=r.organization_id AND b.deleted=false
                LEFT JOIN department d ON d.id=r.business_function AND d.organization_id=r.organization_id AND d.deleted=false
                LEFT JOIN company c ON c.id=r.company_id AND c.organization_id=r.organization_id AND c.deleted=false
                LEFT JOIN user u ON u.id=r.owner_id AND (u.deleted=false OR u.deleted IS NULL)
                    AND (u.organization_id IS NULL OR u.organization_id=r.organization_id)
                LEFT JOIN user_detail ud ON ud.id=u.user_detail_id AND ud.organization_id=r.organization_id
            ORDER BY r.id DESC
            """;
        return jdbc.queryForList(query,f.params());
    }

    /** Rows are deduplicated by score id in the service because a maturity can cover multiple departments. */
    public List<Map<String,Object>> maturities(ChartScope scope,LocalDate start,LocalDate end) {
        var f=charts.filter(scope,"m.organization_id","m.company_id","m.created_by_id","d.branch_id","d.id");
        f.params().addValue("start",start.toString()).addValue("end",end.toString());
        return jdbc.queryForList("""
            SELECT m.id,m.erm_maturity_id,m.company_id,c.name company_name,d.id department_id,d.name department_name,
                s.id score_id,s.marks_achieved
            FROM erm_maturity_assessments m
                JOIN company c ON c.id=m.company_id AND c.organization_id=m.organization_id AND c.deleted=false
                LEFT JOIN erm_maturity_department_ids md ON md.maturity_assessment_id=m.id AND md.department_id<>0
                LEFT JOIN department d ON d.id=md.department_id AND d.organization_id=m.organization_id AND d.deleted=false
                LEFT JOIN erm_maturity_scores s ON s.maturity_assessment_id=m.id AND s.deleted=false
            WHERE m.deleted=false AND m.created_at>=:start AND m.created_at<:end
                AND (md.department_id IS NULL OR d.id IS NOT NULL)
            """+f.sql()+" ORDER BY m.erm_maturity_id,m.id,d.id,s.id",f.params());
    }

    public List<Map<String,Object>> reportingFrequencies(ChartScope scope, LocalDate start, LocalDate end) {
        var f=charts.filter(scope,"k.organization_id","k.company_id","k.created_by_id","r.branch_id","k.business_function");
        f.params().addValue("start",start.toString()).addValue("end",end.toString()).addValue("viewer",scope.userId());
        return jdbc.queryForList("""
            SELECT UPPER(TRIM(COALESCE(NULLIF(TRIM(k.kri_evaluation_frequency),''),k.reporting_frequency))) AS item_key,
                COUNT(DISTINCT k.id) AS item_count
            FROM kri_kpi_reviews k JOIN risk r ON r.id=k.risk_id AND r.organization_id=k.organization_id
                AND r.company_id=k.company_id AND r.deleted=false
            WHERE k.deleted=false AND k.created_at>=:start AND k.created_at<:end
                AND (UPPER(COALESCE(r.risk_status,''))<>'DRAFT'
                    OR (:viewer IS NOT NULL AND r.created_by_id=:viewer)
                    OR (:viewer IS NOT NULL AND EXISTS (SELECT 1 FROM approval ap
                        WHERE ap.deleted=false AND ap.organization_id=r.organization_id
                            AND ap.approver_id=:viewer
                            AND ap.source_record_id=CAST(r.id AS CHAR)
                            AND REGEXP_REPLACE(LOWER(ap.source_module),'[^a-z0-9]','') IN ('risk','riskregister'))))
            """+f.sql()+" GROUP BY item_key ORDER BY item_key",f.params());
    }

    public List<Map<String,Object>> treatments(ChartScope scope, LocalDate start, LocalDate end) {
        var f=charts.filter(scope,"r.organization_id","r.company_id","r.created_by_id","r.branch_id","r.business_function");
        f.params().addValue("start",start.toString()).addValue("end",end.toString()).addValue("viewer",scope.userId());
        return jdbc.queryForList("""
            SELECT rt.id AS id,
                COALESCE(JSON_UNQUOTE(JSON_EXTRACT(e.evaluation_values,'$.risk_treatment_status')),
                    rt.risk_treatment_status) AS treatment_status,
                COALESCE(JSON_UNQUOTE(JSON_EXTRACT(e.evaluation_values,'$.control_evaluation_status')),
                    rt.control_evaluation_status) AS control_evaluation_status,
                CAST(COALESCE(JSON_UNQUOTE(JSON_EXTRACT(e.evaluation_values,'$.control_effectiveness_percentage')),
                    rt.control_effectiveness_percentage) AS DECIMAL(19,4)) AS control_effectiveness_percent,
                COALESCE(e.effective_date,rt.updated_at,rt.created_at) AS evaluated_at,
                JSON_UNQUOTE(JSON_EXTRACT(e.evaluation_values,'$.due_date')) AS due_date,
                e.effective_date AS actual_date,
                r.id risk_id,
                r.risk_title AS linked_risk_title,r.risk_status
            FROM risk_risponse_treatment rt JOIN risk r ON r.id=rt.risk_id AND r.organization_id=rt.organization_id
                AND r.company_id=rt.company_id AND r.deleted=false
            LEFT JOIN period_evaluation e ON e.organization_id=rt.organization_id AND e.company_id=rt.company_id
                AND e.module='TREATMENT' AND e.source_id=rt.id AND e.state='COMPLETED'
                AND e.period_end=(SELECT MAX(e2.period_end) FROM period_evaluation e2
                    WHERE e2.organization_id=e.organization_id AND e2.company_id=e.company_id
                        AND e2.module='TREATMENT' AND e2.source_id=e.source_id AND e2.state='COMPLETED')
            WHERE rt.deleted=false AND rt.created_at<:end
                AND (UPPER(COALESCE(r.risk_status,''))<>'DRAFT'
                    OR (:viewer IS NOT NULL AND r.created_by_id=:viewer)
                    OR (:viewer IS NOT NULL AND EXISTS (SELECT 1 FROM approval ap
                        WHERE ap.deleted=false AND ap.organization_id=r.organization_id
                            AND ap.approver_id=:viewer
                            AND ap.source_record_id=CAST(r.id AS CHAR)
                            AND REGEXP_REPLACE(LOWER(ap.source_module),'[^a-z0-9]','') IN ('risk','riskregister'))))
            """+f.sql()+" ORDER BY rt.id DESC",f.params());
    }

    public List<Map<String,Object>> controls(ChartScope scope) {
        var f=charts.filter(scope,"r.organization_id","r.company_id","r.created_by_id","r.branch_id","r.business_function");
        return jdbc.queryForList("""
            SELECT c.id,c.control_type,c.control_mechanism,c.control_assessment_frequency,
                c.primary_responsible_id,c.last_control_assessment_date,
                JSON_UNQUOTE(JSON_EXTRACT(e.evaluation_values,'$.control_effectiveness_percentage')) AS control_effectiveness_percent,
                JSON_UNQUOTE(JSON_EXTRACT(e.evaluation_values,'$.control_evaluation_status')) AS control_evaluation_status
            FROM risk_controls c JOIN risk r ON r.id=c.risk_id AND r.organization_id=c.organization_id
                AND r.company_id=c.company_id AND r.deleted=false
            LEFT JOIN period_evaluation e ON e.organization_id=c.organization_id AND e.company_id=c.company_id
                AND e.module='CONTROL' AND e.source_id=c.id AND e.state='COMPLETED'
                AND e.period_end=(SELECT MAX(e2.period_end) FROM period_evaluation e2
                    WHERE e2.organization_id=e.organization_id AND e2.company_id=e.company_id
                        AND e2.module='CONTROL' AND e2.source_id=e.source_id AND e2.state='COMPLETED')
            WHERE c.deleted=false
                AND (UPPER(COALESCE(r.risk_status,''))<>'DRAFT'
                    OR (:viewer IS NOT NULL AND r.created_by_id=:viewer)
                    OR (:viewer IS NOT NULL AND EXISTS (SELECT 1 FROM approval ap
                        WHERE ap.deleted=false AND ap.organization_id=r.organization_id
                            AND ap.approver_id=:viewer
                            AND ap.source_record_id=CAST(r.id AS CHAR)
                            AND REGEXP_REPLACE(LOWER(ap.source_module),'[^a-z0-9]','') IN ('risk','riskregister'))))
            """+f.sql()+" ORDER BY c.id",f.params().addValue("viewer",scope.userId()));
    }

    public List<Map<String,Object>> assignedApprovals(ChartScope scope, LocalDate start, LocalDate end) {
        if(scope.userId()==null)return List.of();
        return jdbc.queryForList("""
            SELECT a.id,a.source_module,a.source_record_id,a.record_name,a.status,a.due_at,a.company_id,
                a.created_at
            FROM approval a WHERE a.deleted=false AND a.organization_id=:org AND a.approver_id=:user
                AND a.status='PENDING'
            ORDER BY a.due_at,a.id
            """,new MapSqlParameterSource("org",scope.organizationId()).addValue("user",scope.userId())
                .addValue("start",start.toString()).addValue("end",end.toString()));
    }

    public List<Map<String,Object>> kriEvaluations(ChartScope scope, ChartPeriod period,
            ChartPeriod.Frequency frequency) {
        var f=charts.filter(scope,"k.organization_id","k.company_id","k.created_by_id","r.branch_id","k.business_function");
        f.params().addValue("year",period.year()).addValue("yearType",period.yearType().name())
            .addValue("start",period.start().toString()).addValue("end",period.end().toString());
        f.params().addValue("frequency",frequency.name());
        String columns=String.join(",",period.columns(frequency).stream().map(c->"k."+c).toList());
        return jdbc.queryForList("SELECT k.id,k.risk_id,k.key_risk_indicator_kri AS indicator,k.types_of_key_risk_indicator_kri AS source,"
            +"k.unit_of_measurement AS uom,k.target_value AS threshold,k.actual_value AS current_value,"
            +"k.risk_tolerance_range_min_value,k.risk_tolerance_range_max_value,k.thresholds,k.status,"
            +"k.risk_tolerance_status AS tolerance_status,k.kri_appetite_status,k.reporting_frequency,"
            +"k.risk_owner_id,k.business_function,k.due_date,k.actual_date,d.name AS function_name,"+columns+" FROM kri_kpi_reviews k JOIN risk r ON r.id=k.risk_id "
            +"AND r.organization_id=k.organization_id AND r.company_id=k.company_id AND r.deleted=false "
            +"LEFT JOIN department d ON d.id=k.business_function AND d.organization_id=k.organization_id AND d.deleted=false "
            +"WHERE k.deleted=false AND ((k.reporting_year=:year AND k.reporting_year_type=:yearType) "
            +"OR (k.reporting_year IS NULL AND k.created_at>=:start AND k.created_at<:end)) "
            +"AND UPPER(TRIM(COALESCE(NULLIF(TRIM(k.kri_evaluation_frequency),''),k.reporting_frequency)))=:frequency"
            +" AND (UPPER(COALESCE(r.risk_status,''))<>'DRAFT' OR (:viewer IS NOT NULL AND r.created_by_id=:viewer)"
            +" OR (:viewer IS NOT NULL AND EXISTS (SELECT 1 FROM approval ap WHERE ap.deleted=false"
            +" AND ap.organization_id=r.organization_id AND ap.approver_id=:viewer"
            +" AND ap.source_record_id=CAST(r.id AS CHAR) AND REGEXP_REPLACE(LOWER(ap.source_module),'[^a-z0-9]','') IN ('risk','riskregister'))))"
            +f.sql()+" ORDER BY k.id",f.params().addValue("viewer",scope.userId()));
    }

    public List<Map<String,Object>> kpiEvaluations(ChartScope scope, ChartPeriod period) {
        var f=charts.filter(scope,"k.organization_id","k.company_id","k.created_by_id","u.branch_id","k.business_function");
        f.params().addValue("year",period.year()).addValue("yearType",period.yearType().name())
            .addValue("start",period.start().toString()).addValue("end",period.end().toString());
        return jdbc.queryForList("""
            SELECT k.id,k.key_performance_indicator AS indicator,k.types_of_kpi AS source,k.business_function,d.name AS function_name,
                k.level_of_measurement_level AS uom,k.target_value AS threshold,k.actual_value AS current_value,
                k.q1,k.q2,k.q3,k.q4,k.kra_rating,k.status,k.performance_tolerance_min_value,
                k.performance_tolerance_max_value,k.risk_appetite_status AS tolerance_status,k.owner_id,k.due_date,k.actual_date
            FROM kpa_kpi_review k JOIN user u ON u.id=k.owner_id AND u.deleted=false
                AND u.company_id=k.company_id AND (u.organization_id=k.organization_id OR
                (u.organization_id IS NULL AND EXISTS (SELECT 1 FROM user_detail ud WHERE ud.id=u.user_detail_id
                AND ud.organization_id=k.organization_id)))
                LEFT JOIN department d ON d.id=k.business_function AND d.organization_id=k.organization_id AND d.deleted=false
            WHERE k.is_deleted=false AND ((k.reporting_year=:year AND k.reporting_year_type=:yearType)
                OR (k.reporting_year IS NULL AND k.created_at>=:start AND k.created_at<:end))
            """+f.sql()+" AND k.owner_id=:owner ORDER BY k.id",f.params().addValue("owner",scope.userId()));
    }

    public List<Map<String,Object>> dynamicCategories(ChartScope scope) {
        return jdbc.queryForList("""
            SELECT DISTINCT c.id,c.category_name AS display_label
            FROM category c JOIN organization_module om ON om.category_id=c.id
            WHERE c.deleted=false AND om.deleted=false AND om.org_id=:org
                AND om.module_id=c.module_id
                AND (om.field_id IS NULL OR EXISTS (SELECT 1 FROM fields cf
                    WHERE cf.id=om.field_id AND cf.category_id=c.id AND cf.deleted=false))
            ORDER BY c.display_order,c.id
            """,new MapSqlParameterSource("org",scope.organizationId()));
    }
}
