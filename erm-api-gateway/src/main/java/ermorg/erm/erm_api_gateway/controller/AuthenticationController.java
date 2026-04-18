package ermorg.erm.erm_api_gateway.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import ermorg.erm.erm_api_gateway.dto.request.LoginRequestDTO;
import ermorg.erm.erm_api_gateway.dto.response.GeneralResponse;
import ermorg.erm.erm_api_gateway.dto.response.ResponseStatus;
import ermorg.erm.erm_api_gateway.dto.response.TokenResponseDTO;
import ermorg.erm.erm_api_gateway.exception.PasswordNotMatchedException;
import ermorg.erm.erm_api_gateway.service.ITokenService;
import ermorg.erm.erm_api_gateway.service.IUserService;
import ermorg.erm.erm_api_gateway.util.JwtUtil;

import jakarta.validation.Valid;

@RestController
@RequestMapping(value = "/authenticate")
@CrossOrigin(origins = "*")
public class AuthenticationController {

	private JwtUtil jwtUtil;

	private IUserService userService;
	@Autowired
	private ITokenService tokenService;

	public AuthenticationController(JwtUtil jwtUtil, IUserService userService) {
		this.jwtUtil = jwtUtil;
		this.userService = userService;
	}

	@PostMapping("/login")
	public GeneralResponse<TokenResponseDTO> authenticate(@Valid @RequestBody LoginRequestDTO login)
			throws UsernameNotFoundException, PasswordNotMatchedException {

		GeneralResponse<TokenResponseDTO> response = new GeneralResponse<>();

		tokenService.generateToken(login.getUsername(), login.getPassword());

		response.setMessage("OTP sent.");
		response.setStatus(ResponseStatus.SUCCESS);

		return response;
	}

	@PostMapping("/verify-otp")
	public GeneralResponse<TokenResponseDTO> verifyOtp(@RequestBody LoginRequestDTO loginRequestDTO) {
		GeneralResponse<TokenResponseDTO> response = new GeneralResponse<>();

		TokenResponseDTO token = tokenService.verifyOtp(loginRequestDTO.getUsername(), loginRequestDTO.getOtp());

		response.setData(token);
		response.setMessage("OTP verified.");
		response.setStatus(ResponseStatus.SUCCESS);
		return response;
	}

	@GetMapping("/validate")
	public String testToken() {
		return "valid";
	}

}
