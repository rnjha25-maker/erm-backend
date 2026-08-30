package ermorg.storage.util;

import org.springframework.context.annotation.Configuration;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration("storageWebConfig")
public class WebConfig implements WebMvcConfigurer {

    private final OrganizationInterceptor organizationInterceptor;

    public WebConfig(@Qualifier("storageOrganizationInterceptor") OrganizationInterceptor organizationInterceptor) {
        this.organizationInterceptor = organizationInterceptor;
    }

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(organizationInterceptor);
    }

}
