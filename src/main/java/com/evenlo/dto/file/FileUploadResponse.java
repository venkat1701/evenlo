package com.evenlo.dto.file;

import java.util.UUID;

public record FileUploadResponse(
		UUID id,
		String originalFilename,
		String contentType,
		long sizeBytes,
		String downloadUrl
) {
}
