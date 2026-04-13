package com.storage.dto.response;

import com.storage.model.Document;

import lombok.Data;

@Data
public class DocumentDto {
	
	private String documentId;
	private String fileName;
	private String fileExtension;
	private String bucketName;
	private String fileContent;
	private String contentType;
	private String purpose;
	
	public DocumentDto(Document doc) {
		
		this.documentId = doc.getId().toString();
		this.fileName = doc.getFileName();
		this.fileExtension = doc.getFileExtension();
		this.bucketName = doc.getBucketName();
		this.contentType = doc.getContentType();
		this.purpose = doc.getPurpose();
	}
	
	public DocumentDto(Document doc, String base64) {
		
		this.documentId = doc.getId().toString();
		this.fileName = doc.getFileName();
		this.fileExtension = doc.getFileExtension();
		this.bucketName = doc.getBucketName();
		this.contentType = doc.getContentType();
		this.purpose = doc.getPurpose();
		this.fileContent = base64;
	}


}
