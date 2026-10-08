package ermorg.erm.dashboard;

import java.math.BigDecimal;
import java.util.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ermorg.erm.exception.ResourceNotFoundException;

@Service
@Transactional(readOnly = true)
public class DashboardChartService {
    private final DashboardChartRepository repository;
    private final ChartScope.Resolver scopes;
    public DashboardChartService(DashboardChartRepository repository, ChartScope.Resolver scopes) {
        this.repository = repository; this.scopes = scopes;
    }
    public record PerformanceSeries(long recordId, String indicator, String currency, String valueUnit,
        String measurementUnit, BigDecimal target, List<BigDecimal> actuals, List<String> invalidFields, String yearBasis) {}
    public record PerformanceResponse(ChartPeriod period, ChartPeriod.Indicator indicator, ChartPeriod.Frequency frequency,
        String yearBasis, List<String> labels, int page, int size, boolean hasNext, List<PerformanceSeries> series) {}
    public record ExposureTotal(String currency, String valueUnit, BigDecimal inherent, BigDecimal residual,
        long inherentRiskCount, long residualRiskCount, long missingInherentCount, long missingResidualCount) {}
    public record ExposureResponse(ChartPeriod period, String selection, List<ExposureTotal> totals) {}
    public record TrendResponse(ChartPeriod period, String measure, List<String> labels, List<Long> risks, List<Long> assessments) {}
    public record ScatterResponse(ChartPeriod period, int page, int size, boolean hasNext, List<Map<String,Object>> points) {}

