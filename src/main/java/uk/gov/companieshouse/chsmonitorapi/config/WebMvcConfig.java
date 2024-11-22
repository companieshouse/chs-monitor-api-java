package uk.gov.companieshouse.chsmonitorapi.config;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;
import uk.gov.companieshouse.chsmonitorapi.interceptor.AuthenticationInterceptor;
import uk.gov.companieshouse.chsmonitorapi.logging.RequestLogInterceptor;
import uk.gov.companieshouse.logging.Logger;

@Configuration
public class WebMvcConfig implements WebMvcConfigurer {

    private final AuthenticationInterceptor authenticationInterceptor;
    private final Logger logger;

    @Autowired
    public WebMvcConfig(AuthenticationInterceptor authenticationInterceptor, Logger logger) {
        this.authenticationInterceptor = authenticationInterceptor;
        this.logger = logger;
    }

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(authenticationInterceptor);
        registry.addInterceptor(new RequestLogInterceptor(logger));
    }

}
