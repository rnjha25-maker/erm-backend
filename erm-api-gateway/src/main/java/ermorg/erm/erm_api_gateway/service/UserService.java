package ermorg.erm.erm_api_gateway.service;

import java.util.List;

import org.springframework.stereotype.Service;

import ermorg.erm.erm_api_gateway.dto.response.RightResponse;
import ermorg.erm.erm_api_gateway.dto.response.RoleResponse;
import ermorg.erm.erm_api_gateway.dto.response.UserResponse;
import ermorg.erm.erm_api_gateway.model.Role;
import ermorg.erm.erm_api_gateway.model.User;
import ermorg.erm.erm_api_gateway.repository.UserRepository;

import lombok.RequiredArgsConstructor;
import reactor.core.publisher.Mono;

@Service
@RequiredArgsConstructor
public class UserService implements IUserService {

	private final UserRepository userRepository;

	@Override
	public Mono<UserResponse> getUserByUsernameReactive(String username) {
		return Mono.justOrEmpty(userRepository.findByEmailWithRoles(username))
			.switchIfEmpty(Mono.error(new RuntimeException("User not found with email: " + username)))
			.map(this::mapToUserResponse);
	}

	    @Override
	    public UserResponse getUserByUsername(String username) {
		User user = userRepository.findByEmailWithRoles(username)
			.orElseThrow(() -> new RuntimeException("User not found with email: " + username));
		return mapToUserResponse(user);
	    }

	// =======================
	// MAPPERS
	// =======================

	private UserResponse mapToUserResponse(User user) {
		List<RoleResponse> roles = user.getRoles()
				.stream()
				.map(this::mapToRoleResponse)
				.toList();

		return new UserResponse(user, roles);
	}

	private RoleResponse mapToRoleResponse(Role role) {
		List<RightResponse> rights = role.getRoleRights()
				.stream()
				.map(RightResponse::new)
				.toList();

		return new RoleResponse(role, rights);
	}
}