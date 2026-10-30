package com.tokoku.product.infrastructure.config;

import java.time.Duration;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.cache.RedisCacheConfiguration;
import org.springframework.data.redis.serializer.GenericJacksonJsonRedisSerializer;
import org.springframework.data.redis.serializer.RedisSerializationContext.SerializationPair;

/**
 * Cache di config terpisah, bukan di class main (slide 144). Provider dipaksa lewat spring.cache.type:
 * caffeine (default) atau redis (Spring profile "redis").
 */
@Configuration(proxyBeanMethods = false)
@EnableCaching
class CacheConfig {

	/**
	 * Redis menyimpan byte: record Product diserialisasi sebagai JSON, bukan Java serialization. Bean ini dipakai
	 * Boot sebagai konfigurasi semua cache, termasuk yang dibuat dari spring.cache.cache-names.
	 */
	@Bean
	@ConditionalOnProperty(name = "spring.cache.type", havingValue = "redis")
	RedisCacheConfiguration redisCacheConfiguration() {
		return RedisCacheConfiguration.defaultCacheConfig()
			.entryTtl(Duration.ofMinutes(10))
			.serializeValuesWith(SerializationPair.fromSerializer(GenericJacksonJsonRedisSerializer.builder()
				.enableUnsafeDefaultTyping()
				.build()));
	}

}
