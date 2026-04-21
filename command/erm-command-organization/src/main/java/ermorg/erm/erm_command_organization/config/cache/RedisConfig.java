package ermorg.erm.erm_command_organization.config.cache;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.cache.RedisCacheConfiguration;
import org.springframework.data.redis.serializer.Jackson2JsonRedisSerializer;
import org.springframework.data.redis.serializer.RedisSerializationContext;
import org.springframework.data.redis.serializer.StringRedisSerializer;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.databind.jsontype.impl.LaissezFaireSubTypeValidator;

@Configuration
@ConditionalOnProperty(
    name = "app.redis.enabled",
    havingValue = "true",
    matchIfMissing = false
)
public class RedisConfig {
	
	
	@SuppressWarnings("removal")
	@Bean
	public RedisCacheConfiguration cacheConfiguration() {
	    Jackson2JsonRedisSerializer<Object> serializer = new Jackson2JsonRedisSerializer<>(Object.class);

	    ObjectMapper objectMapper = new ObjectMapper();
	    objectMapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
	    objectMapper.enable(DeserializationFeature.ACCEPT_SINGLE_VALUE_AS_ARRAY);

	    objectMapper.activateDefaultTyping(LaissezFaireSubTypeValidator.instance, ObjectMapper.DefaultTyping.NON_FINAL);

	    serializer.setObjectMapper(objectMapper);

	    return RedisCacheConfiguration.defaultCacheConfig()
	            .serializeValuesWith(
	                    RedisSerializationContext.SerializationPair.fromSerializer(serializer)
	            )
	            .serializeKeysWith(
	                    RedisSerializationContext.SerializationPair.fromSerializer(new StringRedisSerializer())
	            );
	}


}
