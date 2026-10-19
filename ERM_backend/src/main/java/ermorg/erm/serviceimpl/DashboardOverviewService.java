package ermorg.erm.serviceimpl;

import java.math.BigDecimal;
import java.time.*;
import java.time.format.TextStyle;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ermorg.erm.constant.*;
import ermorg.erm.dashboard.*;
import ermorg.erm.dto.response.*;
import ermorg.erm.dto.response.ErmDashboardOverviewResponse.*;
import ermorg.erm.exception.ResourceNotFoundException;
import ermorg.erm.mapping.FieldMapperUtils;
import ermorg.erm.model.*;
import ermorg.erm.repository.DashboardOverviewRepository;
import ermorg.erm.util.*;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
@Transactional(readOnly=true)
public class DashboardOverviewService {
    private static final List<String> LEVELS=List.of("CRITICAL","HIGH","MEDIUM","LOW","VERY_LOW");
    private final ChartScope.Resolver scopes;
    private final DashboardOverviewRepository repository;
    private final DashboardChartService charts;
    private final ErmRiskHeatmapBuilder heatmaps;
    private final FieldMapperUtils ratings;

    public record Window(LocalDate start, LocalDate end) {}
    public static Window window(int year, ChartPeriod.YearType type, ErmDashboardPeriodType period) {
        var annual=ChartPeriod.of(year,type,null);
        if(period==null)throw new IllegalArgumentException("periodType is required.");
        int offset=switch(period) {case Q1,H1,YEAR->0;case Q2->3;case Q3,H2->6;case Q4->9;};
        int months=switch(period) {case YEAR->12;case H1,H2->6;default->3;};
        var start=annual.start().plusMonths(offset);
        return new Window(start,start.plusMonths(months));
    }

    public ErmDashboardOverviewResponse overview(int year,ChartPeriod.YearType yearType,ErmDashboardPeriodType period,
            Long company,Long branch,Long function,ChartPeriod.Indicator indicator,ChartPeriod.Frequency frequency,
            int page,int size,int performancePage,int performanceSize,Long performanceRecordId) throws ResourceNotFoundException {
        DashboardChartService.validatePage(page,size);
        DashboardChartService.validatePage(performancePage,performanceSize);
        if(performanceRecordId!=null && performanceRecordId<=0)throw new IllegalArgumentException("performanceRecordId must be positive.");
        if(indicator==null || frequency==null)throw new IllegalArgumentException("indicator and frequency are required.");
        Window dates=window(year,yearType,period);
        ChartScope scope=scopes.resolve(company,branch,function);
        var rows=repository.risks(scope,dates.start(),dates.end());
        var response=assemble(rows,page,size);
        maturity(response,repository.maturities(scope,dates.start(),dates.end()));
        var performance=charts.performanceForScope(ChartPeriod.of(year,yearType,null),indicator,frequency,
            scope,performancePage,performanceSize);
        List<PerformanceSeries> series=new ArrayList<>();
        for(var source:performance.series()) {
            List<PerformancePoint> points=new ArrayList<>();
            for(int i=0;i<performance.labels().size();i++) {
                LocalDate pointDate=frequency==ChartPeriod.Frequency.MONTHLY
                    ? YearMonth.parse(performance.labels().get(i)).atDay(1)
                    : ChartPeriod.of(year,yearType,null).start().plusMonths(i*3L);
                if(pointDate.isBefore(dates.start()) || !pointDate.isBefore(dates.end()))continue;
                String label=frequency==ChartPeriod.Frequency.MONTHLY
                    ? pointDate.getMonth().getDisplayName(TextStyle.SHORT,Locale.ENGLISH) : "Q"+(i+1);
                points.add(new PerformancePoint(label,source.actuals().get(i),source.target()));
            }
            series.add(new PerformanceSeries(source.recordId(),source.indicator(),source.currency(),source.valueUnit(),
                source.measurementUnit(),points,source.invalidFields()));
        }
        response.setPerformanceSeries(series);
        response.setPerformancePage(new PerformancePage(performancePage,performanceSize,performance.hasNext()));
        if(performanceRecordId!=null) {
            var selected=series.stream().filter(s->s.recordId()==performanceRecordId).findFirst();
            if(selected.isEmpty())throw new IllegalArgumentException("performanceRecordId must identify a series on the requested performance page.");
            response.setPerformance(selected.get().points());
        } else if(performancePage==0 && !performance.hasNext() && series.size()==1) {
            response.setPerformance(series.get(0).points());
        } else if(!series.isEmpty()) {
            response.getNotes().add("Multiple performance series remain separate. Select performanceRecordId from this page for the performance array.");
        }
        response.getUnavailableMetrics().put("overdueEvaluations","Overdue evaluation rules have not been agreed.");
        response.getUnavailableMetrics().put("upcomingReview","Upcoming-review horizon has not been agreed.");
        response.getUnavailableMetrics().put("totalKris","Definition of active/in-scope KRI totals has not been agreed; performance pages are not totals.");
        response.getUnavailableMetrics().put("highRiskKris","High-risk KRI classification has not been agreed.");
        response.getNotes().add("Risk baseline is selected as of period end; latest review is selected within the period, including completed period evaluations.");
        response.getNotes().add("Lifecycle uses explicit Open/In Progress/Closed values only. Other statuses are Unknown, not inferred from approval status.");
        return response;
    }

