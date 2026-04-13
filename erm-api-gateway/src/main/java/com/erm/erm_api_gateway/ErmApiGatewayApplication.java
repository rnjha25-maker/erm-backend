package com.erm.erm_api_gateway;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;

@EnableDiscoveryClient
//@EnableCaching
@SpringBootApplication
@EnableJpaAuditing
public class ErmApiGatewayApplication {

	public static void main(String[] args) {
		SpringApplication.run(ErmApiGatewayApplication.class, args);

	}

}
