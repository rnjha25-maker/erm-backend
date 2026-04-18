package ermorg.erm.util;

import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

import ermorg.erm.model.User;
import ermorg.erm.repository.UserRepository;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

@Component
public class UserInterceptor implements HandlerInterceptor{
	


	private final UserRepository userRepository;

    public UserInterceptor(UserRepository userRepository) {
        this.userRepository = userRepository;
    }
    
    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        String header = request.getHeader("X-user-Id");
        
        String requestURI = request.getRequestURI();
		if (requestURI.contains("swagger-ui") || requestURI.contains("swagger-config")
				|| requestURI.contains("swagger-resources") || requestURI.contains("api-docs")) {
			return true;
		}
        if (header != null) {
            User user = userRepository.findById(Long.parseLong(header))
            .filter(cmp -> !cmp.getDeleted()).orElse(null);
            UserContext.seetUser(user);
            
            if(user == null) {
            	response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
				return false;
			}
        }
        return true;
    }

    @Override
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object handler, Exception ex) {
    	CompanyContext.clear();
    }


}
