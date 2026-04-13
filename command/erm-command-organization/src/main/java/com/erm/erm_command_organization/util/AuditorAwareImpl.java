package com.erm.erm_command_organization.util;

import com.erm.erm_command_organization.model.Organization;
import com.erm.erm_command_organization.repository.OrganizationRepository;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.AuditorAware;
import org.springframework.stereotype.Component;

import java.util.Optional;

@Component
public class AuditorAwareImpl implements AuditorAware<Organization> {
    @Autowired
    private HttpServletRequest request;

    @Autowired
    private OrganizationRepository organizationRepository;

    @Override
    public Optional<Organization> getCurrentAuditor(){
//        return organizationRepository.findById(Long.parseLong(request.getParameter("id")));
    return Optional.empty();
    }

    public String getClientIp() {
        String ipAddress = request.getHeader("X-Forwarded-For");
        if (ipAddress == null || ipAddress.isEmpty()) {
            ipAddress = request.getRemoteAddr();
        }
        return ipAddress;
    }
}
