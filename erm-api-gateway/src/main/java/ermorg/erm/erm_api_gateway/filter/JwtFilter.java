package ermorg.erm.erm_api_gateway.filter;

import org.springframework.web.server.ServerWebExchange;
import org.springframework.web.server.WebFilter;
import org.springframework.web.server.WebFilterChain;

import reactor.core.publisher.Mono;

public class JwtFilter implements WebFilter {
	private final JwtRequestFilter jwtRequestFilter;

	public JwtFilter(JwtRequestFilter jwtRequestFilter) {
		this.jwtRequestFilter = jwtRequestFilter;
	}

	@Override
	public Mono<Void> filter(ServerWebExchange exchange, WebFilterChain chain) {
		return jwtRequestFilter.filter(exchange, chain);
	}

}
