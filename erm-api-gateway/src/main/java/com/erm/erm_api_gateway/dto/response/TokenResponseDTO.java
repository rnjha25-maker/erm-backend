package com.erm.erm_api_gateway.dto.response;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class TokenResponseDTO {

	private String token;
	private UserResponse currentUser;

}
