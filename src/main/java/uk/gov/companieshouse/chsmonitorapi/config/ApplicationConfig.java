package uk.gov.companieshouse.chsmonitorapi.config;

import static uk.gov.companieshouse.chsmonitorapi.ChsMonitorApiApplication.APPLICATION_NAME_SPACE;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;
import uk.gov.companieshouse.chsmonitorapi.logging.RequestLogInterceptor;
import uk.gov.companieshouse.chsmonitorapi.interceptor.AuthenticationInterceptor;
import uk.gov.companieshouse.environment.EnvironmentReader;
import uk.gov.companieshouse.logging.Logger;
import uk.gov.companieshouse.logging.LoggerFactory;

@Configuration
public class ApplicationConfig implements WebMvcConfigurer {
        
    private final AuthenticationInterceptor authenticationInterceptor;

    @Autowired
    public ApplicationConfig(AuthenticationInterceptor authenticationInterceptor) {
        this.authenticationInterceptor = authenticationInterceptor;
    }

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(authenticationInterceptor);
        registry.addInterceptor(new RequestLogInterceptor(getLogger()));
    }

    @Bean
    public Logger getLogger() {
        return LoggerFactory.getLogger(APPLICATION_NAME_SPACE);
    }
}
