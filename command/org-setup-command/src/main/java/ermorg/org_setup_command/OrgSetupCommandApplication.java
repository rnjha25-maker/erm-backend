package ermorg.org_setup_command;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;

@EnableCaching
@EnableDiscoveryClient
@EnableJpaAuditing
@SpringBootApplication
public class OrgSetupCommandApplication {

	public static void main(String[] args) {
		SpringApplication.run(OrgSetupCommandApplication.class, args);
	}

}
