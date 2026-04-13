package com.erm.erm_command_organization.controller.exceptionHandlers;

import java.util.HashMap;
import java.util.Map;

import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import com.erm.erm_command_organization.dto.responseDTO.GeneralResponse;
import com.erm.erm_command_organization.dto.responseDTO.ResponseStatus;

import lombok.extern.slf4j.Slf4j;
import io.swagger.v3.oas.annotations.Hidden;

@Hidden
@RestControllerAdvice
@Slf4j
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
		log.error(ex.getMessage());
//		ex.printStackTrace();
		return new ResponseEntity<>(response, HttpStatus.INTERNAL_SERVER_ERROR);
	}

}
