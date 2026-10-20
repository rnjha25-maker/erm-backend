package ermorg.erm.repository;
import ermorg.erm.dashboard.*;

import java.util.*;
import org.springframework.jdbc.core.namedparam.*;
import org.springframework.stereotype.Repository;

/** Scalar SQL only: no entity graph hydration or per-record database lookups. MySQL 8+. */
@Repository
public class DashboardChartRepository {
    private final NamedParameterJdbcTemplate jdbc;
    public DashboardChartRepository(NamedParameterJdbcTemplate jdbc) { this.jdbc = jdbc; }

    record Filter(String sql, MapSqlParameterSource params) {}
    Filter filter(ChartScope scope, String org, String company, String creator, String branch, String department) {
        MapSqlParameterSource p = new MapSqlParameterSource("org", scope.organizationId());
        String sql = " AND " + org + " = :org";
        if (scope.companyId() != null) { sql += " AND " + company + " = :company"; p.addValue("company", scope.companyId()); }
        if (scope.creatorId() != null) { sql += " AND " + creator + " = :creator"; p.addValue("creator", scope.creatorId()); }
        if (scope.branchId() != null) { sql += " AND " + branch + " = :branch"; p.addValue("branch", scope.branchId()); }
        // KRI/KPI store department IDs as text. Bind text to avoid MySQL matching '50abc' to numeric 50.
        if (scope.departmentId() != null) { sql += " AND " + department + " = :department"; p.addValue("department", scope.departmentId().toString()); }
        if (scope.restricted()) {
            sql += " AND (";
            List<String> terms = new ArrayList<>();
            if (scope.assignedBranchId() != null) { terms.add(branch + " = :assignedBranch"); p.addValue("assignedBranch", scope.assignedBranchId()); }
            if (scope.assignedDepartmentId() != null) { terms.add(department + " = :assignedDepartment"); p.addValue("assignedDepartment", scope.assignedDepartmentId().toString()); }
            sql += terms.isEmpty() ? "1=0)" : String.join(" OR ", terms) + ")";
        }
        return new Filter(sql, p);
    }

    public List<Map<String,Object>> performance(ChartScope scope, ChartPeriod period,
            ChartPeriod.Indicator indicator, ChartPeriod.Frequency frequency, int page, int size) {
        boolean kri = indicator == ChartPeriod.Indicator.KRI;
        String from = kri
            ? " FROM kri_kpi_reviews k JOIN risk r ON r.id=k.risk_id AND r.deleted=false AND r.organization_id=k.organization_id AND r.company_id=k.company_id"
            : " FROM kpa_kpi_review k LEFT JOIN user u ON u.id=k.owner_id AND u.deleted=false AND u.company_id=k.company_id"
                + " AND (u.organization_id=k.organization_id OR (u.organization_id IS NULL AND EXISTS"
                + " (SELECT 1 FROM user_detail owner_profile WHERE owner_profile.id=u.user_detail_id AND owner_profile.organization_id=k.organization_id)))";
        Filter f = filter(scope, "k.organization_id", "k.company_id", "k.created_by_id",
            kri ? "r.branch_id" : "u.branch_id", "k.business_function");
        // A performance row is an annual entry, even when only one quarter is requested.
        ChartPeriod year = ChartPeriod.of(period.year(), period.yearType(), null);
        f.params.addValue("start", year.start().toString()).addValue("end", year.end().toString())
            .addValue("year",year.year()).addValue("yearType",year.yearType().name())
            .addValue("frequency", frequency.name()).addValue("offset", (long) page * size).addValue("limit", size + 1);
        String eval = kri ? "k.kri_evaluation_frequency" : "k.kpi_evaluation_frequency";
        String name = kri ? "k.key_risk_indicator_kri" : "k.key_performance_indicator";
        String cols = String.join(", ", period.columns(frequency).stream().map(c -> "k." + c).toList());
        // Evaluation frequency is authoritative; reporting frequency is only a legacy fallback.
        return jdbc.queryForList("SELECT k.id, k.reporting_year, k.reporting_year_type, " + name + " AS indicator_name, k.target_value, k.currency, k.value_unit, "
            + (kri ? "k.unit_of_measurement" : "k.level_of_measurement_level") + " AS measurement_unit, " + cols + from
            + " WHERE k." + (kri ? "deleted" : "is_deleted") + "=false"
            + " AND ((k.reporting_year=:year AND k.reporting_year_type=:yearType)"
            + " OR (k.reporting_year IS NULL AND k.reporting_year_type IS NULL AND k.created_at>=:start AND k.created_at<:end))"
            + " AND UPPER(TRIM(COALESCE(NULLIF(TRIM(" + eval + "),''),k.reporting_frequency)))=:frequency"
            + f.sql + " ORDER BY k.id DESC LIMIT :limit OFFSET :offset", f.params);
    }

