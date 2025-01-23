package uk.gov.companieshouse.chsmonitorapi.config;

import static uk.gov.companieshouse.chsmonitorapi.ChsMonitorApiApplication.APPLICATION_NAME_SPACE;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import uk.gov.companieshouse.logging.Logger;
import uk.gov.companieshouse.logging.LoggerFactory;

@Configuration
public class LoggingConfig {

    @Bean
    public Logger getLogger() {
        return LoggerFactory.getLogger(APPLICATION_NAME_SPACE);
    }
}
