package ermorg.erm.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import ermorg.erm.dto.ResponseStatus;
import ermorg.erm.dto.response.KpaKpiReviewResponseDTO;
import ermorg.erm.dto.riskDTO.KpaKpiReviewRequestDTO;
import ermorg.erm.exception.ResourceNotFoundException;
import ermorg.erm.response.GeneralResponse;
import ermorg.erm.serviceimpl.KpaKpiReviewService;
import jakarta.validation.Valid;

@RestController
@RequestMapping("/kpa-kpi-review")
public class KpaKpiReviewController {

    @Autowired
    private KpaKpiReviewService kpaKpiReviewService;

    @PostMapping
    public GeneralResponse<KpaKpiReviewResponseDTO> save(@Valid @RequestBody KpaKpiReviewRequestDTO request)
            throws ResourceNotFoundException {
        GeneralResponse<KpaKpiReviewResponseDTO> response = new GeneralResponse<>();
        response.setData(kpaKpiReviewService.save(request));
        response.setStatus(ResponseStatus.SUCCESS);
        response.setMessage("Saved.");
        return response;
    }

    @GetMapping("/get-view/{id}")
    public GeneralResponse<KpaKpiReviewResponseDTO> get(@PathVariable("id") Long id)
            throws ResourceNotFoundException {
        GeneralResponse<KpaKpiReviewResponseDTO> response = new GeneralResponse<>();
        response.setData(kpaKpiReviewService.get(id));
        response.setStatus(ResponseStatus.SUCCESS);
        return response;
    }

    @GetMapping("/all")
    public GeneralResponse<Page<KpaKpiReviewResponseDTO>> getAll(
            @PageableDefault(size = 20, sort = "id", direction = Sort.Direction.DESC) Pageable pageable,
            @RequestParam(required = false) String status,
            @RequestParam(required = false) String search) throws ResourceNotFoundException {
        GeneralResponse<Page<KpaKpiReviewResponseDTO>> response = new GeneralResponse<>();
        response.setData(kpaKpiReviewService.getAll(pageable, status, search));
        response.setStatus(ResponseStatus.SUCCESS);
        return response;
    }

    @DeleteMapping("/{id}")
    public GeneralResponse<Void> delete(@PathVariable("id") Long id) throws ResourceNotFoundException {
        GeneralResponse<Void> response = new GeneralResponse<>();
        kpaKpiReviewService.delete(id);
        response.setMessage("Deleted.");
        response.setStatus(ResponseStatus.SUCCESS);
        return response;
    }
}
