package uk.gov.companieshouse.chsmonitorapi.config;

import static uk.gov.companieshouse.chsmonitorapi.ChsMonitorApiApplication.APPLICATION_NAME_SPACE;

import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import uk.gov.companieshouse.logging.Logger;
import uk.gov.companieshouse.logging.LoggerFactory;

@TestConfiguration
public class TestApplicationConfig {

    @Bean
    public Logger getLogger() {
        return LoggerFactory.getLogger(APPLICATION_NAME_SPACE);
    }
}
