package ermorg.erm.controller;

import org.springframework.web.bind.annotation.*;
import org.springframework.http.*;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.web.multipart.MultipartFile;
import ermorg.erm.evaluation.*;
import ermorg.erm.exception.ResourceNotFoundException;
import ermorg.erm.response.GeneralResponse;
import ermorg.erm.dto.ResponseStatus;

@RestController
@RequestMapping("/period-evaluations")
@io.swagger.v3.oas.annotations.tags.Tag(name="Period evaluations",description="Separate period history. Legacy category rows remain unchanged; completed evaluations are immutable.")
@io.swagger.v3.oas.annotations.responses.ApiResponses({
    @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode="400",description="Invalid request or missing trusted user/company context"),
    @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode="404",description="Record unavailable within caller scope"),
    @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode="409",description="Stale revision, overlapping period, or completed evaluation")})
public class PeriodEvaluationController {
    private final PeriodEvaluationService service;
    public PeriodEvaluationController(PeriodEvaluationService service){this.service=service;}
    @PostMapping("/start")
    @io.swagger.v3.oas.annotations.Operation(summary="Start a period draft",description="sourceId is the original category ID. MONTHLY uses periods 1-12; QUARTERLY uses 1-4. FINANCIAL year starts in April. Starting a draft sends no approval notification.")
    public GeneralResponse<?> start(@RequestBody PeriodEvaluationService.Start request) throws ResourceNotFoundException {return ok(service.start(request));}
    @GetMapping("/{id}")
    @io.swagger.v3.oas.annotations.Operation(summary="Read evaluation and frozen source snapshot",description="Returns revision, state, source_snapshot, evaluation_values, editableFields and evidence manifest inside data.")
    public GeneralResponse<?> get(@PathVariable long id) throws ResourceNotFoundException{return ok(service.get(id));}
    @GetMapping
    @io.swagger.v3.oas.annotations.Operation(summary="List accessible period history",description="Newest first. Zero-based page; size 1-100. Summaries omit large snapshot/value payloads.")
    public GeneralResponse<?> history(@RequestParam EvaluationModule module,@RequestParam long sourceId,
            @RequestParam(defaultValue="0") int page,@RequestParam(defaultValue="20") int size) throws ResourceNotFoundException {return ok(service.history(module,sourceId,page,size));}
    @PatchMapping("/{id}")
    @io.swagger.v3.oas.annotations.Operation(summary="Merge draft values",description="Send latest revision and allowed snake_case fields in values. Omitted values are retained; explicit null clears a draft field. Use editableFields from GET. Completed records cannot be changed.")
    public GeneralResponse<?> edit(@PathVariable long id,@RequestBody PeriodEvaluationService.Edit request) throws ResourceNotFoundException {return ok(service.edit(id,request));}
    @PostMapping("/{id}/complete")
    @io.swagger.v3.oas.annotations.Operation(summary="Complete and lock an evaluation",description="effectiveDate must lie within the selected period. COMPLETED is not APPROVED. Approval is requested separately via /approvals with sourceModule periodEvaluation.")
    public GeneralResponse<?> complete(@PathVariable long id,@RequestBody PeriodEvaluationService.Complete request) throws ResourceNotFoundException {return ok(service.complete(id,request));}
    @PostMapping(value="/{id}/evidence",consumes=MediaType.MULTIPART_FORM_DATA_VALUE)
    @io.swagger.v3.oas.annotations.Operation(summary="Append evidence to draft",description="Multipart file, latest revision and optional description. Maximum 10MB. Returns refreshed evaluation/revision; no automatic retry after ambiguous network failure.")
    public GeneralResponse<?> upload(@PathVariable long id,@RequestParam long revision,@RequestParam MultipartFile file,@RequestParam(required=false) String description) throws ResourceNotFoundException {return ok(service.uploadEvidence(id,revision,file,description));}
    @GetMapping("/{id}/evidence/{documentId}")
    @io.swagger.v3.oas.annotations.Operation(summary="Fetch associated evidence metadata",description="Returns storage metadata in the JSON data wrapper, not raw file bytes. Association and caller access are checked first.")
    public GeneralResponse<?> download(@PathVariable long id,@PathVariable String documentId) throws ResourceNotFoundException {return ok(service.downloadEvidence(id,documentId));}
    @ExceptionHandler(ResponseStatusException.class)
    public ResponseEntity<GeneralResponse<Object>> status(ResponseStatusException ex){return error(ex.getStatusCode(),ex.getReason());}
    @ExceptionHandler({IllegalArgumentException.class,org.springframework.http.converter.HttpMessageNotReadableException.class,
        org.springframework.web.method.annotation.MethodArgumentTypeMismatchException.class,org.springframework.web.bind.MissingServletRequestParameterException.class})
    public ResponseEntity<GeneralResponse<Object>> invalid(Exception ex){return error(HttpStatus.BAD_REQUEST,"Invalid evaluation request: "+ex.getMessage());}
    @ExceptionHandler(org.springframework.dao.DuplicateKeyException.class)
    public ResponseEntity<GeneralResponse<Object>> duplicate(Exception ex){return error(HttpStatus.CONFLICT,"Evaluation already exists for this period.");}
    private static ResponseEntity<GeneralResponse<Object>> error(HttpStatusCode status,String message){
        var result=new GeneralResponse<Object>();result.setStatus(ResponseStatus.FAILED);result.setMessage(message);return ResponseEntity.status(status).body(result);
    }
    private static <T> GeneralResponse<T> ok(T data){var result=new GeneralResponse<T>();result.setStatus(ResponseStatus.SUCCESS);result.setData(data);return result;}
}
