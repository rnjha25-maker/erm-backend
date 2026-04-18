package ermorg.storage.service;

import java.io.IOException;

import ermorg.storage.dto.request.DocumentUploadDto;
import ermorg.storage.dto.response.DocumentDto;
import ermorg.storage.exception.InvalidResourceAccess;
import ermorg.storage.exception.ResourceNotFoundException;

public interface DocumentStorageService {

	public DocumentDto uploadDocument(DocumentUploadDto documentUploadDto) throws InvalidResourceAccess;

	void deleteDocument(String documentId) throws ResourceNotFoundException;

	DocumentDto downloadDocument(String documentId) throws IOException, ResourceNotFoundException;
}
