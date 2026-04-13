package com.erm.erm_query_organization;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;

@EnableDiscoveryClient
@EnableCaching
@SpringBootApplication
public class ErmQueryOrganizationApplication {

	public static void main(String[] args) {
		SpringApplication.run(ErmQueryOrganizationApplication.class, args);
	}

}
