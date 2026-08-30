package ermorg.storage.service;

import java.io.IOException;

import ermorg.storage.dto.request.DocumentUploadDto;
import ermorg.storage.dto.response.DocumentDto;
import ermorg.storage.exception.InvalidResourceAccess;
import ermorg.storage.exception.ResourceNotFoundException;
import org.springframework.web.multipart.MultipartFile;

public interface DocumentStorageService {

	public DocumentDto uploadDocument(DocumentUploadDto documentUploadDto) throws InvalidResourceAccess;

	DocumentDto uploadDocument(MultipartFile file, Long organizationId, Long companyId, String purpose)
			throws IOException, InvalidResourceAccess;

	void deleteDocument(String documentId) throws ResourceNotFoundException;

	DocumentDto getDocument(String documentId) throws ResourceNotFoundException;

	DocumentDto downloadDocument(String documentId) throws IOException, ResourceNotFoundException;
}
