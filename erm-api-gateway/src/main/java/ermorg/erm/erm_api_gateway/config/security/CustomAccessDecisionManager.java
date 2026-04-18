package ermorg.erm.erm_api_gateway.config.security;

import java.util.Collection;
import java.util.function.Supplier;

import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.access.ConfigAttribute;
import org.springframework.security.authentication.InsufficientAuthenticationException;
import org.springframework.security.authorization.AuthorizationDecision;
import org.springframework.security.authorization.AuthorizationManager;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.stereotype.Component;

@Component
public class CustomAccessDecisionManager implements AuthorizationManager<AuthorizationDecision> {

	public void decide(Authentication authentication, Object object, Collection<ConfigAttribute> configAttributes)
			throws AccessDeniedException, InsufficientAuthenticationException {
		for (ConfigAttribute attribute : configAttributes) {
			String requiredRight = attribute.getAttribute();
			boolean hasRight = authentication.getAuthorities().stream()
					.anyMatch(grantedAuthority -> grantedAuthority.getAuthority().equals(requiredRight));

			if (!hasRight) {
				throw new AccessDeniedException("Access Denied!");
			}
		}

	}

	public boolean supports(ConfigAttribute attribute) {
		// TODO Auto-generated method stub
		return false;
	}

	public boolean supports(Class<?> clazz) {
		// TODO Auto-generated method stub
		return false;
	}
////////////////

	@Override
	public AuthorizationDecision check(Supplier<Authentication> authentication, AuthorizationDecision object) {

		Authentication auth = authentication.get();

		if (auth == null || !auth.isAuthenticated()) {
			return new AuthorizationDecision(false);
		}

		// Example logic: Check if the user has a specific authority
		// You can replace this with your database logic
		Collection<? extends GrantedAuthority> authorities = auth.getAuthorities();
		boolean hasPermission = authorities.stream()
				.anyMatch(grantedAuthority -> "MANAGE_USERS".equals(grantedAuthority.getAuthority()));

		return new AuthorizationDecision(hasPermission);
	}

}
