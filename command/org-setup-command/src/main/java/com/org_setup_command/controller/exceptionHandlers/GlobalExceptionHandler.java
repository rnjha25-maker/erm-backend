package com.org_setup_command.controller.exceptionHandlers;

import java.util.HashMap;
import java.util.Map;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import com.org_setup_command.dto.response.GeneralResponse;
import com.org_setup_command.dto.response.ResponseStatus;
import com.org_setup_command.exception.ResourceNotFoundException;

import lombok.extern.slf4j.Slf4j;
import io.swagger.v3.oas.annotations.Hidden;

@Hidden
@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {


	@ExceptionHandler(org.springframework.web.bind.support.WebExchangeBindException.class)
	public ResponseEntity<GeneralResponse<Map<String, String>>> handleValidationExceptions(
			org.springframework.web.bind.support.WebExchangeBindException ex) {
		
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
	public ResponseEntity<GeneralResponse<Object>> handleUserNotFound(ResourceNotFoundException ex) {

		GeneralResponse<Object> response = new GeneralResponse<>();

		response.setMessage(ex.getMessage());
		response.setStatus(ResponseStatus.FAILED);

		log.error(ex.getMessage());
		return new ResponseEntity<>(response, HttpStatus.BAD_REQUEST);
	}
	
	@ExceptionHandler(Exception.class)
	public ResponseEntity<GeneralResponse<Object>> handleUserNotFound(Exception ex) {

		GeneralResponse<Object> response = new GeneralResponse<>();

		response.setMessage(ex.getMessage());
		response.setStatus(ResponseStatus.FAILED);

		log.error(ex.getMessage());
		return new ResponseEntity<>(response, HttpStatus.INTERNAL_SERVER_ERROR);
	}

}
