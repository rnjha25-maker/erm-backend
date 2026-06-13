package ermorg.erm.erm_api_gateway.filter;

import org.springframework.http.HttpStatus;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.ReactiveSecurityContextHolder;
import org.springframework.security.core.userdetails.ReactiveUserDetailsService;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.web.server.ServerWebExchange;
import org.springframework.web.server.WebFilter;
import org.springframework.web.server.WebFilterChain;

import ermorg.erm.erm_api_gateway.dto.response.UserResponse;
import ermorg.erm.erm_api_gateway.service.IUserService;
import ermorg.erm.erm_api_gateway.service.OrganizationValidationService;
import ermorg.erm.erm_api_gateway.util.JwtUtil;

import io.jsonwebtoken.ExpiredJwtException;
import reactor.core.publisher.Mono;

@Component
public class JwtRequestFilter implements WebFilter {

	private final JwtUtil jwtUtil;
	private final ReactiveUserDetailsService userDetailsService;
	private final OrganizationValidationService organizationValidationService;
	private final IUserService userService;

	public JwtRequestFilter(
			JwtUtil jwtUtil,
			ReactiveUserDetailsService userDetailsService,
			OrganizationValidationService organizationValidationService,
			IUserService userService) {
		this.jwtUtil = jwtUtil;
		this.userDetailsService = userDetailsService;
		this.organizationValidationService = organizationValidationService;
		this.userService = userService;
	}

	@Override
	public Mono<Void> filter(ServerWebExchange exchange, WebFilterChain chain) {

		String token = extractToken(exchange);
		if (token == null) {
			return chain.filter(exchange);
		}

		String username;
		try {
			username = jwtUtil.extractUsername(token);
		} catch (ExpiredJwtException e) {
			return Mono.error(new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Token expired"));
		}

		return userDetailsService.findByUsername(username)
				.flatMap(userDetails -> {

					if (!jwtUtil.validateToken(token, userDetails.getUsername())) {
						return Mono.error(new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Invalid token"));
					}

					Long orgId = jwtUtil.extractOrganizationId(token);
					organizationValidationService.validateOrganizationCanAuthenticate(orgId);

					return userService.getUserByUsernameReactive(username) // MUST be reactive
							.flatMap(user -> {

								UsernamePasswordAuthenticationToken auth =
										new UsernamePasswordAuthenticationToken(
												userDetails,
												null,
												userDetails.getAuthorities()
										);

								return chain.filter(
										exchange.mutate()
												.request(addHeaders(exchange, user, orgId))
												.build()
								).contextWrite(
										ReactiveSecurityContextHolder.withAuthentication(auth)
								);
							});
				});
	}

	private ServerHttpRequest addHeaders(ServerWebExchange exchange, UserResponse user, Long orgId) {
		return exchange.getRequest().mutate()
				.header("X-User-Id", String.valueOf(user.getUserId()))
				.header("X-Org-Id", String.valueOf(orgId))
				.header("X-Cmp-Id", String.valueOf(user.getCompanyId()))
				.build();
	}

	private String extractToken(ServerWebExchange exchange) {
		String authHeader = exchange.getRequest().getHeaders().getFirst("Authorization");
		if (authHeader != null && authHeader.startsWith("Bearer ")) {
			return authHeader.substring(7);
		}
		return null;
	}

}
