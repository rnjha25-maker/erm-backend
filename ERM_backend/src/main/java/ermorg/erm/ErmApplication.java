package ermorg.erm;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.FilterType;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@ComponentScan(
		basePackages = {"ermorg.erm", "ermorg.storage"},
		excludeFilters = @ComponentScan.Filter(
				type = FilterType.ASSIGNABLE_TYPE,
				classes = ermorg.storage.StorageApplication.class))
@EnableJpaAuditing
@EnableJpaRepositories(basePackages = {"ermorg.erm.repository", "ermorg.erm.service", "ermorg.storage.repository"})
@EntityScan(basePackages = {"ermorg.erm.model", "ermorg.storage.model"})
@EnableDiscoveryClient
@EnableScheduling
public class ErmApplication extends Thread{

	public static void main(String[] args) {
		SpringApplication.run(ErmApplication.class, args);
		
		
	}
	


}
