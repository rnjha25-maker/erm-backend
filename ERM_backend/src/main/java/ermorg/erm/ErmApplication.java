package ermorg.erm;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@ComponentScan(basePackages = {"ermorg.erm", "ermorg.storage"})
@EnableJpaAuditing
@EnableDiscoveryClient
@EnableScheduling
public class ErmApplication extends Thread{

	public static void main(String[] args) {
		SpringApplication.run(ErmApplication.class, args);
		
		
	}
	


}
