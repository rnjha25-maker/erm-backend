package ermorg.erm.controller;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import ermorg.erm.dto.ResponseStatus;
import ermorg.erm.dto.response.CustomResponse;
import ermorg.erm.dto.response.ErmMaturityResponse;
import ermorg.erm.dto.riskDTO.ErmMaturityDto;
import ermorg.erm.dto.riskDTO.ErmMaturityRequest;
import ermorg.erm.exception.ResourceNotFoundException;
import ermorg.erm.response.GeneralResponse;
import ermorg.erm.service.IErmMaturityService;

@RestController
@RequestMapping("/maturity")
public class MaturityController {

	@Autowired
	private IErmMaturityService ermMaturityService;

	@PostMapping("/save")
	public GeneralResponse<ErmMaturityResponse> save(@RequestBody List<ErmMaturityDto> maturityList)
			throws ResourceNotFoundException {

		// Wrap the list in ErmMaturityRequest for the service
		ErmMaturityRequest request = new ErmMaturityRequest();
		request.setMaturityRequest(maturityList);

		ErmMaturityResponse data = ermMaturityService.save(request);

		GeneralResponse<ErmMaturityResponse> response = new GeneralResponse<>();

		response.setData(data);
		response.setStatus(ResponseStatus.SUCCESS);
		response.setMessage("Saved");
		return response;
	}

	@GetMapping("/{id}")
	public GeneralResponse<ErmMaturityResponse> get(@PathVariable("id") Long maturityId)
			throws ResourceNotFoundException {

		GeneralResponse<ErmMaturityResponse> response = new GeneralResponse<>();

		ErmMaturityResponse data = ermMaturityService.get(maturityId);

		response.setData(data);
		response.setStatus(ResponseStatus.SUCCESS);
		return response;
	}

	@GetMapping("/get-view/{id}")
	public GeneralResponse<List<CustomResponse>> getView(@PathVariable("id") Long maturityId)
			throws ResourceNotFoundException {

		GeneralResponse<List<CustomResponse>> response = new GeneralResponse<>();

		List<CustomResponse> data = ermMaturityService.getView(maturityId);

		response.setData(data);
		response.setStatus(ResponseStatus.SUCCESS);
		return response;
	}

	@GetMapping("/all")
	public GeneralResponse<Page<CustomResponse>> getAll(
			@org.springframework.data.web.PageableDefault(size = 20, sort = "createdAt", direction = Sort.Direction.DESC) Pageable pageable)
			throws ResourceNotFoundException {
		GeneralResponse<Page<CustomResponse>> response = new GeneralResponse<>();

		Page<CustomResponse> data = ermMaturityService.getAll(pageable);

		response.setData(data);
		response.setStatus(ResponseStatus.SUCCESS);

		return response;
	}

	@DeleteMapping("/delete/{id}")
	public GeneralResponse<Void> delete(@PathVariable("id") Long maturityId) throws ResourceNotFoundException {

		GeneralResponse<Void> response = new GeneralResponse<>();

		ermMaturityService.delete(maturityId);

		response.setStatus(ResponseStatus.SUCCESS);
		response.setMessage("Deleted.");
		return response;
	}

}
