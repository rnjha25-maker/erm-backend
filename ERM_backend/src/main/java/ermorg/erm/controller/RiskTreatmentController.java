package ermorg.erm.controller;

import java.io.IOException;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import ermorg.erm.dto.ResponseStatus;
import ermorg.erm.dto.response.CustomResponse;
import ermorg.erm.dto.response.RiskResponseTreatmentResponse;
import ermorg.erm.dto.riskDTO.RiskResponseTreatmentDto;
import ermorg.erm.exception.ResourceNotFoundException;
import ermorg.erm.response.GeneralResponse;
import ermorg.erm.service.IRiskTreatmentService;

@RestController
@RequestMapping("/risk-treatment")
public class RiskTreatmentController {
	
	@Autowired
	private IRiskTreatmentService riskTreatmentService;
	
	@PostMapping("/save")
	public GeneralResponse<RiskResponseTreatmentResponse> save(@RequestBody RiskResponseTreatmentDto riskResponseTreatmentDto) throws ResourceNotFoundException {
		GeneralResponse<RiskResponseTreatmentResponse> response = new GeneralResponse<>();
		RiskResponseTreatmentResponse savedData = riskTreatmentService.save(riskResponseTreatmentDto);
		
		response.setData(savedData);
		response.setStatus(ResponseStatus.SUCCESS);
		response.setMessage("Saved.");
		return response;
	}
	
	@GetMapping("/{id}")
	public GeneralResponse<RiskResponseTreatmentResponse> getRiskTreatment(@PathVariable("id") Long id) throws ResourceNotFoundException {
		GeneralResponse<RiskResponseTreatmentResponse> response = new GeneralResponse<>();
		RiskResponseTreatmentResponse data = riskTreatmentService.getRiskTreatment(id);
		
		response.setData(data);
		response.setStatus(ResponseStatus.SUCCESS);
		response.setMessage("Saved.");
		return response;
	}
	
	@GetMapping("get-view/{id}")
	public GeneralResponse<List<CustomResponse>> getRiskTreatmentView(@PathVariable("id") Long id) throws ResourceNotFoundException {
		GeneralResponse<List<CustomResponse>> response = new GeneralResponse<>();
		List<CustomResponse> data = riskTreatmentService.getRiskTreatmentView(id);
		
		response.setData(data);
		response.setStatus(ResponseStatus.SUCCESS);
		response.setMessage("Saved.");
		return response;
	}
	
	@PostMapping(value = "/{id}/evidence", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
	public GeneralResponse<RiskResponseTreatmentResponse> uploadEvidence(@PathVariable("id") Long riskTreatmentId,
			@RequestParam("file") MultipartFile file,
			@RequestParam(value = "description", required = false) String description,
			@RequestParam(value = "purpose", required = false, defaultValue = "risk-response-treatment") String purpose)
			throws Exception {
		GeneralResponse<RiskResponseTreatmentResponse> response = new GeneralResponse<>();
		response.setData(riskTreatmentService.uploadEvidence(riskTreatmentId, file, description, purpose));
		response.setStatus(ResponseStatus.SUCCESS);
		response.setMessage("Supporting evidence uploaded successfully.");
		return response;
	}
	
	@GetMapping("/{id}/evidence")
	public GeneralResponse<Object> getEvidence(@PathVariable("id") Long riskTreatmentId) throws ResourceNotFoundException {
		GeneralResponse<Object> response = new GeneralResponse<>();
		response.setData(riskTreatmentService.getEvidence(riskTreatmentId));
		response.setStatus(ResponseStatus.SUCCESS);
		response.setMessage("Supporting evidence fetched.");
		return response;
	}
	
	@GetMapping("/evidence/{documentId}")
	public GeneralResponse<Object> downloadEvidence(@PathVariable("documentId") String documentId)
            throws IOException, ResourceNotFoundException {
		GeneralResponse<Object> response = new GeneralResponse<>();
		response.setData(riskTreatmentService.downloadEvidence(documentId));
		response.setStatus(ResponseStatus.SUCCESS);
		response.setMessage("Supporting evidence downloaded.");
		return response;
	}
	
	@DeleteMapping("/{id}/evidence/{documentId}")
	public GeneralResponse<Void> deleteEvidence(@PathVariable("id") Long riskTreatmentId,
			@PathVariable("documentId") String documentId) throws ResourceNotFoundException {
		GeneralResponse<Void> response = new GeneralResponse<>();
		riskTreatmentService.deleteEvidence(riskTreatmentId, documentId);
		response.setStatus(ResponseStatus.SUCCESS);
		response.setMessage("Supporting evidence deleted.");
		return response;
	}
	
	@GetMapping("/all")
	public GeneralResponse<Page<List<CustomResponse>>> getAllRisksPaginated(
			@PageableDefault(size = 20, sort = "id", direction = Sort.Direction.DESC) Pageable pageable)
			throws ResourceNotFoundException {

		GeneralResponse<Page<List<CustomResponse>>> response = new GeneralResponse<>();

		Page<List<CustomResponse>> risks = riskTreatmentService.getAllRisks(pageable);
		response.setData(risks);
		response.setMessage("Risks fetched with pagination.");
		response.setStatus(ResponseStatus.SUCCESS);

		return response;
	}

}
