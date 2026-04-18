package ermorg.erm.controller.exceptionHandler;

import java.util.HashMap;
import java.util.Map;

import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

import ermorg.erm.dto.ResponseStatus;
import ermorg.erm.exception.ResourceNotFoundException;
import ermorg.erm.response.GeneralResponse;

import lombok.extern.slf4j.Slf4j;
import io.swagger.v3.oas.annotations.Hidden;

@Hidden
@Slf4j
@ControllerAdvice
public class GlobalExceptionHandler {
	
	@ExceptionHandler(org.springframework.web.bind.MethodArgumentNotValidException.class)
	public ResponseEntity<GeneralResponse<Map<String, String>>> handleValidationExceptions(
			org.springframework.web.bind.MethodArgumentNotValidException ex) {
	
		GeneralResponse<Map<String, String>> response = new GeneralResponse<>();
		Map<String, String> errors = new HashMap<>();
		ex.getBindingResult().getFieldErrors().forEach(error -> {

			errors.put(error.getField(), error.getDefaultMessage());
			response.setMessage(error.getDefaultMessage());
		});
		
		response.setData(errors); 
		response.setStatus(ResponseStatus.FAILED);
		
		log.error(ex.getMessage());
		return new ResponseEntity<>(response, HttpStatus.BAD_REQUEST);
	}
	
	@ExceptionHandler(ResourceNotFoundException.class)
	public ResponseEntity<GeneralResponse<Object>> handleResourceNotFound(
			ResourceNotFoundException ex) {
	
		GeneralResponse<Object> response = new GeneralResponse<>();

		response.setMessage(ex.getMessage());
		response.setStatus(ResponseStatus.FAILED);
		
		log.error(ex.getMessage());
		return new ResponseEntity<>(response, HttpStatus.BAD_REQUEST);
	}
	
	@ExceptionHandler(IllegalArgumentException.class)
	public ResponseEntity<GeneralResponse<Object>> handleIllegalArgument(IllegalArgumentException ex) {

		GeneralResponse<Object> response = new GeneralResponse<>();

		response.setMessage(ex.getMessage());
		response.setStatus(ResponseStatus.FAILED);

		log.warn(ex.getMessage());
		return new ResponseEntity<>(response, HttpStatus.BAD_REQUEST);
	}

	@ExceptionHandler(DataIntegrityViolationException.class)
	public ResponseEntity<GeneralResponse<Object>> handleUserNotFound(DataIntegrityViolationException ex) {

		GeneralResponse<Object> response = new GeneralResponse<>();

		response.setMessage("Duplicate entry is not allowed.");
		response.setStatus(ResponseStatus.FAILED);

		log.error(ex.getMessage());
//		ex.printStackTrace();
		return new ResponseEntity<>(response, HttpStatus.INTERNAL_SERVER_ERROR);
	}
	
	@ExceptionHandler(Exception.class)
	public ResponseEntity<GeneralResponse<Object>> handleUserNotFound(Exception ex) {

		GeneralResponse<Object> response = new GeneralResponse<>();

		response.setMessage(ex.getMessage());
		response.setStatus(ResponseStatus.FAILED);

		ex.printStackTrace();
		log.error("Exception while performing operation",ex);
//		ex.printStackTrace();
		return new ResponseEntity<>(response, HttpStatus.INTERNAL_SERVER_ERROR);
	}



}
