package ermorg.erm.evaluation;

import java.time.LocalDate;
import java.util.*;
import com.fasterxml.jackson.databind.*;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.springframework.jdbc.core.namedparam.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;
import ermorg.erm.dashboard.*;
import ermorg.erm.exception.ResourceNotFoundException;
import ermorg.erm.util.*;

@Service
@Transactional(rollbackFor = Exception.class)
public class PeriodEvaluationService {
    private final NamedParameterJdbcTemplate jdbc;
    private final ObjectMapper json;
    private final ChartScope.Resolver scopes;
    @org.springframework.beans.factory.annotation.Autowired
    private PeriodEvidenceClient evidenceClient;
    public PeriodEvaluationService(NamedParameterJdbcTemplate jdbc,ObjectMapper json,ChartScope.Resolver scopes) {
        this.jdbc=jdbc;this.json=json;this.scopes=scopes;
    }
    @io.swagger.v3.oas.annotations.media.Schema(name="StartPeriodEvaluation",example="{\"module\":\"REVIEW\",\"sourceId\":123,\"year\":2026,\"yearType\":\"FINANCIAL\",\"frequency\":\"QUARTERLY\",\"periodNumber\":2}")
    public record Start(EvaluationModule module,
        @io.swagger.v3.oas.annotations.media.Schema(description="Original category record ID, never a prior evaluation ID",minimum="1") long sourceId,
        @io.swagger.v3.oas.annotations.media.Schema(description="Starting year; FINANCIAL 2026 means April 2026-March 2027",minimum="1900",maximum="2200") int year,
        ChartPeriod.YearType yearType,ChartPeriod.Frequency frequency,
        @io.swagger.v3.oas.annotations.media.Schema(description="1-12 for Monthly, 1-4 for Quarterly",minimum="1",maximum="12") int periodNumber) {}
    @io.swagger.v3.oas.annotations.media.Schema(name="EditPeriodEvaluation",example="{\"revision\":0,\"values\":{\"review_type\":\"Quantitative\",\"annual_loss_expectancy\":50,\"currency\":\"INR\",\"value_unit\":\"CRORES\"}}")
    public record Edit(@io.swagger.v3.oas.annotations.media.Schema(description="Latest revision from GET; stale writes return 409",minimum="0") long revision,
        @io.swagger.v3.oas.annotations.media.Schema(description="Allowed snake_case scalar fields from editableFields; omitted keys retained, null clears") Map<String,JsonNode> values) {}
    @io.swagger.v3.oas.annotations.media.Schema(name="CompletePeriodEvaluation",example="{\"revision\":1,\"effectiveDate\":\"2026-08-15\"}")
    public record Complete(long revision,@io.swagger.v3.oas.annotations.media.Schema(description="Evaluation date within the selected period, ISO YYYY-MM-DD") LocalDate effectiveDate) {}

