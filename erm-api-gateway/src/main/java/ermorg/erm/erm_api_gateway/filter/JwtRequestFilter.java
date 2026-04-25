package ermorg.erm.erm_api_gateway.filter;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.security.authentication.ReactiveAuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.context.ReactiveSecurityContextHolder;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.ReactiveUserDetailsService;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.web.server.authentication.ServerAuthenticationConverter;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.web.server.ServerWebExchange;
import org.springframework.web.server.WebFilter;
import org.springframework.web.server.WebFilterChain;

import ermorg.erm.erm_api_gateway.dto.response.UserResponse;
import ermorg.erm.erm_api_gateway.exception.TokenExpiredException;
import ermorg.erm.erm_api_gateway.model.RoleRight;
import ermorg.erm.erm_api_gateway.repository.RoleRightRepository;
import ermorg.erm.erm_api_gateway.service.IUserService;
import ermorg.erm.erm_api_gateway.util.JwtUtil;

import io.jsonwebtoken.ExpiredJwtException;
import jakarta.transaction.Transactional;
import reactor.core.publisher.Mono;

@Component
public class JwtRequestFilter implements ReactiveAuthenticationManager, WebFilter {

	private JwtUtil jwtUtil;
	private final ReactiveUserDetailsService userDetailsService;
	private final RoleRightRepository roleRightRepository;
	private IUserService userService;
	private String token;

	public JwtRequestFilter(JwtUtil jwtUtil, ReactiveUserDetailsService userDetailsService,
			RoleRightRepository roleRightRepository, IUserService userService) {
		this.jwtUtil = jwtUtil;
		this.userDetailsService = userDetailsService;
		this.roleRightRepository = roleRightRepository;
		this.userService = userService;

	}

	@Override
	public Mono<Void> filter(ServerWebExchange exchange, WebFilterChain chain) {

		return ReactiveSecurityContextHolder.getContext().flatMap(context -> {
			Authentication authentication = context.getAuthentication();
			if (authentication != null && authentication.isAuthenticated()) {
				String username = authentication.getName();

				if (hasAccess(exchange, authentication)) {
					Long organizationId = jwtUtil.extractOrganizationId(token);
					UserResponse user = userService.getUserByUsername(username);
					ServerHttpRequest request = exchange.getRequest().mutate()
							.header("X-User-Id", String.valueOf(user.getUserId()))
							.header("X-Org-Id", String.valueOf(organizationId))
							.header("X-Cmp-Id", String.valueOf(user.getCompanyId())).build();
					return chain.filter(exchange.mutate().request(request).build()); // Proceed if authorized
				} else {
					return Mono.error(new ResponseStatusException(HttpStatus.FORBIDDEN, "Access Denied"));
				}
			} else {
				return Mono.error(new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Unauthorized"));
			}
		}).switchIfEmpty(chain.filter(exchange)); // Allow if no SecurityContext is found

	}

	private ServerWebExchange addHeaders(ServerWebExchange exchange, String userId) {
		return exchange.mutate().request(exchange.getRequest().mutate().header("userId", userId).build()).build();
	}

	@Transactional

//	@Cacheable(value = "roleRights", key = "#organizationId")
	private boolean hasAccess(ServerWebExchange exchange, Authentication authentication) {

		Long organizationId = jwtUtil.extractOrganizationId(token);
		List<RoleRight> roleRights = roleRightRepository.getRoleRightByOrganizationId(organizationId);
//		if (roleRights == null || roleRights.size() <= 0)
//			return false;

		String path = exchange.getRequest().getURI().getPath();

		System.out.println("request for path " + path);
		return true;
//		boolean rturnVal = authentication.getAuthorities().stream().anyMatch(grantedAuthority -> {
//
//			return roleRights.stream().filter(right -> {
//
//				Role role = right.getRole();
//				return grantedAuthority.getAuthority().endsWith(role.getName());
//
//			}).anyMatch(right -> {
//
//				return path.toLowerCase().contains(right.getRight().getName().toLowerCase())
//						&& grantedAuthority.getAuthority().toLowerCase()
//								.substring(5, grantedAuthority.getAuthority().length())
//								.equals(right.getRole().getName().toLowerCase())
//						&& (path.toLowerCase().contains("delete") && right.getDelete())
//						|| (path.toLowerCase().contains("update") && right.getUpdate())
//						|| (path.toLowerCase().contains("create") && right.getCreate())
//						|| (path.toLowerCase().contains("validate") || path.toLowerCase().contains("get"));
//			});
//		});
//
//		return rturnVal;
	}

	@Override
	public Mono<Authentication> authenticate(Authentication authentication) throws AuthenticationException {
		token = authentication.getCredentials().toString();
		String username = null;
		try {
			username = jwtUtil.extractUsername(token);
		} catch (ExpiredJwtException e) {
			throw new TokenExpiredException("Login expired");
		}

		Mono<UserDetails> userDetailsMono = userDetailsService.findByUsername(username);
		userDetailsMono.map(user -> {
			return user.getUsername();
		}).subscribe();

		return userDetailsService.findByUsername(username).map(userDetails -> {
			if (jwtUtil.validateToken(token, userDetails.getUsername())) {

				return new UsernamePasswordAuthenticationToken(userDetails.getUsername(), null,
						userDetails.getAuthorities());
			} else {
				throw new AuthenticationException("Invalid JWT token") {
				};
			}
		});
	}

	public ServerAuthenticationConverter authenticationConverter() {

		return new ServerAuthenticationConverter() {
			@Override
			public Mono<Authentication> convert(ServerWebExchange exchange) {
				String token = exchange.getRequest().getHeaders().getFirst("Authorization");
				if (token != null && token.startsWith("Bearer ")) {
					token = token.substring(7);
					return Mono.just(SecurityContextHolder.getContext().getAuthentication());
				}
				return Mono.empty();
			}
		};
	}

}
