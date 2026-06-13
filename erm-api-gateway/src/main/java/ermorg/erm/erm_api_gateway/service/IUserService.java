package ermorg.erm.erm_api_gateway.service;
import ermorg.erm.erm_api_gateway.dto.response.UserResponse;
import reactor.core.publisher.Mono;

public interface IUserService {

	Mono<UserResponse> getUserByUsernameReactive(String username);

	/**
	 * Synchronous accessor used by blocking services (kept for compatibility).
	 */
	UserResponse getUserByUsername(String username);

}