    public PerformanceResponse performance(ChartPeriod period, ChartPeriod.Indicator indicator, ChartPeriod.Frequency frequency,
            Long company, Long branch, Long department, int page, int size) throws ResourceNotFoundException {
        validatePage(page,size);
        ChartScope scope=scopes.resolve(company,branch,department);
        List<Map<String,Object>> rows = repository.performance(scope,period,indicator,frequency,page,size);
        rows=rows.stream().map(row->(Map<String,Object>)new LinkedHashMap<String,Object>(row)).toList();
        var visible=rows.subList(0,Math.min(size,rows.size()));
        var byId=new HashMap<Long,Map<String,Object>>();
        for(var row:visible)byId.put(((Number)row.get("id")).longValue(),row);
        var histories=repository.performanceHistory(scope,period,indicator,frequency,new ArrayList<>(byId.keySet()));
        var initialized=new HashSet<Long>();
        var parser=new com.fasterxml.jackson.databind.ObjectMapper();
        for(var history:histories) {
            Long id=((Number)history.get("source_id")).longValue();var row=byId.get(id);
            try {
                var values=parser.readTree(history.get("evaluation_values").toString());
                if(initialized.add(id)) {
                    var original=parser.readTree(history.get("source_snapshot").toString()).path("record");
                    for(String field:List.of("target_value","currency","value_unit"))row.put(field,original.hasNonNull(field)?original.get(field).asText():null);
                    for(String field:period.columns(frequency))row.put(field,original.hasNonNull(field)?original.get(field).asText():null);
                }
                String column=frequency==ChartPeriod.Frequency.QUARTERLY?"q"+history.get("period_number"):
                    java.time.LocalDate.parse(history.get("period_start").toString()).getMonth().name().toLowerCase(Locale.ROOT);
                row.put(column,values.hasNonNull("actual_value")?values.get("actual_value").asText():null);
                row.put("period_history",true);
            }catch(com.fasterxml.jackson.core.JsonProcessingException ex){throw new IllegalStateException("Invalid stored evaluation JSON",ex);}
        }
        List<PerformanceSeries> result = new ArrayList<>();
        for (Map<String,Object> row : rows.subList(0, Math.min(size,rows.size()))) {
            List<String> invalid = new ArrayList<>();
            BigDecimal target = numeric(row.get("target_value"),"targetValue",invalid);
            List<BigDecimal> actuals = new ArrayList<>();
            for(String column : period.columns(frequency)) actuals.add(numeric(row.get(column),column,invalid));
            result.add(new PerformanceSeries(((Number)row.get("id")).longValue(),text(row.get("indicator_name")),
                text(row.get("currency")),text(row.get("value_unit")),text(row.get("measurement_unit")),target,actuals,invalid,
                Boolean.TRUE.equals(row.get("period_history"))?"PERIOD_HISTORY":row.get("reporting_year")==null ? "ENTRY_CREATED_AT_FALLBACK" : "EXPLICIT_REPORTING_YEAR"));
        }
        return new PerformanceResponse(period,indicator,frequency,"SEE_SERIES_YEAR_BASIS",period.labels(frequency),page,size,rows.size()>size,result);
    }
    public ExposureResponse exposure(ChartPeriod period,Long company,Long branch,Long department) throws ResourceNotFoundException {
        ChartScope scope = scopes.resolve(company,branch,department);
        Map<List<String>,Map<String,Object>> inherent = keyed(repository.exposure(scope,period,false));
        Map<List<String>,Map<String,Object>> residual = keyed(repository.exposure(scope,period,true));
        Set<List<String>> keys = new LinkedHashSet<>(inherent.keySet()); keys.addAll(residual.keySet());
        List<ExposureTotal> totals = new ArrayList<>();
        for(List<String> key: keys) {
            Map<String,Object> i=inherent.get(key), r=residual.get(key);
            totals.add(new ExposureTotal(key.get(0),key.get(1),total(i),total(r),count(i,"risk_count"),count(r,"risk_count"),
                count(i,"risk_count")-count(i,"valued_count"),count(r,"risk_count")-count(r,"valued_count")));
        }
        return new ExposureResponse(period,"INHERENT_BASELINE_AS_OF_PERIOD_END_RESIDUAL_LATEST_IN_PERIOD",totals);
    }
    public TrendResponse trend(ChartPeriod period,Long company,Long branch,Long department) throws ResourceNotFoundException {
        ChartScope scope=scopes.resolve(company,branch,department);
        List<String> labels=period.labels(ChartPeriod.Frequency.MONTHLY);
        return new TrendResponse(period,"CREATED_RECORDS",labels,buckets(labels,repository.trend(scope,period,false)),
            buckets(labels,repository.trend(scope,period,true)));
    }
    public ScatterResponse scatter(ChartPeriod period,Long company,Long branch,Long department,int page,int size) throws ResourceNotFoundException {
        validatePage(page,size);
        List<Map<String,Object>> rows=repository.scatter(scopes.resolve(company,branch,department),period,page,size);
        return new ScatterResponse(period,page,size,rows.size()>size,rows.subList(0,Math.min(size,rows.size())));
    }
    static void validatePage(int page,int size) {
        if(page<0||size<1||size>100) throw new IllegalArgumentException("page must be >=0; size must be 1-100.");
    }
    static BigDecimal numeric(Object value,String field,List<String> invalid) {
        if(value==null||value.toString().isBlank()) return null;
        try { return new BigDecimal(value.toString().trim()); }
        catch(NumberFormatException ex) { invalid.add(field); return null; }
    }
    private static String text(Object value) { return value==null?null:value.toString(); }
    private static BigDecimal total(Map<String,Object> row) {
        return row==null||row.get("total")==null?null:new BigDecimal(row.get("total").toString());
    }
    private static long count(Map<String,Object> row,String key) {return row==null?0:((Number)row.get(key)).longValue();}
    private static Map<List<String>,Map<String,Object>> keyed(List<Map<String,Object>> rows) {
        Map<List<String>,Map<String,Object>> result=new LinkedHashMap<>();
        for(var row:rows) result.put(Arrays.asList(text(row.get("currency")),text(row.get("value_unit"))),row);
        return result;
    }
    private static List<Long> buckets(List<String> labels,List<Map<String,Object>> rows) {
        Map<String,Long> counts=new HashMap<>();
        for(var row:rows) counts.put(row.get("period").toString(),((Number)row.get("total")).longValue());
        return labels.stream().map(label->counts.getOrDefault(label,0L)).toList();
    }
}
