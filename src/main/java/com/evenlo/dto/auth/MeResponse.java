package com.evenlo.dto.auth;

import com.evenlo.model.PlatformRole;
import java.util.UUID;

public record MeResponse(
		UUID id,
		String email,
		String displayName,
		PlatformRole platformRole
) {
}
