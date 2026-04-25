package ermorg.erm.erm_api_gateway.config.security;

import java.util.Arrays;
import java.util.List;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
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

import ermorg.erm.erm_api_gateway.filter.JwtFilter;
import ermorg.erm.erm_api_gateway.filter.JwtRequestFilter;

@Configuration
@EnableWebFluxSecurity
public class SecurityConfig {

    private final JwtRequestFilter jwtRequestFilter;

    @Value("${allowed.origins}")
    private String allowedOrigins;

    public SecurityConfig(JwtRequestFilter jwtRequestFilter) {
        this.jwtRequestFilter = jwtRequestFilter;
    }

    private static final String[] PUBLIC_PATHS = {
            "/authenticate/**", "/verify-otp", "/",
            "/swagger-ui/**", "/swagger-ui.html",
            "/webjars/**", "/v3/api-docs/**",
            "/favicon.ico", "/.well-known/**",
            "/org/**", "/query-org/**", "/org-setup/**",
            "/user-setup/**", "/storage/**", "/erm/**"
    };

    @Bean
    public SecurityWebFilterChain securityFilterChain(
            ServerHttpSecurity http,
            ReactiveAuthenticationManager authManager,
            ServerAuthenticationConverter authConverter) {

        AuthenticationWebFilter authFilter = new AuthenticationWebFilter(jwtRequestFilter);
        authFilter.setServerAuthenticationConverter(authConverter);
        List<String> origins = Arrays.asList(allowedOrigins.split("\\s*,\\s*"));
        
        
        return http
                .csrf(csrf -> csrf.disable())

                // ✅ Modern CORS config (no deprecated usage)
                .cors(cors -> cors.configurationSource(exchange -> {
                    CorsConfiguration config = new CorsConfiguration();
                    config.setAllowedOriginPatterns(origins);  
                    config.setAllowedHeaders(Arrays.asList("*"));
                    config.setAllowedMethods(Arrays.asList("GET", "POST", "PUT", "DELETE", "OPTIONS", "PATCH"));
                    config.setAllowCredentials(true);
                    config.setMaxAge(3600L); // cache preflight for 1 hour
                    return config;
                }))

                .authorizeExchange(exchange -> exchange
                	    .pathMatchers(HttpMethod.OPTIONS, "/**").permitAll() // ✅ allow all preflight
                	    .pathMatchers(PUBLIC_PATHS).permitAll()
                	    .anyExchange().authenticated()
                )

                .securityContextRepository(NoOpServerSecurityContextRepository.getInstance())

                .authenticationManager(jwtRequestFilter)

                .addFilterAt(authFilter, SecurityWebFiltersOrder.AUTHENTICATION)
                .addFilterAt(new JwtFilter(jwtRequestFilter), SecurityWebFiltersOrder.AUTHENTICATION)

                .build();
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