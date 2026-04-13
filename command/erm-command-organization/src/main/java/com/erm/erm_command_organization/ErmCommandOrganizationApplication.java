package com.erm.erm_command_organization;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;

@EnableDiscoveryClient
@SpringBootApplication
@EnableJpaAuditing
public class ErmCommandOrganizationApplication {

	public static void main(String[] args) {
		SpringApplication.run(ErmCommandOrganizationApplication.class, args);
	}

}
