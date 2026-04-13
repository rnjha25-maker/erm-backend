package com.storage.dto.request;

import lombok.Data;

@Data
public class DocumentUploadDto {
	
	private String documentId;
	private String fileName;
	private String fileExtension;
	private String bucketName;
	private String fileContent;
	private String purpose;
	private String contentType;

}
