package uk.gov.companieshouse.chsmonitorapi.config;

import static uk.gov.companieshouse.chsmonitorapi.ChsMonitorApiApplication.APPLICATION_NAME_SPACE;

import java.util.Optional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import uk.gov.companieshouse.environment.EnvironmentReader;
import uk.gov.companieshouse.logging.Logger;
import uk.gov.companieshouse.logging.LoggerFactory;

@Configuration
public class ApplicationConfig {

    private final EnvironmentReader environmentReader;

    @Autowired
    public ApplicationConfig(EnvironmentReader environmentReader) {
        this.environmentReader = environmentReader;
    }

    @Bean
    public Logger getLogger() {
        return LoggerFactory.getLogger(APPLICATION_NAME_SPACE);
    }


    // Mandatory env vars
    @Bean
    public String getBindUrl() {
        return environmentReader.getMandatoryString("BIND_URL");
    }

    @Bean
    public String getCertFile() {
        return environmentReader.getMandatoryString("CERT_FILE");
    }

    @Bean
    public String getApiLocalUrl() {
        return environmentReader.getMandatoryUrl("API_LOCAL_URL");
    }

    @Bean
    public String getChsInternalApiKey() {
        return environmentReader.getMandatoryString("CHS_INTERNAL_API_KEY");
    }

    @Bean
    public String getEricUrl() {
        return environmentReader.getMandatoryUrl("ERIC_URL");
    }

    @Bean
    public String getKeyFile() {
        return environmentReader.getMandatoryString("KEY_FILE");
    }

    @Bean
    public String getMongoUrl() {
        return environmentReader.getMandatoryUrl("MONG_URL");
    }

    @Bean
    public String getMongoDatabase() {
        return environmentReader.getMandatoryString("MONGO_DATABASE");
    }

    @Bean
    public String getMongoCollection() {
        return environmentReader.getMandatoryString("MONGO_COLLECTION");
    }

    @Bean
    public int getMongoTimeout() {
        return environmentReader.getMandatoryInteger("MONGO_TIMEOUT");
    }

    //Optional env vars
    @Bean
    public Optional<String> getDeveloperUrl() {
        return Optional.ofNullable(environmentReader.getOptionalUrl("DEVELOPER_URL"));
    }

}
