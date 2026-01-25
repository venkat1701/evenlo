package com.evenlo.service;

import com.evenlo.config.AppProperties;
import com.evenlo.model.PlatformRole;
import com.evenlo.model.User;
import java.time.Instant;
import java.util.List;
import org.springframework.security.oauth2.jwt.JwtClaimsSet;
import org.springframework.security.oauth2.jwt.JwtEncoder;
import org.springframework.security.oauth2.jwt.JwtEncoderParameters;
import org.springframework.stereotype.Service;

@Service
public class JwtService {
	private final JwtEncoder encoder;
	private final AppProperties props;

	public JwtService(JwtEncoder encoder, AppProperties props) {
		this.encoder = encoder;
		this.props = props;
	}

	public String createAccessToken(User user) {
		Instant now = Instant.now();
		Instant exp = now.plusSeconds(props.getSecurity().getJwt().getAccessTokenTtlSeconds());

		List<String> roles = List.of(toRole(user.getPlatformRole()));
		JwtClaimsSet claims = JwtClaimsSet.builder()
				.issuer(props.getSecurity().getJwt().getIssuer())
				.issuedAt(now)
				.expiresAt(exp)
				.subject(user.getId().toString())
				.claim("uid", user.getId().toString())
				.claim("email", user.getEmail())
				.claim("roles", roles)
				.build();

		return encoder.encode(JwtEncoderParameters.from(claims)).getTokenValue();
	}

	private String toRole(PlatformRole role) {
		return switch (role) {
			case PLATFORM_ADMIN -> "PLATFORM_ADMIN";
			case USER -> "USER";
		};
	}
}