    /** Single pass over one row per risk; all cards and register share this population. */
    ErmDashboardOverviewResponse assemble(List<Map<String,Object>> rows,int page,int size) {
        var result=new ErmDashboardOverviewResponse();
        result.setTotalRisks(rows.size());
        List<Map<String,Object>> normalized=new ArrayList<>();
        List<Risk> risks=new ArrayList<>(); Map<Long,RiskReview> reviews=new HashMap<>(); Map<String,String> owners=new HashMap<>();
        List<RegisterRow> register=new ArrayList<>();
        for(var raw:rows) {
            var row=new LinkedHashMap<String,Object>(raw);
            String rating=ratings.resolveResidualRating(text(row,"residual_risk_rating"),text(row,"residual_risk_score_range"));
            if(rating==null || rating.isBlank())rating=ratings.resolveRatingLabel(row.get("risk_rating"));
            row.put("level",level(rating));
            row.put("category",category(row.get("risk_category")));
            row.put("source",key(text(row,"risk_source")));
            var priority=ErmDashboardValueNormalizer.priorityBucket(text(row,"risk_priority"));
            row.put("priority",priority==null?"UNKNOWN":priority.name());
            row.put("strategy",key(text(row,"risk_treatment_strategy")));
            row.put("analysis",key(text(row,"risk_analysis_type")));
            row.put("appetite",appetite(first(row,"risk_appetite_status","assessment_appetite","baseline_appetite")));
            row.put("lifecycle",lifecycle(first(row,"risk_treatment_status","risk_status")));
            String impact=first(row,"revise_impact_score","gross_impact_score");
            var impactBucket=ErmDashboardValueNormalizer.impactBucket(impact);
            row.put("impact_band",impactBucket==null?"UNKNOWN":impactBucket.name());
            normalized.add(row);
            long id=((Number)row.get("id")).longValue();
            String owner=first(row,"owner_name");if(owner==null)owner="Unassigned";
            register.add(new RegisterRow(id,text(row,"risk_title"),text(row,"category"),owner,
                number(first(row,"revised_likelihood","likelihood")),number(impact),number(row.get("risk_rating_score")),
                number(row.get("residual_risk_score_range")),text(row,"level"),text(row,"risk_status")));
            boolean hasReview=row.get("review_id")!=null;
            String analysis=hasReview?text(row,"review_type"):text(row,"risk_analysis_type");
            if(ErmDashboardValueNormalizer.isQuantitative(analysis)) {
                var ale=number(row.get(hasReview?"annual_loss_expectancy":"yearly_loss_expectancy"));
                // Missing latest ALE stays missing. Never substitute an older assessment amount.
                result.getFinancialExposureByRisk().add(new ExposureRow(id,text(row,"risk_title"),ale,
                    text(row,hasReview?"currency":"assessment_currency"),text(row,hasReview?"value_unit":"assessment_value_unit"),
                    hasReview?"RESIDUAL":"INHERENT"));
            }
            Risk risk=new Risk();risk.setId(id);risk.setRisktitle(text(row,"risk_title"));
            try {risk.setCategory(RiskCategory.valueOf(text(row,"category")));}catch(IllegalArgumentException ignored) { }
            if(row.get("owner_id") instanceof Number ownerId) {
                User user=new User();user.setId(ownerId.longValue());risk.setRiskOwner(user);owners.put(user.getId().toString(),owner);
            }
            if(row.get("assessment_id")!=null) {
                RiskAssessment a=new RiskAssessment();a.setId(((Number)row.get("assessment_id")).longValue());
                a.setLikelihood(text(row,"likelihood"));a.setGrossImpactScore(text(row,"gross_impact_score"));
                a.setRiskRating(text(row,"risk_rating"));a.setVelocity(text(row,"velocity"));risk.setRiskAssessments(List.of(a));
            }
            if(hasReview) {
                RiskReview review=new RiskReview();review.setRevisedLikelihood(text(row,"revised_likelihood"));
                review.setReviseImpactScore(text(row,"revise_impact_score"));review.setResidualRiskRating(rating);
                review.setResidualRiskScoreRange(text(row,"residual_risk_score_range"));review.setRevisedVelocity(text(row,"revised_velocity"));
                reviews.put(id,review);
            }
            risks.add(risk);
        }
        result.setRiskLevelSummary(counts(normalized,"level",LEVELS));
        result.setRiskSummaryPriorityBased(counts(normalized,"priority",List.of("VERY_HIGH","HIGH","MODERATE","LOW","VERY_LOW")));
        result.setRiskByCategory(counts(normalized,"category",List.of()));
        result.setRiskBySource(counts(normalized,"source",List.of()));
        result.setRiskByImpact(counts(normalized,"impact_band",List.of("CATASTROPHIC","MAJOR","MODERATE","MINOR","INSIGNIFICANT")));
        result.setTreatmentStrategy(counts(normalized,"strategy",List.of("ACCEPT","AVOID","MITIGATE","TRANSFER")));
        result.setAssessmentType(counts(normalized,"analysis",List.of("QUALITATIVE","QUANTITATIVE")));
        result.setRiskAppetiteStatus(counts(normalized,"appetite",List.of("WITHIN","APPETITE_BREACHED","TOLERANCE_BREACHED")));
        result.setWithinRiskAppetite(normalized.stream().filter(r->"WITHIN".equals(r.get("appetite"))).count());
        result.setRiskToleranceBreached(normalized.stream().filter(r->"TOLERANCE_BREACHED".equals(r.get("appetite"))).count());
        for(String level:appendUnknown(LEVELS,normalized,"level")) {
            var group=normalized.stream().filter(r->level.equals(r.get("level"))).toList();
            result.getRatingWiseStatus().add(new RatingStatus(label(level),statusCount(group,"OPEN"),statusCount(group,"IN_PROGRESS"),
                statusCount(group,"CLOSED"),statusCount(group,"UNKNOWN")));
        }
        result.setRiskRatingByLocation(groups(normalized,"branch_id","branch_name"));
        result.setFunctionWiseRiskRating(groups(normalized,"business_function","department_name"));
        result.setRiskRatingByOwner(groups(normalized,"owner_id","owner_name"));
        result.setRiskRatingByCategory(groups(normalized,"category",null));
        var heatmap=heatmaps.build(risks,reviews,owners);
        // The existing heatmap uses MODERATE; the UI contract's severity key is MEDIUM.
        heatmap.getCells().forEach(c->{if("MODERATE".equals(c.getCellLevelKey()))c.setCellLevelKey("MEDIUM");
            if("MODERATE".equals(c.getDominantRiskLevelKey()))c.setDominantRiskLevelKey("MEDIUM");});
        heatmap.getRisks().forEach(r->{if("MODERATE".equals(r.getRiskLevelKey()))r.setRiskLevelKey("MEDIUM");});
        heatmap.getRiskLevelLegend().forEach(l->{if("MODERATE".equals(l.getKey())){l.setKey("MEDIUM");l.setDisplayLabel("Medium");}});
        result.setRiskHeatmap(heatmap);
        long offset=(long)page*size;int from=(int)Math.min(offset,register.size());int to=(int)Math.min(offset+size,register.size());
        result.setRiskRegister(register.subList(from,to));result.setRiskRegisterPage(new PageMetadata(page,size,register.size(),to<register.size()));
        return result;
    }

