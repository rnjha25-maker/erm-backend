package ermorg.erm.controller;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import ermorg.erm.dto.ResponseStatus;
import ermorg.erm.dto.response.CustomResponse;
import ermorg.erm.dto.response.KriKpiReviewResponseDTO;
import ermorg.erm.dto.riskDTO.KriKpiReviewRequestDTO;
import ermorg.erm.exception.ResourceNotFoundException;
import ermorg.erm.response.GeneralResponse;
import ermorg.erm.service.IKriKpiRiskService;

@RestController
@RequestMapping("/kri-kpi-riview")
public class KriKpiReviewController {

	@Autowired
	private IKriKpiRiskService kriKpiRiskReviewService;

	@RequestMapping("/save")
	public GeneralResponse<KriKpiReviewResponseDTO> save(@RequestBody KriKpiReviewRequestDTO request)
			throws ResourceNotFoundException {
		GeneralResponse<KriKpiReviewResponseDTO> response = new GeneralResponse<KriKpiReviewResponseDTO>();

		KriKpiReviewResponseDTO data = kriKpiRiskReviewService.save(request);

		response.setData(data);
		response.setStatus(ResponseStatus.SUCCESS);

		response.setMessage("Saved.");
		;
		return response;
	}

	@GetMapping("/{id}")
	public GeneralResponse<KriKpiReviewResponseDTO> get(@PathVariable("id") Long kriId)
			throws ResourceNotFoundException {

		GeneralResponse<KriKpiReviewResponseDTO> response = new GeneralResponse<>();

		KriKpiReviewResponseDTO data = kriKpiRiskReviewService.get(kriId);

		response.setData(data);
		response.setStatus(ResponseStatus.SUCCESS);

		return response;

	}

	@GetMapping("/all")
	public GeneralResponse<List<List<CustomResponse>>> getAll() throws ResourceNotFoundException {

		GeneralResponse<List<List<CustomResponse>>> response = new GeneralResponse<>();

		List<List<CustomResponse>> data = kriKpiRiskReviewService.getAll();

		response.setData(data);
		response.setStatus(ResponseStatus.SUCCESS);

		return response;

	}

	@GetMapping("/get-view/{id}")
	public GeneralResponse<List<CustomResponse>> getView(@PathVariable("id") Long kriId)
			throws ResourceNotFoundException {

		GeneralResponse<List<CustomResponse>> response = new GeneralResponse<>();

		List<CustomResponse> data = kriKpiRiskReviewService.getView(kriId);

		response.setData(data);
		response.setStatus(ResponseStatus.SUCCESS);

		return response;

	}

	@DeleteMapping("/delete/{id}")
	public GeneralResponse<Void> delete(@PathVariable("id") Long kriId) throws ResourceNotFoundException {

		GeneralResponse<Void> response = new GeneralResponse<>();

		kriKpiRiskReviewService.delete(kriId);

		response.setMessage("Deleted.");
		response.setStatus(ResponseStatus.SUCCESS);

		return response;

	}
}
