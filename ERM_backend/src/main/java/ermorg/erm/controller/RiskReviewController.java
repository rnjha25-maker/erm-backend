package ermorg.erm.controller;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import ermorg.erm.dto.ResponseStatus;
import ermorg.erm.dto.response.CustomResponse;
import ermorg.erm.dto.response.RiskReviewResponseDtoResponse;
import ermorg.erm.dto.riskDTO.RiskReviewRequestDTO;
import ermorg.erm.exception.ResourceNotFoundException;
import ermorg.erm.response.GeneralResponse;
import ermorg.erm.service.IRiskReviewService;

@RestController
@RequestMapping("/riskReview")
public class RiskReviewController {

	@Autowired
	private IRiskReviewService riskReviewService;
	
	@PostMapping("/save")
	public GeneralResponse<RiskReviewResponseDtoResponse> saveRiskReview(@RequestBody RiskReviewRequestDTO requestDTO) throws ResourceNotFoundException {
		GeneralResponse<RiskReviewResponseDtoResponse> response = new GeneralResponse<>();
		RiskReviewResponseDtoResponse saveRiskReview = riskReviewService.saveRiskReview(requestDTO);
		
		response.setData(saveRiskReview);
		response.setMessage("Saved");
		response.setStatus(ResponseStatus.SUCCESS);
		
		return response;
	}
	
	@GetMapping("/{id}")
	public GeneralResponse<RiskReviewResponseDtoResponse> get(@PathVariable("id") Long id) throws ResourceNotFoundException {
		GeneralResponse<RiskReviewResponseDtoResponse> response = new GeneralResponse<>();
		RiskReviewResponseDtoResponse saveRiskReview = riskReviewService.get(id);
		
		response.setData(saveRiskReview);
		response.setStatus(ResponseStatus.SUCCESS);
		
		return response;
	}
	
	@GetMapping("/get-view/{id}")
	public GeneralResponse<List<CustomResponse>> getView(@PathVariable("id") Long id) throws ResourceNotFoundException {
		GeneralResponse<List<CustomResponse>> response = new GeneralResponse<>();
		List<CustomResponse> saveRiskReview = riskReviewService.getView(id);
		
		response.setData(saveRiskReview);
		response.setStatus(ResponseStatus.SUCCESS);
		
		return response;
	}
	
	@GetMapping("/all")
	public GeneralResponse<List<List<CustomResponse>>> getView() throws ResourceNotFoundException {
		GeneralResponse<List<List<CustomResponse>>> response = new GeneralResponse<>();
		List<List<CustomResponse>> saveRiskReview = riskReviewService.getAll();
		
		response.setData(saveRiskReview);
		response.setStatus(ResponseStatus.SUCCESS);
		
		return response;
	}
	
	@DeleteMapping("/delete/{id}")
	public GeneralResponse<Void> DeleteView(@PathVariable("id") Long id) throws ResourceNotFoundException {
		GeneralResponse<Void> response = new GeneralResponse<>();
		riskReviewService.delete(id);
		
		response.setMessage("Deleted");
		response.setStatus(ResponseStatus.SUCCESS);
		
		return response;
	}
}
