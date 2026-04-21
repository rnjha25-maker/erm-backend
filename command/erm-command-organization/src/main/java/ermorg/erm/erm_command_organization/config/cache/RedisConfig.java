package ermorg.erm.erm_command_organization.config.cache;

 
public class RedisConfig {
	
	/*
	 * 
	 * public RedisCacheConfiguration cacheConfiguration() {
	 * Jackson2JsonRedisSerializer<Object> serializer = new
	 * Jackson2JsonRedisSerializer<>(Object.class);
	 * 
	 * ObjectMapper objectMapper = new ObjectMapper();
	 * objectMapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
	 * objectMapper.enable(DeserializationFeature.ACCEPT_SINGLE_VALUE_AS_ARRAY);
	 * 
	 * objectMapper.activateDefaultTyping(LaissezFaireSubTypeValidator.instance,
	 * ObjectMapper.DefaultTyping.NON_FINAL);
	 * 
	 * serializer.setObjectMapper(objectMapper);
	 * 
	 * return RedisCacheConfiguration.defaultCacheConfig() .serializeValuesWith(
	 * RedisSerializationContext.SerializationPair.fromSerializer(serializer) )
	 * .serializeKeysWith(
	 * RedisSerializationContext.SerializationPair.fromSerializer(new
	 * StringRedisSerializer()) ); }
	 */

}
