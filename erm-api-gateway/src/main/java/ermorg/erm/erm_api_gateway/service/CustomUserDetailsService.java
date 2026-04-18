package ermorg.erm.erm_api_gateway.service;

import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.ReactiveUserDetailsService;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;

import ermorg.erm.erm_api_gateway.dto.response.UserResponse;

import reactor.core.publisher.Mono;

@Service
public class CustomUserDetailsService implements ReactiveUserDetailsService {

	@Autowired
	private IUserService userService;

	@Override
	public Mono<UserDetails> findByUsername(String username) {

		UserResponse user = userService.getUserByUsername(username);

		if (user != null) {
			String[] roles = user.getRoles().stream().map(role -> role.getName()).collect(Collectors.toList())
					.toArray(new String[0]);

			return Mono.just(User.withUsername(user.getEmail()).password(user.getPassKey()) // password encoder
					.roles(roles).build());
		}
		return Mono.empty();
	}

}
