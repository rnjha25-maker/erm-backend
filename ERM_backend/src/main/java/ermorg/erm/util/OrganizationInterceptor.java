package ermorg.erm.util;

import java.util.Optional;

import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

import ermorg.erm.model.Organization;
import ermorg.erm.repository.OrganizationRepository;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

@Component
public class OrganizationInterceptor implements HandlerInterceptor {
	
	private final OrganizationRepository organizationRepository;

    public OrganizationInterceptor(OrganizationRepository organizationRepository) {
        this.organizationRepository = organizationRepository;
    }
    
    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        String header = request.getHeader("X-Org-Id");
        System.out.println("pre handle " + header);
        
        String requestURI = request.getRequestURI();
		if (requestURI.contains("swagger-ui") || requestURI.contains("swagger-config")
				|| requestURI.contains("swagger-resources") || requestURI.contains("api-docs")) {
			return true;
		}
        if (header != null) {
            Optional<Organization> organizationOptional = organizationRepository.findById(Long.parseLong(header));
            Organization organization = organizationOptional.orElse(null);
            OrganizationContext.setOrganization(organization);
            
            if(organization == null) {
            	response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
				return false;
			}
        }
        return true;
    }

    @Override
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object handler, Exception ex) {
        OrganizationContext.clear();
    }

	
}
