package com.storage.service.impl;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Date;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.model.GetObjectRequest;
import com.amazonaws.services.s3.model.ObjectMetadata;
import com.amazonaws.services.s3.model.S3Object;
import com.storage.dto.request.DocumentUploadDto;
import com.storage.dto.response.DocumentDto;
import com.storage.exception.InvalidResourceAccess;
import com.storage.exception.ResourceNotFoundException;
import com.storage.model.Document;
import com.storage.model.Organization;
import com.storage.repository.DocumentRepository;
import com.storage.service.DocumentStorageService;
import com.storage.util.FileUtil;
import com.storage.util.OrganizationContext;

@Service
public class DocumentStorageServiceImpl implements DocumentStorageService {

	@Autowired
    private AmazonS3 s3Client;
	
	@Autowired
	private DocumentRepository documentRepository;
	
	private String bucketName = "erm-storage";

	@Override
	public DocumentDto uploadDocument(DocumentUploadDto documentUploadDto) throws InvalidResourceAccess {
		
		Organization organization = OrganizationContext.getOrganization();
		
		if(organization == null) {
			throw new InvalidResourceAccess("Invalid resource access.");
		}
		byte[] fileBytes = FileUtil.decodeBase64ToBytes(documentUploadDto.getFileContent());
		ByteArrayInputStream inputStream = new ByteArrayInputStream(fileBytes);

		ObjectMetadata metadata = new ObjectMetadata();
		metadata.setContentType(documentUploadDto.getContentType());
		metadata.setContentLength(fileBytes.length);
		metadata.setLastModified(new Date());
		String key = documentUploadDto.getPurpose() + "/" + documentUploadDto.getFileName() + UUID.randomUUID().toString() + documentUploadDto.getFileExtension();
		s3Client.putObject(bucketName, key, inputStream, metadata);
		
		Document document = new Document();
		document.setFileName(documentUploadDto.getFileName());
		document.setFileExtension(documentUploadDto.getFileExtension());
		document.setBucketName(bucketName);
		document.setPurpose(documentUploadDto.getPurpose());
		document.setContentType(documentUploadDto.getContentType());
		document.setFilePath(key);
		document.setOrganizationId(organization.getId());
		Document savedDocument = documentRepository.save(document);
		return new DocumentDto(savedDocument);
	}
	
	@Override
	public DocumentDto downloadDocument(String documentId) throws IOException, ResourceNotFoundException {
		UUID id = UUID.fromString(documentId);
		Document document = documentRepository.findById(id).filter(doc -> !doc.getDeleted()).orElseThrow(() -> new ResourceNotFoundException("No document found."));
		
        GetObjectRequest getObjectRequest = new GetObjectRequest(bucketName, document.getFilePath());

        S3Object fileObject = s3Client.getObject(getObjectRequest);

		InputStream fileContent = fileObject.getObjectContent();
		
		String base64String = FileUtil.toBase64String(fileContent.readAllBytes());
		
		return new DocumentDto(document, base64String);
	
	}
	
	@Override
	public void deleteDocument(String documentId) throws ResourceNotFoundException {
		UUID id = UUID.fromString(documentId);
		Document document = documentRepository.findById(id).orElseThrow(() -> new ResourceNotFoundException("No document found."));
		
		s3Client.deleteObject(bucketName, document.getFilePath());
		document.setDeleted(true);
		documentRepository.save(document);
	}
	
	
}