    public List<Map<String,Object>> exposure(ChartScope scope, ChartPeriod period, boolean residual) {
        String table = residual ? "risk_reviews" : "risk_assessment";
        String type = residual ? "review_type" : "risk_analysis_type";
        String value = residual ? "annual_loss_expectancy" : "yearly_loss_expectancy";
        Filter f = filter(scope, "r.organization_id", "r.company_id", "r.created_by_id", "r.branch_id", "r.business_function");
        f.params.addValue("start", period.start().toString()).addValue("end", period.end().toString());
        // Rank BEFORE dropping null ALE: never silently substitute an older value for a missing latest value.
        String ranked = "SELECT a." + value + " AS ale, a.currency, a.value_unit, "
            + "ROW_NUMBER() OVER (PARTITION BY a.risk_id ORDER BY COALESCE(a.last_evaluation_date,DATE(a.created_at)) DESC,a.created_at DESC,a.id DESC) AS rn"
            + " FROM " + table + " a JOIN risk r ON r.id=a.risk_id AND r.deleted=false"
            + " AND a.organization_id=r.organization_id AND a.company_id=r.company_id"
            + " WHERE a.deleted=false AND LOWER(TRIM(a." + type + "))='quantitative'"
            + evaluationWindow(residual) + f.sql;
        if(residual) {
            String history = "SELECT e.risk_id,e.organization_id,e.company_id,CAST(JSON_UNQUOTE(JSON_EXTRACT(e.evaluation_values,'$.annual_loss_expectancy')) AS DECIMAL(30,8)) AS ale,"
                + "JSON_UNQUOTE(JSON_EXTRACT(e.evaluation_values,'$.currency')) AS currency,JSON_UNQUOTE(JSON_EXTRACT(e.evaluation_values,'$.value_unit')) AS value_unit,"
                + "e.effective_date AS evaluated,e.created_at,e.id,1 AS history_priority"
                + " FROM period_evaluation e"
                + " WHERE e.module='REVIEW' AND e.state='COMPLETED' AND LOWER(JSON_UNQUOTE(JSON_EXTRACT(e.evaluation_values,'$.review_type')))='quantitative'"
                + " AND e.organization_id=:org AND e.effective_date>=:start AND e.effective_date<:end";
            String legacy="SELECT a.risk_id,a.organization_id,a.company_id,a.annual_loss_expectancy AS ale,a.currency,a.value_unit,COALESCE(a.last_evaluation_date,DATE(a.created_at)) AS evaluated,a.created_at,a.id,0 AS history_priority"
                + " FROM risk_reviews a"
                + " WHERE a.organization_id=:org AND a.deleted=false AND LOWER(TRIM(a.review_type))='quantitative'"+evaluationWindow(true);
            ranked="SELECT c.ale,c.currency,c.value_unit,ROW_NUMBER() OVER(PARTITION BY c.risk_id ORDER BY c.evaluated DESC,c.history_priority DESC,c.created_at DESC,c.id DESC) rn FROM ("+legacy+" UNION ALL "+history+") c JOIN risk r ON r.id=c.risk_id AND r.organization_id=c.organization_id AND r.company_id=c.company_id WHERE r.deleted=false"+f.sql;
        }
        return jdbc.queryForList("SELECT currency,value_unit,SUM(ale) AS total,COUNT(*) AS risk_count,"
            + "COUNT(ale) AS valued_count FROM (" + ranked + ") latest WHERE rn=1 GROUP BY currency,value_unit ORDER BY currency,value_unit", f.params);
    }

