package com.evenlo.config;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

@Validated
@ConfigurationProperties(prefix = "app")
public class AppProperties {
	@Valid
	@NotNull
	private Security security = new Security();

	@Valid
	@NotNull
	private Integration integration = new Integration();

	@Valid
	@NotNull
	private File file = new File();

	@Valid
	@NotNull
	private Redis redis = new Redis();

	@Valid
	@NotNull
	private RateLimit rateLimit = new RateLimit();

	public Security getSecurity() {
		return security;
	}

	public void setSecurity(Security security) {
		this.security = security;
	}

	public Integration getIntegration() {
		return integration;
	}

	public void setIntegration(Integration integration) {
		this.integration = integration;
	}

	public File getFile() {
		return file;
	}

	public void setFile(File file) {
		this.file = file;
	}

	public Redis getRedis() {
		return redis;
	}

	public void setRedis(Redis redis) {
		this.redis = redis;
	}

	public RateLimit getRateLimit() {
		return rateLimit;
	}

	public void setRateLimit(RateLimit rateLimit) {
		this.rateLimit = rateLimit;
	}

	public static class Security {
		@Valid
		@NotNull
		private Jwt jwt = new Jwt();

		public Jwt getJwt() {
			return jwt;
		}

		public void setJwt(Jwt jwt) {
			this.jwt = jwt;
		}

		public static class Jwt {
			@NotBlank
			private String issuer;

			@NotBlank
			private String secret;

			@Positive
			private long accessTokenTtlSeconds;

			public String getIssuer() {
				return issuer;
			}

			public void setIssuer(String issuer) {
				this.issuer = issuer;
			}

			public String getSecret() {
				return secret;
			}

			public void setSecret(String secret) {
				this.secret = secret;
			}

			public long getAccessTokenTtlSeconds() {
				return accessTokenTtlSeconds;
			}

			public void setAccessTokenTtlSeconds(long accessTokenTtlSeconds) {
				this.accessTokenTtlSeconds = accessTokenTtlSeconds;
			}
		}
	}

	public static class Integration {
		@Valid
		@NotNull
		private Razorpay razorpay = new Razorpay();

		public Razorpay getRazorpay() {
			return razorpay;
		}

		public void setRazorpay(Razorpay razorpay) {
			this.razorpay = razorpay;
		}

		public static class Razorpay {
			@NotBlank
			private String baseUrl;

			@NotBlank
			private String keyId;

			@NotBlank
			private String keySecret;

			public String getBaseUrl() {
				return baseUrl;
			}

			public void setBaseUrl(String baseUrl) {
				this.baseUrl = baseUrl;
			}

			public String getKeyId() {
				return keyId;
			}

			public void setKeyId(String keyId) {
				this.keyId = keyId;
			}

			public String getKeySecret() {
				return keySecret;
			}

			public void setKeySecret(String keySecret) {
				this.keySecret = keySecret;
			}
		}
	}

	public static class File {
		@NotBlank
		private String storageDir;

		public String getStorageDir() {
			return storageDir;
		}

		public void setStorageDir(String storageDir) {
			this.storageDir = storageDir;
		}
	}

	public static class Redis {
		@Positive
		private long hotAvailabilityTtlSeconds;

		public long getHotAvailabilityTtlSeconds() {
			return hotAvailabilityTtlSeconds;
		}

		public void setHotAvailabilityTtlSeconds(long hotAvailabilityTtlSeconds) {
			this.hotAvailabilityTtlSeconds = hotAvailabilityTtlSeconds;
		}
	}

	public static class RateLimit {
		private boolean enabled;

		@Positive
		private long capacity;

		@Positive
		private long refillTokens;

		@Positive
		private long refillPeriodSeconds;

		public boolean isEnabled() {
			return enabled;
		}

		public void setEnabled(boolean enabled) {
			this.enabled = enabled;
		}

		public long getCapacity() {
			return capacity;
		}

		public void setCapacity(long capacity) {
			this.capacity = capacity;
		}

		public long getRefillTokens() {
			return refillTokens;
		}

		public void setRefillTokens(long refillTokens) {
			this.refillTokens = refillTokens;
		}

		public long getRefillPeriodSeconds() {
			return refillPeriodSeconds;
		}

		public void setRefillPeriodSeconds(long refillPeriodSeconds) {
			this.refillPeriodSeconds = refillPeriodSeconds;
		}
	}
}
