package ermorg.erm.erm_api_gateway.service;

import org.bouncycastle.crypto.RuntimeCryptoException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import ermorg.erm.erm_api_gateway.dto.response.TokenResponseDTO;
import ermorg.erm.erm_api_gateway.dto.response.UserResponse;
import ermorg.erm.erm_api_gateway.exception.PasswordNotMatchedException;
import ermorg.erm.erm_api_gateway.model.OTPStorage;
import ermorg.erm.erm_api_gateway.repository.OTPStorageRepository;
import ermorg.erm.erm_api_gateway.util.JwtUtil;

@Service
public class TokenServiceImpl implements ITokenService {

	@Autowired
	private IUserService userService;

	@Autowired
	private OTPStorageRepository otpStorageRepository;

	@Autowired
	private JwtUtil jwtUtil;

	@Autowired
	private OrganizationValidationService organizationValidationService;

	@Override
	public TokenResponseDTO generateToken(String username, String password) throws PasswordNotMatchedException {

		UserResponse user = userService.getUserByUsername(username);

		if (user == null)
			throw new UsernameNotFoundException("User not found with email " + username);

		String password2 = user.getPassKey();

		if (!password.equals(password2))
			throw new PasswordNotMatchedException("Incorrect password!");

		organizationValidationService.validateOrganizationCanAuthenticate(user.getOrgId());

//		String generatedToken = jwtUtil.generateToken(username, user.getOrgId());

		OTPStorage otpStorage = new OTPStorage();

		otpStorage.setEmail(user.getEmail());
//		otpStorage.setToken(generatedToken);
		otpStorage.setOrganizationId(user.getOrgId());
		otpStorage.setCompanyId(user.getCompanyId());
		otpStorage.setOtp("1234");
		otpStorage.setVerified(false);

		otpStorageRepository.save(otpStorage);

		TokenResponseDTO token = new TokenResponseDTO();
//		token.setToken(generatedToken);
		token.setCurrentUser(user);
		return token;
	}

	@Override
	public TokenResponseDTO verifyOtp(String username, String otp) {

		OTPStorage sotoredOtp = otpStorageRepository.findByEmail(username);
		String generatedToken = null;
		if (sotoredOtp != null && sotoredOtp.getOtp().equals(otp)) {

			organizationValidationService.validateOrganizationCanAuthenticate(sotoredOtp.getOrganizationId());

			generatedToken = jwtUtil.generateToken(username, sotoredOtp.getOrganizationId(), sotoredOtp.getCompanyId());

//			 generatedToken = sotoredOtp.getToken();
			sotoredOtp.setVerified(true);
			otpStorageRepository.save(sotoredOtp);
		} else {
			throw new RuntimeCryptoException("Invalid Otp");
		}
		UserResponse user = userService.getUserByUsername(username);
		TokenResponseDTO token = new TokenResponseDTO();
		token.setToken(generatedToken);
		token.setCurrentUser(user);
		return token;
	}

}
