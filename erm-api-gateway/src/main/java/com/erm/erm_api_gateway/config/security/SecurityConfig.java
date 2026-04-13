package com.erm.erm_api_gateway.config.security;

import java.util.Arrays;
import java.util.List;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.ReactiveAuthenticationManager;
import org.springframework.security.config.annotation.web.reactive.EnableWebFluxSecurity;
import org.springframework.security.config.web.server.SecurityWebFiltersOrder;
import org.springframework.security.config.web.server.ServerHttpSecurity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.server.SecurityWebFilterChain;
import org.springframework.security.web.server.authentication.AuthenticationWebFilter;
import org.springframework.security.web.server.authentication.ServerAuthenticationConverter;
import org.springframework.security.web.server.context.NoOpServerSecurityContextRepository;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.server.WebFilter;

import com.erm.erm_api_gateway.filter.JwtFilter;
import com.erm.erm_api_gateway.filter.JwtRequestFilter;

@Configuration
@EnableWebFluxSecurity
public class SecurityConfig {

	private JwtRequestFilter jwtRequestFilter;

	@Value("${allowed.origins}")
	private String[] allowedOrigin; // String allowedOrigin;

	public SecurityConfig(JwtRequestFilter jwtRequestFilter) {
		this.jwtRequestFilter = jwtRequestFilter;
	}

	@Bean
	public SecurityWebFilterChain securityFilterChain(ServerHttpSecurity http,
			ReactiveAuthenticationManager authManager, ServerAuthenticationConverter authConverter) throws Exception {

		AuthenticationWebFilter authenticationWebFilter = new AuthenticationWebFilter(jwtRequestFilter);
		authenticationWebFilter.setServerAuthenticationConverter(authConverter);

		return http.csrf(csrf -> csrf.disable()).cors().configurationSource(request -> {
			CorsConfiguration corsConfig = new CorsConfiguration();
			corsConfig.setAllowedOriginPatterns(Arrays.asList(allowedOrigin));
//                    corsConfig.addAllowedOrigin(allowedOrigin);
			corsConfig.addAllowedMethod("*");
			corsConfig.addAllowedHeader("*");
			corsConfig.setAllowCredentials(true);
			return corsConfig;
		}).and().authorizeExchange()
				.pathMatchers("/authenticate/**", "/verify-otp", "/",
					    "/swagger-ui/**", "/swagger-ui.html",
					    "/webjars/**",
					    "/v3/api-docs", "/v3/api-docs/**",
					    "/favicon.ico",
					    "/org/swagger-ui/**", "/query-org/swagger-ui/**", 
					    "/org-setup/swagger-ui/**", "/user-setup/swagger-ui/**", 
					    "/storage/swagger-ui/**", "/erm/swagger-ui/**",
					    "/v3/api-docs/**",
					    "/swagger-ui.html",
					    "/swagger-ui/**",
					    "/webjars/**",
					    "/favicon.ico",
					    "/.well-known/**")
				.permitAll().anyExchange().authenticated().and()
				.securityContextRepository(NoOpServerSecurityContextRepository.getInstance())

                .authenticationManager(jwtRequestFilter)

				.addFilterAt(authenticationWebFilter, SecurityWebFiltersOrder.AUTHENTICATION)
				.addFilterAt(new JwtFilter(jwtRequestFilter), SecurityWebFiltersOrder.AUTHENTICATION).build();

	}

	@Bean
	public WebFilter logRequestUrl() {
	    return (exchange, chain) -> {
	        System.out.println("Request: " + exchange.getRequest().getURI());
	        return chain.filter(exchange);
	    };
	}
	@Bean
	public PasswordEncoder passwordEncoder() {
		return new BCryptPasswordEncoder();
	}

}
