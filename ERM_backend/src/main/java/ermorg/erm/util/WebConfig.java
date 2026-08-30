package ermorg.erm.util;

import org.springframework.context.annotation.Configuration;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration("ermWebConfig")
public class WebConfig implements WebMvcConfigurer {

    private final OrganizationInterceptor organizationInterceptor;
    private UserInterceptor userInterceptor;
    private final CompanyIntercepter companyIntercepter;
    public WebConfig(@Qualifier("ermOrganizationInterceptor") OrganizationInterceptor organizationInterceptor, CompanyIntercepter companyIntercepter, UserInterceptor userInterceptor) {
        this.organizationInterceptor = organizationInterceptor;
        this.companyIntercepter = companyIntercepter;
        this.userInterceptor = userInterceptor;
    }

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(organizationInterceptor);
        registry.addInterceptor(companyIntercepter);
        registry.addInterceptor(userInterceptor);
    }

}
