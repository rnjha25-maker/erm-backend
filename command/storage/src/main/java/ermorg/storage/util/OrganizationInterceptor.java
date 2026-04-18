package ermorg.storage.util;

import java.util.Optional;

import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

import ermorg.storage.model.Organization;
import ermorg.storage.repository.OrganizationRepository;

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
        if (header != null) {
            Optional<Organization> organizationOptional = organizationRepository.findById(Long.parseLong(header));
            Organization organization = organizationOptional.orElse(null);
            OrganizationContext.setOrganization(organization);
        }
        return true;
    }

    @Override
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object handler, Exception ex) {
        OrganizationContext.clear();
    }

	
}
