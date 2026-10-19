package ermorg.erm.repository;

import java.time.LocalDate;
import java.util.*;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Repository;
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
        f.params().addValue("start",start.toString()).addValue("end",end.toString());
        String[] fields={"revised_likelihood","revise_impact_score","residual_risk_rating","residual_risk_score_range",
            "revised_velocity","risk_appetite_status","risk_treatment_status","review_type","annual_loss_expectancy","currency","value_unit"};
        String legacy=String.join(",",Arrays.stream(fields).map(s->"v."+s).toList());
        String history=String.join(",",Arrays.stream(fields).map(s->
            "NULLIF(JSON_UNQUOTE(JSON_EXTRACT(e.evaluation_values,'$."+s+"')),'null') AS "+s).toList());
        String query="""
            WITH scoped AS (
                SELECT DISTINCT r.id,r.organization_id,r.company_id,r.risk_title,r.risk_category,r.risk_source,
                    r.risk_status,r.risk_appetite_status,r.branch_id,r.business_function,r.owner_id
                FROM risk r WHERE r.deleted=false AND r.created_at<:end
            """+f.sql()+"""
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
            SELECT r.id,r.risk_title,r.risk_category,r.risk_source,r.risk_status,r.risk_appetite_status AS baseline_appetite,
                r.branch_id,r.business_function,r.owner_id,b.name branch_name,d.name department_name,
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
}
