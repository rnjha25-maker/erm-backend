package ermorg.erm.erm_api_gateway.config.cors;

import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.reactive.CorsWebFilter;
import org.springframework.web.cors.reactive.UrlBasedCorsConfigurationSource;

//@Configuration
public class CorsConfig {

	//@Bean
	public CorsWebFilter corsWebFilter() {
		CorsConfiguration corsConfig = new CorsConfiguration();
//	        corsConfig.setAllowedOriginPatterns(Arrays.asList("http://localhost:3000", "http://localhost:5173", "http://sub.example.com"));
		corsConfig.addAllowedOrigin("http://localhost:3000"); // Replace with your frontend domain
		corsConfig.addAllowedMethod("*"); // Allow all HTTP methods
		corsConfig.addAllowedHeader("*"); // Allow all headers
		corsConfig.setAllowCredentials(true);
		UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
		source.registerCorsConfiguration("/**", corsConfig);

		return new CorsWebFilter(source);
	}
}
