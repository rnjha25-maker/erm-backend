package com.erm.util;

import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.AuditorAware;
import org.springframework.stereotype.Component;

import com.erm.model.BaseModel;
import com.erm.model.Organization;
import com.erm.model.User;
import com.erm.repository.UserRepository;

import jakarta.servlet.http.HttpServletRequest;
@Component
public class AuditorAwareImpl implements AuditorAware<User> {

	@Autowired
    private HttpServletRequest request;
	
	@Autowired
	private UserRepository userRepository;
	
	
	public static ThreadLocal<Organization> threadLocal = new ThreadLocal<>();
	
	@Override
	public Optional<User> getCurrentAuditor() {
		String header = request.getHeader("X-User-Id");
		String ipAddress = request.getHeader("X-Forwarded-For");
		if(header == null) return Optional.empty();
		
		 Optional<User> userOptional = userRepository.findById(Long.parseLong(header));
		 User user = userOptional.get();
		 
		 if(user == null) return Optional.empty();
		 
//		 user.setIp(ipAddress);
		 
		 return Optional.of(user);
	}
	


	public String getClientIp(BaseModel entity) {
        String ipAddress = request.getHeader("X-Forwarded-For");
        if (ipAddress == null || ipAddress.isEmpty()) {
            ipAddress = request.getRemoteAddr();
        }
        entity.setClientIP(ipAddress);
        return ipAddress;
    }

}