    private void maturity(ErmDashboardOverviewResponse result,List<Map<String,Object>> rows) {
        var groups=rows.stream().filter(r->text(r,"erm_maturity_id")!=null)
            .collect(Collectors.groupingBy(r->text(r,"company_id")+":"+text(r,"erm_maturity_id"),TreeMap::new,Collectors.toList()));
        for(var group:groups.values()) {
            Map<String,String> scores=new LinkedHashMap<>(),departments=new TreeMap<>();
            for(var row:group) {
                if(row.get("score_id")!=null)scores.put(text(row,"score_id"),text(row,"marks_achieved"));
                if(row.get("department_id")!=null)departments.put(text(row,"department_id"),text(row,"department_name"));
            }
            var first=group.get(0);var dto=new ErmMaturitySummaryGroup();
            dto.setErmMaturityId(text(first,"erm_maturity_id"));dto.setCompanyId(((Number)first.get("company_id")).longValue());
            var score=MaturityLevelResolver.sumMarksAchieved(new ArrayList<>(scores.values()));
            dto.setTotalWeightageScore(score);dto.setOverallMaturityLevel(ErmMaturityGroupingUtil.maturityLabel(score));
            dto.setDepartmentIds(departments.keySet().stream().map(Long::valueOf).toList());
            dto.setDisplayLabel(departments.isEmpty()?text(first,"company_name"):String.join(", ",departments.values()));
            (departments.isEmpty()?result.getErmMaturityCompanyWise():result.getErmMaturityFunctionWise()).add(dto);
        }
    }
    private static long statusCount(List<Map<String,Object>> rows,String status) {
        return rows.stream().filter(r->status.equals(r.get("lifecycle"))).count();
    }
    private static List<ErmGroupBreakdown> groups(List<Map<String,Object>> rows,String field,String name) {
        var grouped=rows.stream().collect(Collectors.groupingBy(r->r.get(field)==null?"NONE":r.get(field).toString(),TreeMap::new,Collectors.toList()));
        List<ErmGroupBreakdown> result=new ArrayList<>();
        grouped.forEach((id,items)->{var dto=new ErmGroupBreakdown();dto.setKey(id);dto.setTotal(items.size());
            String display=name==null?label(id):text(items.get(0),name);
            dto.setDisplayLabel(display==null?"Unassigned":display);dto.setBreakdown(counts(items,"level",LEVELS));result.add(dto);});
        return result;
    }
    private static List<String> appendUnknown(List<String> fixed,List<Map<String,Object>> rows,String field) {
        var keys=new LinkedHashSet<>(fixed);rows.stream().map(r->text(r,field)).filter(Objects::nonNull).sorted().forEach(keys::add);
        return new ArrayList<>(keys);
    }
    private static List<NamedCount> counts(List<Map<String,Object>> rows,String field,List<String> fixed) {
        var counts=rows.stream().collect(Collectors.groupingBy(r->text(r,field),Collectors.counting()));
        return appendUnknown(fixed,rows,field).stream().map(k->new NamedCount(k,counts.getOrDefault(k,0L),label(k))).toList();
    }
    static String appetite(String value) {
        String k=key(value);
        if(Set.of("WITHIN","WITHIN_APPETITE","WITHIN_RISK_APPETITE").contains(k))return "WITHIN";
        if(Set.of("APPETITE_BREACHED","APPETITE_BREACHED_BUT_WITHIN_TOLERANCE","RISK_APPETITE_BREACHED_BUT_WITHIN_TOLERANCE").contains(k))return "APPETITE_BREACHED";
        if(Set.of("TOLERANCE_BREACHED","RISK_TOLERANCE_BREACHED").contains(k))return "TOLERANCE_BREACHED";
        return "UNKNOWN";
    }
    private static String lifecycle(String value) {
        String k=key(value);return Set.of("OPEN","IN_PROGRESS","CLOSED").contains(k)?k:"UNKNOWN";
    }
    private static String level(String value) {
        var bucket=ErmRiskLevelBucket.forValue(value);return bucket==null?"UNKNOWN":bucket==ErmRiskLevelBucket.MODERATE?"MEDIUM":bucket.name();
    }
    private static String category(Object value) {
        if(value instanceof Number n) {int ordinal=n.intValue();return ordinal>=0 && ordinal<RiskCategory.values().length?RiskCategory.values()[ordinal].name():"UNKNOWN";}
        return key(value==null?null:value.toString());
    }
    private static String key(String value) {return value==null || value.isBlank()?"UNKNOWN":value.trim().toUpperCase(Locale.ROOT).replaceAll("[^A-Z0-9]+","_");}
    private static String label(String key) {
        return switch(key) {case "WITHIN"->"Within Risk Appetite";case "APPETITE_BREACHED"->"Appetite Breached but within Tolerance";
            case "TOLERANCE_BREACHED"->"Risk Tolerance Breached";default->ErmDashboardValueNormalizer.ratingLabel(key.replace('_',' '));};
    }
    private static String text(Map<String,Object> row,String field) {Object v=row.get(field);return v==null?null:v.toString();}
    private static String first(Map<String,Object> row,String... fields) {for(String f:fields){String s=text(row,f);if(s!=null&&!s.isBlank())return s;}return null;}
    private static BigDecimal number(Object v) {if(v==null)return null;try{return new BigDecimal(v.toString().trim());}catch(NumberFormatException e){return null;}}
}
