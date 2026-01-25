package com.evenlo.util;

import com.evenlo.exception.ForbiddenException;
import java.util.Optional;
import java.util.UUID;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.jwt.Jwt;

public final class SecurityUtil {
	private SecurityUtil() {
	}

	public static UUID currentUserId() {
		Authentication auth = SecurityContextHolder.getContext().getAuthentication();
		if (auth == null || !(auth.getPrincipal() instanceof Jwt jwt)) {
			throw new ForbiddenException("unauthenticated");
		}
		String uid = jwt.getClaimAsString("uid");
		if (uid == null || uid.isBlank()) {
			throw new ForbiddenException("missing_uid");
		}
		return UUID.fromString(uid);
	}

	public static boolean hasRole(String role) {
		Authentication auth = SecurityContextHolder.getContext().getAuthentication();
		if (auth == null) {
			return false;
		}
		return auth.getAuthorities().stream().anyMatch(a -> a.getAuthority().equals(role));
	}

	public static Optional<String> currentEmail() {
		Authentication auth = SecurityContextHolder.getContext().getAuthentication();
		if (auth == null || !(auth.getPrincipal() instanceof Jwt jwt)) {
			return Optional.empty();
		}
		return Optional.ofNullable(jwt.getClaimAsString("email"));
	}
}
