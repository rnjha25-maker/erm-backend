package ermorg.storage.controller;

import java.io.IOException;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import ermorg.storage.dto.request.DocumentUploadDto;
import ermorg.storage.dto.response.DocumentDto;
import ermorg.storage.dto.response.GeneralResponse;
import ermorg.storage.dto.response.ResponseStatus;
import ermorg.storage.exception.InvalidResourceAccess;
import ermorg.storage.exception.ResourceNotFoundException;
import ermorg.storage.service.DocumentStorageService;

@RestController
public class DocumentStorageController {

	@Autowired
	private DocumentStorageService documentStorageService;
	
	@PostMapping("/upload")
	public GeneralResponse<DocumentDto> uploadFile(@RequestBody DocumentUploadDto request) throws InvalidResourceAccess {
		GeneralResponse<DocumentDto> response = new GeneralResponse<>();
		DocumentDto document = documentStorageService.uploadDocument(request);
		
		response.setData(document);
		response.setStatus(ResponseStatus.SUCCESS);
		response.setMessage("Upload successful");
		
		return response;
	}
	
	@GetMapping("/download/{documentId}")
	public GeneralResponse<DocumentDto> uploadFile(@PathVariable("documentId") String documentId) throws IOException, ResourceNotFoundException {
		GeneralResponse<DocumentDto> response = new GeneralResponse<>();
		DocumentDto document = documentStorageService.downloadDocument(documentId);
		
		response.setData(document);
		response.setStatus(ResponseStatus.SUCCESS);
		response.setMessage("Upload successful");
		
		return response;
	}
	
	@DeleteMapping("/delete/{documentId}")
	public GeneralResponse<Void> deleteFile(@PathVariable("documentId") String documentId) throws IOException, ResourceNotFoundException {
		GeneralResponse<Void> response = new GeneralResponse<>();
		
		documentStorageService.deleteDocument(documentId);
		
		response.setStatus(ResponseStatus.SUCCESS);
		response.setMessage("Deletion successful");
		
		return response;
	}
}
