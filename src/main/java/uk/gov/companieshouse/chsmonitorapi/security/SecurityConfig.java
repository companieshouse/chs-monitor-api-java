package uk.gov.companieshouse.chsmonitorapi.security;

import static uk.gov.companieshouse.csrf.config.ChsCsrfMitigationHttpSecurityBuilder.configureApiCsrfMitigations;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.www.BasicAuthenticationFilter;
import uk.gov.companieshouse.auth.filter.UserAuthFilter;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    @Order(1)
    @Bean
    public SecurityFilterChain healthCheckFilterChain(HttpSecurity http) throws Exception {
        return http.securityMatcher("/chs-monitor-api/healthcheck").build();
    }

//    @Order(2)
//    @Bean
//    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
//        return configureApiCsrfMitigations(http.addFilterBefore(new UserAuthFilter(),
//                BasicAuthenticationFilter.class)).build();
//    }
}
