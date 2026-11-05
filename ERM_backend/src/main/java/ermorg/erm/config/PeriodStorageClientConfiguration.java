package ermorg.erm.config;

import org.springframework.context.annotation.*;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.web.client.RestTemplate;

/** Bound network time while an evidence mutation holds the evaluation lock. Legacy client unchanged. */
@Configuration
public class PeriodStorageClientConfiguration {
    @Bean
    RestTemplate periodStorageRestTemplate(
            @Value("${erm.period-evidence.connect-timeout-ms:5000}") int connect,
            @Value("${erm.period-evidence.read-timeout-ms:15000}") int read) {
        if(connect<=0||read<=0)throw new IllegalArgumentException("Period storage timeouts must be positive.");
        var factory=new SimpleClientHttpRequestFactory();factory.setConnectTimeout(connect);factory.setReadTimeout(read);
        return new RestTemplate(factory);
    }
}
