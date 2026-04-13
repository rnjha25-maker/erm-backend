package com.erm.erm_api_gateway.dto.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class LoginRequestDTO {

	@NotNull(message = "Email is required!")
	@NotBlank(message = "Email is required!")
	@Email(message = "Email should be valid")
	private String username;
	@NotBlank(message = "Password is required!")
	@NotNull(message = "Password is required!")
	@Size(min = 8, message = "Password must be at least 8 characters long")
	@Pattern(message = "Password must contain at least one uppercase letter, one lowercase letter, and one number", regexp = "^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d).+$")
	private String password;

	private String otp;

}
