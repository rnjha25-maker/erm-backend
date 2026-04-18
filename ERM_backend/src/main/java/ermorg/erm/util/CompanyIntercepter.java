package ermorg.erm.util;

import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

import ermorg.erm.model.Company;
import ermorg.erm.repository.CompanyRepository;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
@Component
public class CompanyIntercepter implements HandlerInterceptor{

	private final CompanyRepository companyRepository;

    public CompanyIntercepter(CompanyRepository companyRepository) {
        this.companyRepository = companyRepository;
    }
    
    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        String header = request.getHeader("X-Cmp-Id");
        
        String requestURI = request.getRequestURI();
		if (requestURI.contains("swagger-ui") || requestURI.contains("swagger-config")
				|| requestURI.contains("swagger-resources") || requestURI.contains("api-docs")) {
			return true;
		}
        if (header != null) {
            Company company = companyRepository.findById(Long.parseLong(header))
            .filter(cmp -> !cmp.getDeleted()).orElse(null);
            CompanyContext.setCompany(company);
            
            if(company == null) {
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
