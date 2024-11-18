package uk.gov.companieshouse.chsmonitorapi;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class ChsMonitorApiApplication {

    public static final String APPLICATION_NAME_SPACE = "chs-monitor-api";

    public static void main(String[] args) {
        SpringApplication.run(ChsMonitorApiApplication.class, args);
    }

}
