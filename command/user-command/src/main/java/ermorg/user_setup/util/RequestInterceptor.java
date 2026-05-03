package ermorg.user_setup.util;

import java.util.Optional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

import ermorg.user_setup.modal.Organization;
import ermorg.user_setup.repository.OrganizationRepository;
import ermorg.user_setup.repository.UserRepository;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

@Component
public class RequestInterceptor implements HandlerInterceptor {

    private static final Logger log = LoggerFactory.getLogger(RequestInterceptor.class);

    private final OrganizationRepository organizationRepository;
    private final UserRepository userRepository;

    public RequestInterceptor(OrganizationRepository organizationRepository, UserRepository userRepository) {
        this.organizationRepository = organizationRepository;
        this.userRepository = userRepository;
    }

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler)
            throws Exception {

        String requestURI = request.getRequestURI();
        String header = request.getHeader("X-Org-Id");

        log.info("➡️ Incoming Request: {}", requestURI);
        log.info("➡️ X-Org-Id Header: {}", header);

        // Skip swagger
        if (requestURI.contains("swagger-ui") || requestURI.contains("swagger-config")
                || requestURI.contains("swagger-resources") || requestURI.contains("api-docs")) {
            log.info("✅ Swagger request bypassed");
            return true;
        }

        // Header missing
        if (header == null) {
            log.error("❌ Missing X-Org-Id header");
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            return false;
        }

        try {
            Long orgId = Long.parseLong(header);
            log.info("➡️ Parsed Org ID: {}", orgId);

            Optional<Organization> organizationOptional = organizationRepository.findById(orgId)
                    .filter(u -> !u.getDeleted());

            if (organizationOptional.isEmpty()) {
                log.error("❌ Organization NOT FOUND or DELETED for id: {}", orgId);
            } else {
                log.info("✅ Organization FOUND: {}", organizationOptional.get().getId());
            }

            Organization organization = organizationOptional.orElse(null);
            OrganizationContext.setOrganization(organization);

        } catch (Exception e) {
            log.error("❌ Error while parsing or fetching organization: {}", e.getMessage(), e);
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            return false;
        }

        if (OrganizationContext.getOrganization() == null) {
            log.error("❌ OrganizationContext is NULL → Unauthorized");
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            return false;
        }

        log.info("✅ Request allowed for Org ID: {}", OrganizationContext.getOrganization().getId());
        return true;
    }

    @Override
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object handler,
            Exception ex) {

        log.info("🧹 Clearing OrganizationContext for request: {}", request.getRequestURI());

        if (ex != null) {
            log.error("❌ Exception occurred: {}", ex.getMessage(), ex);
        }

        OrganizationContext.clear();
    }
}