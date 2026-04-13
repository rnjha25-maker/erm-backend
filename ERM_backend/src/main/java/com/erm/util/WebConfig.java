package com.erm.util;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class WebConfig implements WebMvcConfigurer {

    private final OrganizationInterceptor organizationInterceptor;
    private UserInterceptor userInterceptor;
    private final CompanyIntercepter companyIntercepter;
    public WebConfig(OrganizationInterceptor organizationInterceptor, CompanyIntercepter companyIntercepter, UserInterceptor userInterceptor) {
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
