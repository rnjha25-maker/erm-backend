package ermorg.erm.erm_api_gateway.service;

import ermorg.erm.erm_api_gateway.dto.response.RoleResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.ReactiveUserDetailsService;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import reactor.core.publisher.Mono;

@Service
public class CustomUserDetailsService implements ReactiveUserDetailsService {

	@Autowired
	private IUserService userService;

	@Override
	public Mono<UserDetails> findByUsername(String username) {

		return userService.getUserByUsernameReactive(username)
				.switchIfEmpty(Mono.error(new UsernameNotFoundException(
						"User not found: " + username
				)))
				.map(user -> {

					String[] roles = user.getRoles()
							.stream()
							.map(RoleResponse::getName)
							.toArray(String[]::new);

					return User.withUsername(user.getEmail())
							.password(user.getPassKey()) // already encoded
							.roles(roles)
							.build();
				});
	}

}
