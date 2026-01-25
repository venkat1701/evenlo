package com.evenlo.config;

import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RateLimitingConfig {
	@Bean
	RateLimitingFilter rateLimitingFilter(StringRedisTemplate redisTemplate, AppProperties props) {
		return new RateLimitingFilter(redisTemplate, props);
	}
}
