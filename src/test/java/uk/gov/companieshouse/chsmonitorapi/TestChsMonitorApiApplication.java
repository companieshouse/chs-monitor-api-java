package uk.gov.companieshouse.chsmonitorapi;

import org.springframework.boot.SpringApplication;

public class TestChsMonitorApiApplication {

	public static void main(String[] args) {
		SpringApplication.from(ChsMonitorApiApplication::main).with(TestcontainersConfiguration.class).run(args);
	}

}
