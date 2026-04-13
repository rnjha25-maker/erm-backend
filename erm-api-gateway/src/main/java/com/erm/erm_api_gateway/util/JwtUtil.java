package com.erm.erm_api_gateway.util;

import java.util.Date;

import org.springframework.http.HttpHeaders;
import org.springframework.stereotype.Component;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;

@Component
public class JwtUtil {

	private String secretKey = "aDOzmJzMtKlQdQSM8aD0fgCSZyahQGKFHZj/77yFFcI=";

	public String generateToken(String username, Long organizationId, Long companyId) {

		return Jwts.builder().setSubject(username).claim("organizationId", organizationId) // Custom claim
				.claim("companyId", companyId).setIssuedAt(new Date())
				.setExpiration(new Date(System.currentTimeMillis() + 86400000)) // 1 day
																				// expiration
				.signWith(SignatureAlgorithm.HS256, secretKey).compact();
	}

	public String extractUsername(String token) {
		return Jwts.parserBuilder().setSigningKey(secretKey).build().parseClaimsJws(token).getBody().getSubject();
	}

	public Long extractOrganizationId(String token) {
		return Jwts.parserBuilder().setSigningKey(secretKey).build().parseClaimsJws(token).getBody()
				.get("organizationId", Long.class);
	}

	public boolean isTokenExpired(String token) {
		return extractExpiration(token).before(new Date());
	}

	private Date extractExpiration(String token) {
		return Jwts.parserBuilder().setSigningKey(secretKey).build().parseClaimsJws(token).getBody().getExpiration();
	}

	public boolean validateToken(String token, String username) {
		return (username.equals(extractUsername(token)) && !isTokenExpired(token));
	}

	public String extractToken(org.springframework.http.server.reactive.ServerHttpRequest serverHttpRequest) {
		String bearerToken = serverHttpRequest.getHeaders().getFirst(HttpHeaders.AUTHORIZATION);
		if (bearerToken != null && bearerToken.startsWith("Bearer ")) {
			return bearerToken.substring(7);
		}
		return null;
	}

	public boolean validateToken(String token) {
		try {
			Jwts.parserBuilder()
//                .setSigningKey(SECRET_KEY.getBytes(StandardCharsets.UTF_8))
					.setSigningKey(secretKey).build().parseClaimsJws(token);
			return true;
		} catch (JwtException | IllegalArgumentException e) {
			return false;
		}
	}

	public Claims getClaims(String token) {
		return Jwts.parserBuilder()
//                   .setSigningKey(SECRET_KEY.getBytes(StandardCharsets.UTF_8))
				.setSigningKey(secretKey).build().parseClaimsJws(token).getBody();
	}
}
