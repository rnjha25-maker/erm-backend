package ermorg.erm.erm_api_gateway.controller.exceptionHandlers;

import java.util.HashMap;
import java.util.Map;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import ermorg.erm.erm_api_gateway.dto.response.GeneralResponse;
import ermorg.erm.erm_api_gateway.dto.response.ResponseStatus;
import ermorg.erm.erm_api_gateway.exception.PasswordNotMatchedException;
import ermorg.erm.erm_api_gateway.exception.TokenExpiredException;

import lombok.extern.slf4j.Slf4j;
import io.swagger.v3.oas.annotations.Hidden;

@Hidden
@RestControllerAdvice
@Slf4j
public class GlobalExceptionHandler {

	@ExceptionHandler(org.springframework.web.bind.support.WebExchangeBindException.class)
	public ResponseEntity<GeneralResponse<Map<String, String>>> handleValidationExceptions(
			org.springframework.web.bind.support.WebExchangeBindException ex) {

		GeneralResponse<Map<String, String>> response = new GeneralResponse<>();
		Map<String, String> errors = new HashMap<>();
//		ex.printStackTrace();
		ex.getBindingResult().getFieldErrors().forEach(error -> {

			errors.put(error.getField(), error.getDefaultMessage());
			response.setMessage(error.getDefaultMessage());
		});

		response.setData(errors);
		response.setStatus(ResponseStatus.FAILD);

		log.error(ex.getMessage());
		return new ResponseEntity<>(response, HttpStatus.BAD_REQUEST);
	}

	@ExceptionHandler(PasswordNotMatchedException.class)
	public ResponseEntity<GeneralResponse<Object>> handlePasswordNotMatch(PasswordNotMatchedException ex) {

		GeneralResponse<Object> response = new GeneralResponse<>();

		response.setMessage(ex.getMessage());
		response.setStatus(ResponseStatus.FAILD);

		log.error(ex.getMessage());
		return new ResponseEntity<>(response, HttpStatus.BAD_REQUEST);
	}

	@ExceptionHandler(UsernameNotFoundException.class)
	public ResponseEntity<GeneralResponse<Object>> handleUserNotFound(UsernameNotFoundException ex) {

		GeneralResponse<Object> response = new GeneralResponse<>();

		response.setMessage(ex.getMessage());
		response.setStatus(ResponseStatus.FAILD);

		log.error(ex.getMessage());
		return new ResponseEntity<>(response, HttpStatus.BAD_REQUEST);
	}

	@ExceptionHandler(TokenExpiredException.class)
	public ResponseEntity<GeneralResponse<Object>> handleUserNotFound(TokenExpiredException ex) {

		GeneralResponse<Object> response = new GeneralResponse<>();

		response.setMessage(ex.getMessage());
		response.setStatus(ResponseStatus.FAILD);

		log.error(ex.getMessage());
		return new ResponseEntity<>(response, HttpStatus.UNAUTHORIZED);
	}

	@ExceptionHandler(Exception.class)
	public ResponseEntity<GeneralResponse<Object>> handleUserNotFound(Exception ex) {

		GeneralResponse<Object> response = new GeneralResponse<>();

		response.setMessage(ex.getMessage());
		response.setStatus(ResponseStatus.FAILD);
		ex.printStackTrace();
		log.error(ex.getMessage());
		return new ResponseEntity<>(response, HttpStatus.INTERNAL_SERVER_ERROR);
	}
//	
}
