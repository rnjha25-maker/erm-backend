package com.erm.erm_api_gateway.service;

import com.erm.erm_api_gateway.dto.response.TokenResponseDTO;
import com.erm.erm_api_gateway.exception.PasswordNotMatchedException;

public interface ITokenService {

	public TokenResponseDTO generateToken(String username, String password) throws PasswordNotMatchedException;

	public TokenResponseDTO verifyOtp(String username, String otp);

}
