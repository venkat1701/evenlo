package com.evenlo.config;

import com.evenlo.util.SecurityUtil;
import jakarta.servlet.FilterChain;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.time.Instant;
import java.util.List;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.script.DefaultRedisScript;
import org.springframework.data.redis.core.script.RedisScript;
import org.springframework.http.HttpStatus;
import org.springframework.web.filter.OncePerRequestFilter;

public class RateLimitingFilter extends OncePerRequestFilter {
	private final StringRedisTemplate redisTemplate;
	private final AppProperties props;
	private final RedisScript<Long> incrementScript;

	public RateLimitingFilter(StringRedisTemplate redisTemplate, AppProperties props) {
		this.redisTemplate = redisTemplate;
		this.props = props;
		this.incrementScript = new DefaultRedisScript<>(
				"local current = redis.call('INCR', KEYS[1]); " +
						"if tonumber(current) == 1 then redis.call('EXPIRE', KEYS[1], ARGV[1]); end; " +
						"return current;",
				Long.class
		);
	}

	@Override
	protected boolean shouldNotFilter(HttpServletRequest request) {
		String path = request.getRequestURI();
		return !props.getRateLimit().isEnabled()
				|| path.startsWith("/v3/api-docs")
				|| path.startsWith("/swagger-ui")
				|| path.startsWith("/actuator/health")
				|| path.startsWith("/api/v1/auth/");
	}

	@Override
	protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws java.io.IOException, jakarta.servlet.ServletException {
		long capacity = props.getRateLimit().getCapacity();
		long ttlSeconds = props.getRateLimit().getRefillPeriodSeconds();
		String key = buildWindowedKey(resolveKey(request), ttlSeconds);

		Long current = redisTemplate.execute(incrementScript, List.of(key), Long.toString(ttlSeconds));
		long currentValue = current == null ? capacity + 1 : current;
		long remaining = Math.max(0, capacity - currentValue);

		response.setHeader("X-RateLimit-Limit", Long.toString(capacity));
		response.setHeader("X-RateLimit-Remaining", Long.toString(remaining));

		if (currentValue <= capacity) {
			filterChain.doFilter(request, response);
			return;
		}

		response.setStatus(HttpStatus.TOO_MANY_REQUESTS.value());
		response.setContentType("application/json");
		response.getWriter().write("{\"message\":\"rate_limited\"}");
	}

	private String resolveKey(HttpServletRequest request) {
		try {
			return "user:" + SecurityUtil.currentUserId();
		} catch (Exception ex) {
			String forwarded = request.getHeader("X-Forwarded-For");
			String ip = forwarded != null && !forwarded.isBlank() ? forwarded.split(",")[0].trim() : request.getRemoteAddr();
			return "ip:" + ip;
		}
	}

	private String buildWindowedKey(String subject, long windowSeconds) {
		long now = Instant.now().getEpochSecond();
		long window = now / windowSeconds;
		return "evenlo:ratelimit:" + subject + ":" + window;
	}
}