    private ChartScope scope() throws ResourceNotFoundException {
        if(CompanyContext.getCompany()==null||CompanyContext.getCompany().getId()==null)
            throw new IllegalArgumentException("Company context is required.");
        return scopes.resolve(CompanyContext.getCompany().getId(),null,null);
    }
    private MapSqlParameterSource params(ChartScope scope) {
        return new MapSqlParameterSource("org",scope.organizationId()).addValue("company",scope.companyId());
    }
    private static Long number(Object v) {return v==null?null:((Number)v).longValue();}
    private void access(ChartScope scope,Map<String,Object> row,boolean write) {
        Long creator=number(row.get("created_by_id")),original=number(row.get("source_creator_id"));
        boolean permitted=scope.creatorId()==null||Objects.equals(scope.creatorId(),creator)||Objects.equals(scope.creatorId(),original);
        if(scope.restricted()) permitted=
            (scope.assignedBranchId()!=null&&Objects.equals(scope.assignedBranchId(),number(row.get("branch_id"))))
            ||(scope.assignedDepartmentId()!=null&&scope.assignedDepartmentId().toString().equals(Objects.toString(row.get("department_id"),null)));
        // An assigned approver can read the immutable evaluation they are asked to review.
        if(!permitted&&!write&&row.get("id")!=null) {
            Long count=jdbc.queryForObject("SELECT COUNT(*) FROM approval WHERE organization_id=:org AND company_id=:company AND deleted=false AND source_module='periodEvaluation' AND source_record_id=:id AND approver_id=:user",
                params(scope).addValue("id",row.get("id").toString()).addValue("user",UserContext.getUser().getId()),Long.class);
            permitted=count!=null&&count>0;
        }
        if(!permitted) throw new ResponseStatusException(HttpStatus.NOT_FOUND,"Evaluation outside user scope.");
    }
    private Map<String,Object> row(long id,boolean write) throws ResourceNotFoundException {
        ChartScope scope=scope();
        var rows=jdbc.queryForList("SELECT * FROM period_evaluation WHERE id=:id AND organization_id=:org AND company_id=:company"+(write?" FOR UPDATE":""),params(scope).addValue("id",id));
        if(rows.isEmpty())throw new ResponseStatusException(HttpStatus.NOT_FOUND,"Evaluation not found.");
        access(scope,rows.get(0),write);return rows.get(0);
    }
    @Transactional(readOnly=true)
    public Map<String,Object> get(long id) throws ResourceNotFoundException {return response(row(id,false));}
    private Map<String,Object> response(Map<String,Object> row) {
        var response=new LinkedHashMap<>(row);
        response.put("source_snapshot",parse(row.get("source_snapshot")));
        response.put("evaluation_values",parse(row.get("evaluation_values")));
        response.put("editableFields",EvaluationModule.valueOf(row.get("module").toString()).fields);
        response.put("evidence",jdbc.queryForList("SELECT document_id,inherited,description FROM period_evaluation_evidence WHERE evaluation_id=:id ORDER BY document_id",new MapSqlParameterSource("id",row.get("id"))));
        return response;
    }
    public Map<String,Object> start(Start request) throws ResourceNotFoundException {
        if(request==null||request.module()==null||request.sourceId()<=0)throw new IllegalArgumentException("module and positive sourceId required.");
        var period=EvaluationPeriod.of(request.year(),request.yearType(),request.frequency(),request.periodNumber());
        var module=request.module();var scope=scope();
        var p=params(scope).addValue("source",request.sourceId()).addValue("module",module.name());
        // Lock stable source row: serializes concurrent starts before the unique constraint.
        var sources=jdbc.queryForList("SELECT * FROM "+module.table+" WHERE id=:source AND organization_id=:org AND company_id=:company AND "+module.deletedColumn()+"=false FOR UPDATE",p);
        if(sources.isEmpty())throw new ResponseStatusException(HttpStatus.NOT_FOUND,"Source not found.");
        var source=sources.get(0);var identity=new HashMap<String,Object>();
        if(module==EvaluationModule.KRI||module==EvaluationModule.KPI) {
            if(source.get("reporting_year")==null||source.get("reporting_year_type")==null)
                throw new IllegalArgumentException("Tag the annual KRI/KPI entry with reportingYear and reportingYearType before starting period history.");
            if(((Number)source.get("reporting_year")).intValue()!=request.year()||!request.yearType().name().equals(source.get("reporting_year_type")))
                throw new IllegalArgumentException("Create a separate annual KRI/KPI entry for a different reporting year.");
            String frequency=Objects.toString(source.get(module==EvaluationModule.KRI?"kri_evaluation_frequency":"kpi_evaluation_frequency"),"").trim();
            if(!request.frequency().name().equalsIgnoreCase(frequency))throw new IllegalArgumentException("Frequency must match the annual entry.");
        }
        identity.put("created_by_id",source.get("created_by_id"));
        identity.put("department_id",source.get("business_function"));
        if(module!=EvaluationModule.KPI) {
            var risks=jdbc.queryForList("SELECT id,branch_id,business_function,created_by_id FROM risk WHERE id=:risk AND organization_id=:org AND company_id=:company AND deleted=false",p.addValue("risk",source.get("risk_id")));
            if(risks.isEmpty())throw new ResponseStatusException(HttpStatus.NOT_FOUND,"Source risk not found.");
            var risk=risks.get(0);identity.put("branch_id",risk.get("branch_id"));
            if(identity.get("department_id")==null)identity.put("department_id",risk.get("business_function"));
        } else {
            var owners=jdbc.queryForList("SELECT branch_id FROM user WHERE id=:owner AND organization_id=:org AND company_id=:company AND deleted=false",p.addValue("owner",source.get("owner_id")));
            identity.put("branch_id",owners.isEmpty()?null:owners.get(0).get("branch_id"));
        }
        access(scope,identity,true);
        var existing=jdbc.queryForList("SELECT * FROM period_evaluation WHERE organization_id=:org AND company_id=:company AND module=:module AND source_id=:source ORDER BY period_start DESC,id DESC LIMIT 1 FOR UPDATE",p);
        Map<String,Object> previous=existing.isEmpty()?null:existing.get(0);
        if(previous!=null) {
            if(!Objects.equals(number(previous.get("risk_id")),number(source.get("risk_id"))))
                conflict("Source risk changed after period history started; use a separate source record.");
            if(!request.yearType().name().equals(previous.get("year_type")))conflict("Year convention cannot change within an evaluation history.");
            if(!"COMPLETED".equals(previous.get("state")))conflict("Complete the current draft before starting another period.");
            LocalDate previousEnd=LocalDate.parse(previous.get("period_end").toString());
            if(period.start().isBefore(previousEnd))conflict("Period already exists or overlaps existing history.");
            if(request.year()==((Number)previous.get("reporting_year")).intValue()&&!request.frequency().name().equals(previous.get("frequency")))
                conflict("Frequency must remain the same within a reporting year.");
        }
        ObjectNode snapshot=json.createObjectNode();
        if(previous==null) {
            snapshot.set("record",json.valueToTree(source));
            if(module.subRiskColumn!=null) snapshot.set("subRisks",json.valueToTree(jdbc.queryForList("SELECT id,sub_risk,risk_sub_category,risk_id FROM sub_risk WHERE "+module.subRiskColumn+"=:source AND organization_id=:org AND company_id=:company AND deleted=false",p)));
            if(module==EvaluationModule.CONTROL) {
                snapshot.set("subControls",json.valueToTree(jdbc.queryForList("SELECT id,sub_control_title,risk_appetite_status,risk_acceptance_level FROM risk_sub_control WHERE risk_control_id=:source AND organization_id=:org AND company_id=:company AND deleted=false",p)));
                snapshot.set("tags",json.valueToTree(jdbc.queryForList("SELECT tags FROM control_tags WHERE control_id=:source",p)));
            }
        } else {
            snapshot=(ObjectNode)parse(previous.get("source_snapshot")).deepCopy();
            snapshot.set("previousValues",parse(previous.get("evaluation_values")));
        }
        String approvalModule=previous==null?module.approvalModule:"periodevaluation";
        snapshot.set("previousApprovals",json.valueToTree(jdbc.queryForList("SELECT id,status,approver_id,submitter_id,comment,closed_at FROM approval WHERE organization_id=:org AND company_id=:company AND deleted=false AND REGEXP_REPLACE(LOWER(source_module),'[^a-z0-9]','')=:approvalModule AND source_record_id=:approvalSource",params(scope).addValue("approvalModule",approvalModule).addValue("approvalSource",previous==null?Long.toString(request.sourceId()):previous.get("id").toString()))));
        p.addValue("risk",source.get("risk_id")).addValue("branch",identity.get("branch_id")).addValue("department",identity.get("department_id"))
            .addValue("creator",source.get("created_by_id")).addValue("previous",previous==null?null:previous.get("id"))
            .addValue("year",request.year()).addValue("yearType",request.yearType().name()).addValue("frequency",request.frequency().name())
            .addValue("number",request.periodNumber()).addValue("start",period.start().toString()).addValue("end",period.end().toString())
            .addValue("snapshot",snapshot.toString()).addValue("user",UserContext.getUser().getId());
        var key=new org.springframework.jdbc.support.GeneratedKeyHolder();
        jdbc.update("INSERT INTO period_evaluation(organization_id,company_id,module,source_id,risk_id,branch_id,department_id,source_creator_id,previous_evaluation_id,reporting_year,year_type,frequency,period_number,period_start,period_end,source_snapshot,evaluation_values,created_by_id) VALUES(:org,:company,:module,:source,:risk,:branch,:department,:creator,:previous,:year,:yearType,:frequency,:number,:start,:end,:snapshot,'{}',:user)",p,key,new String[]{"id"});
        long id=Objects.requireNonNull(key.getKey()).longValue();
        if(previous!=null) {
            var documents=jdbc.queryForList("SELECT document_id,description FROM period_evaluation_evidence WHERE evaluation_id=:previous",new MapSqlParameterSource("previous",previous.get("id")));
            var bindings=documents.stream().map(document->new MapSqlParameterSource("id",id).addValue("document",document.get("document_id")).addValue("description",document.get("description"))).toArray(MapSqlParameterSource[]::new);
            if(bindings.length>0)jdbc.batchUpdate("INSERT INTO period_evaluation_evidence(evaluation_id,document_id,inherited,description) VALUES(:id,:document,true,:description)",bindings);
        }
        else if(module==EvaluationModule.TREATMENT&&source.get("supporting_evidence_document")!=null)
            jdbc.update("INSERT INTO period_evaluation_evidence(evaluation_id,document_id,inherited,description) VALUES(:id,:document,true,:description)",new MapSqlParameterSource("id",id).addValue("document",source.get("supporting_evidence_document").toString()).addValue("description",source.get("supporting_evidence_artifacts_description")));
        return get(id);
    }
    public Map<String,Object> edit(long id,Edit request) throws ResourceNotFoundException {
        var row=row(id,true);draft(row,request.revision());
        if(request.values()==null||request.values().isEmpty())throw new IllegalArgumentException("Evaluation values required.");
        ObjectNode values=(ObjectNode)parse(row.get("evaluation_values"));
        EvaluationModule module=EvaluationModule.valueOf(row.get("module").toString());
        request.values().forEach((field,value)->{
            if(!module.fields.contains(field))throw new IllegalArgumentException("Field is not editable: "+field);
            validateValue(field,value);values.set(field,value==null?json.nullNode():value);
        });
        jdbc.update("UPDATE period_evaluation SET evaluation_values=:values,revision=revision+1,updated_at=CURRENT_TIMESTAMP(6) WHERE id=:id",new MapSqlParameterSource("id",id).addValue("values",values.toString()));
        return get(id);
    }
    static void validateValue(String field,JsonNode value) {
        if(value==null||value.isNull())return;
        if(value.isContainerNode()||value.isBoolean()||value.asText().length()>4000)throw new IllegalArgumentException("Scalar text/number required: "+field);
        if(field.equals("currency")&&!value.asText().matches("[A-Z]{3}"))throw new IllegalArgumentException("Currency must be a three-letter uppercase code.");
        if(field.equals("value_unit"))ermorg.erm.constant.RiskValueUnit.valueOf(value.asText());
        if(field.equals("risk_acceptance_level"))ermorg.erm.constant.RiskAcceptanceLevel.valueOf(value.asText());
        if(Set.of("actual_value","assets_value","per_of_potential_loss","yearly_frequency","annual_loss_expectancy").contains(field)) {
            if(!value.isNumber()||value.decimalValue().abs().compareTo(new java.math.BigDecimal("1000000000000000"))>0)
                throw new IllegalArgumentException("Finite numeric value required: "+field);
            if(!field.equals("actual_value")&&value.decimalValue().signum()<0)throw new IllegalArgumentException("Negative value not allowed: "+field);
            if(field.equals("per_of_potential_loss")&&value.decimalValue().compareTo(java.math.BigDecimal.valueOf(100))>0)throw new IllegalArgumentException("Loss percentage must be <=100.");
            if(field.equals("yearly_frequency")&&value.decimalValue().stripTrailingZeros().scale()>0)throw new IllegalArgumentException("Yearly frequency must be an integer.");
        }
    }
    public Map<String,Object> complete(long id,Complete request) throws ResourceNotFoundException {
        var row=row(id,true);draft(row,request.revision());
        EvaluationPeriod.of(((Number)row.get("reporting_year")).intValue(),ChartPeriod.YearType.valueOf(row.get("year_type").toString()),ChartPeriod.Frequency.valueOf(row.get("frequency").toString()),((Number)row.get("period_number")).intValue()).validateDate(request.effectiveDate());
        var values=parse(row.get("evaluation_values"));var module=EvaluationModule.valueOf(row.get("module").toString());
        String required=switch(module){case CONTROL->"control_title";case TREATMENT->"risk_treatment_status";case REVIEW->"review_type";default->"actual_value";};
        if(!values.hasNonNull(required)||values.get(required).asText().isBlank())throw new IllegalArgumentException("Complete "+required+" before finalizing.");
        if(module==EvaluationModule.REVIEW) {
            String type=values.get("review_type").asText();
            if(!type.equalsIgnoreCase("Qualitative")&&!type.equalsIgnoreCase("Quantitative"))throw new IllegalArgumentException("Review type must be Qualitative or Quantitative.");
            String result=type.equalsIgnoreCase("Quantitative")?"annual_loss_expectancy":"residual_risk_rating";
            if(!values.hasNonNull(result)||values.get(result).asText().isBlank())throw new IllegalArgumentException("Complete "+result+" before finalizing.");
            if(type.equalsIgnoreCase("Quantitative")&&(!values.hasNonNull("currency")||!values.hasNonNull("value_unit")))throw new IllegalArgumentException("Quantitative review requires currency and value_unit.");
        }
        jdbc.update("UPDATE period_evaluation SET state='COMPLETED',effective_date=:date,completed_at=CURRENT_TIMESTAMP(6),updated_at=CURRENT_TIMESTAMP(6),revision=revision+1 WHERE id=:id",new MapSqlParameterSource("id",id).addValue("date",request.effectiveDate().toString()));
        return get(id);
    }
    @Transactional(readOnly=true)
    public List<Map<String,Object>> history(EvaluationModule module,long sourceId,int page,int size) throws ResourceNotFoundException {
        if(module==null||sourceId<=0||page<0||size<1||size>100)throw new IllegalArgumentException("Invalid module/source/page/size.");
        var scope=scope();var p=params(scope).addValue("module",module.name()).addValue("source",sourceId).addValue("offset",(long)page*size).addValue("size",size);
        String accessSql="";
        if(scope.creatorId()!=null||scope.restricted()) {
            var allowed=new ArrayList<String>();
            if(scope.creatorId()!=null){allowed.add("(e.source_creator_id=:reader OR e.created_by_id=:reader)");p.addValue("reader",scope.creatorId());}
            if(scope.restricted()) {
                if(scope.assignedBranchId()!=null){allowed.add("e.branch_id=:assignedBranch");p.addValue("assignedBranch",scope.assignedBranchId());}
                if(scope.assignedDepartmentId()!=null){allowed.add("e.department_id=:assignedDepartment");p.addValue("assignedDepartment",scope.assignedDepartmentId().toString());}
            }
            allowed.add("EXISTS(SELECT 1 FROM approval a WHERE a.organization_id=e.organization_id AND a.company_id=e.company_id AND a.deleted=false AND a.source_module='periodEvaluation' AND a.source_record_id=CAST(e.id AS CHAR) AND a.approver_id=:user)");
            p.addValue("user",UserContext.getUser().getId());accessSql=" AND ("+String.join(" OR ",allowed)+")";
        }
        var rows=jdbc.queryForList("SELECT e.id,e.module,e.source_id,e.risk_id,e.branch_id,e.department_id,e.source_creator_id,e.created_by_id,e.previous_evaluation_id,e.reporting_year,e.year_type,e.frequency,e.period_number,e.period_start,e.period_end,e.state,e.revision,e.created_at,e.updated_at,e.completed_at,e.effective_date FROM period_evaluation e WHERE e.organization_id=:org AND e.company_id=:company AND e.module=:module AND e.source_id=:source"+accessSql+" ORDER BY e.period_start DESC,e.id DESC LIMIT :size OFFSET :offset",p);
        return rows.stream().map(item->{var summary=new LinkedHashMap<>(item);summary.remove("source_snapshot");summary.remove("evaluation_values");return (Map<String,Object>)summary;}).toList();
    }
    public void assertApprovalSource(long id) throws ResourceNotFoundException {
        var evaluation=row(id,false);
        if(!"COMPLETED".equals(evaluation.get("state")))throw new IllegalArgumentException("Complete the evaluation before requesting approval.");
    }
    static void draft(Map<String,Object> row,long revision) {
        if(!"DRAFT".equals(row.get("state")))conflict("Completed evaluations are immutable.");
        if(((Number)row.get("revision")).longValue()!=revision)conflict("Evaluation changed; reload before saving.");
    }
    public Map<String,Object> uploadEvidence(long id,long revision,org.springframework.web.multipart.MultipartFile file,String description) throws ResourceNotFoundException {
        var evaluation=row(id,true);draft(evaluation,revision);
        if(description!=null&&description.length()>2000)throw new IllegalArgumentException("Description is too long.");
        String document=evidenceClient.upload(file);
        jdbc.update("INSERT INTO period_evaluation_evidence(evaluation_id,document_id,inherited,description) VALUES(:id,:document,false,:description)",new MapSqlParameterSource("id",id).addValue("document",document).addValue("description",description));
        jdbc.update("UPDATE period_evaluation SET revision=revision+1,updated_at=CURRENT_TIMESTAMP(6) WHERE id=:id",new MapSqlParameterSource("id",id));
        return get(id);
    }
    @Transactional(readOnly=true)
    public Map<?,?> downloadEvidence(long id,String document) throws ResourceNotFoundException {
        row(id,false);String uuid=UUID.fromString(document).toString();
        Long count=jdbc.queryForObject("SELECT COUNT(*) FROM period_evaluation_evidence WHERE evaluation_id=:id AND document_id=:document",new MapSqlParameterSource("id",id).addValue("document",uuid),Long.class);
        if(count==null||count==0)throw new ResponseStatusException(HttpStatus.NOT_FOUND,"Evidence not associated with evaluation.");
        return evidenceClient.download(uuid);
    }
    @Transactional(readOnly=true)
    public boolean retainsDocument(String document) {
        Long count=jdbc.queryForObject("SELECT COUNT(*) FROM period_evaluation_evidence WHERE document_id=:document",new MapSqlParameterSource("document",document),Long.class);
        return count!=null&&count>0;
    }
    public void guardTreatmentEvidenceDeletion(long treatmentId,String document) {
        if(OrganizationContext.getOrganization()==null||CompanyContext.getCompany()==null)
            throw new IllegalArgumentException("Organization and company context required for evidence deletion.");
        String uuid=UUID.fromString(document).toString();
        var p=new MapSqlParameterSource("id",treatmentId).addValue("org",OrganizationContext.getOrganization().getId())
            .addValue("company",CompanyContext.getCompany().getId());
        // Same source-row lock as start(): deletion cannot race the first history snapshot.
        var rows=jdbc.queryForList("SELECT supporting_evidence_document FROM risk_risponse_treatment WHERE id=:id AND organization_id=:org AND company_id=:company AND deleted=false FOR UPDATE",p);
        if(rows.isEmpty()||!uuid.equalsIgnoreCase(Objects.toString(rows.get(0).get("supporting_evidence_document"),"")))
            throw new IllegalArgumentException("Evidence is not associated with the selected treatment.");
        if(retainsDocument(uuid))throw new IllegalArgumentException("Evidence is retained in evaluation history and cannot be deleted.");
    }
    static void conflict(String message){throw new ResponseStatusException(HttpStatus.CONFLICT,message);}
    private JsonNode parse(Object value) {
        try{return json.readTree(value.toString());}catch(Exception ex){throw new IllegalStateException("Invalid stored evaluation JSON",ex);}
    }
}
