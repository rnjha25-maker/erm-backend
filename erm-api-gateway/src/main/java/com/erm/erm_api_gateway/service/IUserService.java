package com.erm.erm_api_gateway.service;

import org.springframework.security.core.userdetails.UsernameNotFoundException;

import com.erm.erm_api_gateway.dto.response.TokenResponseDTO;
import com.erm.erm_api_gateway.dto.response.UserResponse;
import com.erm.erm_api_gateway.exception.PasswordNotMatchedException;

public interface IUserService {

	public TokenResponseDTO generateToken(String username, String password)
			throws UsernameNotFoundException, PasswordNotMatchedException;

	UserResponse getUserByUsername(String username);

}
