package ermorg.erm.service;

import java.util.List;

import org.springframework.web.multipart.MultipartFile;

import ermorg.erm.dto.bulk.BulkImportSummary;
import ermorg.erm.dto.bulk.BulkImportUploadResponse;
import ermorg.erm.exception.ResourceNotFoundException;

public interface IBulkImportService {

    byte[] downloadTemplate(Long moduleId, String templateType) throws ResourceNotFoundException;

    BulkImportSummary validateImport(MultipartFile file, Long moduleId, String templateType) throws Exception;

    BulkImportUploadResponse processImport(MultipartFile file, Long moduleId, String templateType) throws Exception;

    BulkImportUploadResponse getImportStatus(Long importId);

    List<Object> getImportErrors(Long importId);
}
