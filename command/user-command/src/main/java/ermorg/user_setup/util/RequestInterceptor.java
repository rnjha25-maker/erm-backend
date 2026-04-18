package ermorg.user_setup.util;

import java.util.Optional;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

import ermorg.user_setup.modal.Organization;
import ermorg.user_setup.repository.OrganizationRepository;
import ermorg.user_setup.repository.UserRepository;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

@Component
public class RequestInterceptor implements HandlerInterceptor {

	private final OrganizationRepository organizationRepository;
	private final UserRepository userRepository;

	public RequestInterceptor(OrganizationRepository organizationRepository, UserRepository userRepository) {
		this.organizationRepository = organizationRepository;
		this.userRepository = userRepository;
	}

	@Override
	public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler)
			throws Exception {
		String header = request.getHeader("X-Org-Id");

		String requestURI = request.getRequestURI();
		if (requestURI.contains("swagger-ui") || requestURI.contains("swagger-config")
				|| requestURI.contains("swagger-resources") || requestURI.contains("api-docs")) {
			return true;
		}
		if (header == null) {
			response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
			return false;
		}

		if (header != null) {
			Optional<Organization> organizationOptional = organizationRepository.findById(Long.parseLong(header))
					.filter(u -> !u.getDeleted());
			Organization organization = organizationOptional.orElse(null);
			OrganizationContext.setOrganization(organization);
		}

		if (OrganizationContext.getOrganization() == null) {
			response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
			return false;
		}

		return true;
	}

	@Override
	public void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object handler,
			Exception ex) {
		OrganizationContext.clear();
	}

}
