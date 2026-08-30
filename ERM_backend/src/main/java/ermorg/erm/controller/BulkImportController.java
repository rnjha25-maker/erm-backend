package ermorg.erm.controller;

import java.util.List;

import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import ermorg.erm.dto.ResponseStatus;
import ermorg.erm.dto.bulk.BulkImportSummary;
import ermorg.erm.dto.bulk.BulkImportUploadResponse;
import ermorg.erm.exception.ResourceNotFoundException;
import ermorg.erm.response.GeneralResponse;
import ermorg.erm.service.IBulkImportService;

@RestController
@RequestMapping("/bulk-import")
public class BulkImportController {

    private final IBulkImportService bulkImportService;

    public BulkImportController(IBulkImportService bulkImportService) {
        this.bulkImportService = bulkImportService;
    }

    @GetMapping("/template/{moduleId}")
    public GeneralResponse<byte[]> downloadTemplate(@PathVariable Long moduleId,
            @RequestParam(required = false) String templateType) throws ResourceNotFoundException {
        GeneralResponse<byte[]> response = new GeneralResponse<>();
        response.setData(bulkImportService.downloadTemplate(moduleId, templateType));
        response.setStatus(ResponseStatus.SUCCESS);
        response.setMessage("Import template fetched.");
        return response;
    }

    @PostMapping(value = "/validate", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public GeneralResponse<BulkImportSummary> validate(@RequestParam("file") MultipartFile file,
            @RequestParam Long moduleId,
            @RequestParam(required = false) String templateType) throws Exception {
        GeneralResponse<BulkImportSummary> response = new GeneralResponse<>();
        response.setData(bulkImportService.validateImport(file, moduleId, templateType));
        response.setStatus(ResponseStatus.SUCCESS);
        response.setMessage("Validation complete.");
        return response;
    }

    @PostMapping(value = "/process", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public GeneralResponse<BulkImportUploadResponse> process(@RequestParam("file") MultipartFile file,
            @RequestParam Long moduleId,
            @RequestParam(required = false) String templateType) throws Exception {
        GeneralResponse<BulkImportUploadResponse> response = new GeneralResponse<>();
        response.setData(bulkImportService.processImport(file, moduleId, templateType));
        response.setStatus(ResponseStatus.SUCCESS);
        response.setMessage("Import processed.");
        return response;
    }

    @GetMapping("/{importId}/status")
    public GeneralResponse<BulkImportUploadResponse> status(@PathVariable Long importId) {
        GeneralResponse<BulkImportUploadResponse> response = new GeneralResponse<>();
        response.setData(bulkImportService.getImportStatus(importId));
        response.setStatus(ResponseStatus.SUCCESS);
        response.setMessage("Import status fetched.");
        return response;
    }

    @GetMapping("/{importId}/errors")
    public GeneralResponse<List<Object>> errors(@PathVariable Long importId) {
        GeneralResponse<List<Object>> response = new GeneralResponse<>();
        response.setData(bulkImportService.getImportErrors(importId));
        response.setStatus(ResponseStatus.SUCCESS);
        response.setMessage("Import errors fetched.");
        return response;
    }
}
