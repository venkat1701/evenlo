package com.evenlo.service;

import com.evenlo.config.AppProperties;
import java.time.Duration;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

@Service
public class HotAvailabilityService {
	private final StringRedisTemplate redis;
	private final AppProperties props;

	public HotAvailabilityService(StringRedisTemplate redis, AppProperties props) {
		this.redis = redis;
		this.props = props;
	}

	public Optional<Integer> getAvailable(UUID eventId, String category) {
		String value = redis.opsForValue().get(key(eventId, category));
		if (value == null) {
			return Optional.empty();
		}
		try {
			return Optional.of(Integer.parseInt(value));
		} catch (NumberFormatException ex) {
			return Optional.empty();
		}
	}

	public void putAvailable(UUID eventId, String category, int available) {
		redis.opsForValue().set(
				key(eventId, category),
				Integer.toString(available),
				Duration.ofSeconds(props.getRedis().getHotAvailabilityTtlSeconds())
		);
	}

	public void evict(UUID eventId, String category) {
		redis.delete(key(eventId, category));
	}

	private String key(UUID eventId, String category) {
		return "evenlo:availability:" + eventId + ":" + category.toLowerCase();
	}
}
