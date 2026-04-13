package com.storage.service;

import java.io.IOException;

import com.storage.dto.request.DocumentUploadDto;
import com.storage.dto.response.DocumentDto;
import com.storage.exception.InvalidResourceAccess;
import com.storage.exception.ResourceNotFoundException;

public interface DocumentStorageService {

	public DocumentDto uploadDocument(DocumentUploadDto documentUploadDto) throws InvalidResourceAccess;

	void deleteDocument(String documentId) throws ResourceNotFoundException;

	DocumentDto downloadDocument(String documentId) throws IOException, ResourceNotFoundException;
}
