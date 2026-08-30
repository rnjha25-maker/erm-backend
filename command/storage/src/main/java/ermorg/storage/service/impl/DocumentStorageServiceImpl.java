package ermorg.storage.service.impl;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Base64;
import java.util.Date;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.model.GetObjectRequest;
import com.amazonaws.services.s3.model.ObjectMetadata;
import com.amazonaws.services.s3.model.S3Object;
import ermorg.storage.dto.request.DocumentUploadDto;
import ermorg.storage.dto.response.DocumentDto;
import ermorg.storage.exception.InvalidResourceAccess;
import ermorg.storage.exception.ResourceNotFoundException;
import ermorg.storage.model.Document;
import ermorg.storage.model.Organization;
import ermorg.storage.repository.DocumentRepository;
import ermorg.storage.service.DocumentStorageService;
import ermorg.storage.util.FileUtil;
import ermorg.storage.util.OrganizationContext;

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
		Long organizationId = documentUploadDto.getOrganizationId();
		if (organizationId == null && organization != null) {
			organizationId = organization.getId();
		}
		
		if(organizationId == null) {
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
		document.setOrganizationId(organizationId);
		document.setCompanyId(documentUploadDto.getCompanyId());
		Document savedDocument = documentRepository.save(document);
		return new DocumentDto(savedDocument);
	}

	@Override
	public DocumentDto uploadDocument(MultipartFile file, Long organizationId, Long companyId, String purpose)
			throws IOException, InvalidResourceAccess {
		if (file == null || file.isEmpty()) {
			throw new IllegalArgumentException("Please select a file to upload.");
		}
		if (file.getSize() > 10L * 1024 * 1024) {
			throw new IllegalArgumentException("File size exceeds the allowed limit of 10MB.");
		}
		String originalFilename = StringUtils.cleanPath(file.getOriginalFilename() == null ? "document" : file.getOriginalFilename());
		String extension = StringUtils.getFilenameExtension(originalFilename);
		String fileName = StringUtils.stripFilenameExtension(originalFilename);
		if (fileName == null || fileName.isBlank()) {
			fileName = "supporting-evidence";
		}
		if (extension == null || extension.isBlank()) {
			extension = "bin";
		}
		DocumentUploadDto uploadRequest = new DocumentUploadDto();
		uploadRequest.setFileName(fileName);
		uploadRequest.setFileExtension("." + extension);
		uploadRequest.setContentType(file.getContentType() == null ? "application/octet-stream" : file.getContentType());
		uploadRequest.setPurpose(purpose == null || purpose.isBlank() ? "risk-response-treatment" : purpose);
		uploadRequest.setFileContent(Base64.getEncoder().encodeToString(file.getBytes()));
		uploadRequest.setOrganizationId(organizationId);
		uploadRequest.setCompanyId(companyId);
		return uploadDocument(uploadRequest);
	}

	@Override
	public DocumentDto getDocument(String documentId) throws ResourceNotFoundException {
		UUID id = UUID.fromString(documentId);
		Document document = documentRepository.findById(id).filter(doc -> !doc.getDeleted())
				.orElseThrow(() -> new ResourceNotFoundException("No document found."));
		return new DocumentDto(document);
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