    public List<Map<String,Object>> performanceHistory(ChartScope scope,ChartPeriod period,ChartPeriod.Indicator indicator,ChartPeriod.Frequency frequency,List<Long> ids) {
        if(ids.isEmpty())return List.of();
        // IDs come from the scoped performance query; a null company means all permitted companies.
        return jdbc.queryForList("SELECT source_id,period_number,period_start,evaluation_values,source_snapshot FROM period_evaluation WHERE organization_id=:org AND (:company IS NULL OR company_id=:company) AND module=:module AND source_id IN (:ids) AND reporting_year=:year AND year_type=:yearType AND frequency=:frequency AND state='COMPLETED' ORDER BY period_start,id",
            new MapSqlParameterSource("org",scope.organizationId()).addValue("company",scope.companyId()).addValue("module",indicator.name())
                .addValue("ids",ids).addValue("year",period.year()).addValue("yearType",period.yearType().name()).addValue("frequency",frequency.name()));
    }

    public List<Map<String,Object>> trend(ChartScope scope, ChartPeriod period, boolean assessments) {
        Filter f = filter(scope, "r.organization_id", "r.company_id", "r.created_by_id", "r.branch_id", "r.business_function");
        f.params.addValue("start", period.start().toString()).addValue("end", period.end().toString());
        String date = assessments ? "a.created_at" : "r.created_at";
        String from = assessments ? "risk_assessment a JOIN risk r ON r.id=a.risk_id AND a.organization_id=r.organization_id AND a.company_id=r.company_id"
            : "risk r";
        return jdbc.queryForList("SELECT DATE_FORMAT(" + date + ",'%Y-%m') AS period,COUNT(*) AS total FROM " + from
            + " WHERE r.deleted=false" + (assessments ? " AND a.deleted=false" : "")
            + " AND " + date + ">=:start AND " + date + "<:end" + f.sql + " GROUP BY DATE_FORMAT(" + date + ",'%Y-%m')", f.params);
    }

    public List<Map<String,Object>> scatter(ChartScope scope, ChartPeriod period, int page, int size) {
        Filter f = filter(scope, "r.organization_id", "r.company_id", "r.created_by_id", "r.branch_id", "r.business_function");
        f.params.addValue("start", period.start().toString()).addValue("end", period.end().toString())
            .addValue("offset", (long)page * size).addValue("limit", size + 1);
        String ranked = "SELECT r.id AS risk_id,r.risk_title,r.risk_category,a.id AS assessment_id,"
            + "a.likelihood,a.gross_impact_score,a.risk_rating,"
            + "ROW_NUMBER() OVER(PARTITION BY a.risk_id ORDER BY COALESCE(a.last_evaluation_date,DATE(a.created_at)) DESC,a.created_at DESC,a.id DESC) rn"
            + " FROM risk_assessment a JOIN risk r ON r.id=a.risk_id AND r.deleted=false"
            + " AND a.organization_id=r.organization_id AND a.company_id=r.company_id WHERE a.deleted=false"
            + evaluationWindow(false) + f.sql;
        return jdbc.queryForList("SELECT risk_id,risk_title,risk_category,assessment_id,likelihood,gross_impact_score,risk_rating FROM ("
            + ranked + ") ranked WHERE rn=1 ORDER BY risk_id LIMIT :limit OFFSET :offset", f.params);
    }

    private static String evaluationWindow(boolean periodicReview) {
        // Identification/assessment are retained baselines, not recreated each quarter.
        // Reviews belong to the requested evaluation period. An unreviewed quarter stays empty.
        return " AND ((" + (periodicReview ? "a.last_evaluation_date>=:start AND " : "")
            + "a.last_evaluation_date<:end) OR (a.last_evaluation_date IS NULL AND "
            + (periodicReview ? "a.created_at>=:start AND " : "") + "a.created_at<:end))";
    }
}
