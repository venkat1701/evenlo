package com.evenlo.service;

import com.evenlo.config.AppProperties;
import com.evenlo.dto.file.FileUploadResponse;
import com.evenlo.exception.BadRequestException;
import com.evenlo.exception.NotFoundException;
import com.evenlo.model.StoredFile;
import com.evenlo.repository.StoredFileRepository;
import com.evenlo.util.SecurityUtil;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.UUID;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

@Service
public class FileStorageService {
	private final AppProperties props;
	private final StoredFileRepository storedFileRepository;

	public FileStorageService(AppProperties props, StoredFileRepository storedFileRepository) {
		this.props = props;
		this.storedFileRepository = storedFileRepository;
	}

	@Transactional
	public FileUploadResponse upload(MultipartFile file) {
		if (file == null || file.isEmpty()) {
			throw new BadRequestException("file_required");
		}
		String contentType = file.getContentType() == null ? "application/octet-stream" : file.getContentType();
		UUID userId = SecurityUtil.currentUserId();

		UUID storedFileId = UUID.randomUUID();
		Path storageDir = Path.of(props.getFile().getStorageDir()).toAbsolutePath().normalize();
		try {
			Files.createDirectories(storageDir);
			String safeName = storedFileId + "-" + sanitize(file.getOriginalFilename());
			Path target = storageDir.resolve(safeName);
			Files.copy(file.getInputStream(), target, StandardCopyOption.REPLACE_EXISTING);

			StoredFile stored = new StoredFile();
			stored.setId(storedFileId);
			stored.setOriginalFilename(file.getOriginalFilename() == null ? safeName : file.getOriginalFilename());
			stored.setContentType(contentType);
			stored.setSizeBytes(file.getSize());
			stored.setStoragePath(target.toString());
			stored.setUploadedBy(userId);
			storedFileRepository.save(stored);

			return new FileUploadResponse(stored.getId(), stored.getOriginalFilename(), stored.getContentType(), stored.getSizeBytes(), "/api/v1/files/" + stored.getId());
		} catch (IOException ex) {
			throw new IllegalStateException("file_upload_failed", ex);
		}
	}

	@Transactional(readOnly = true)
	public Resource load(UUID id) {
		StoredFile stored = storedFileRepository.findById(id).orElseThrow(() -> new NotFoundException("file_not_found"));
		return new FileSystemResource(stored.getStoragePath());
	}

	@Transactional(readOnly = true)
	public StoredFile metadata(UUID id) {
		return storedFileRepository.findById(id).orElseThrow(() -> new NotFoundException("file_not_found"));
	}

	private String sanitize(String filename) {
		if (filename == null || filename.isBlank()) {
			return "upload";
		}
		String trimmed = filename.trim();
		return trimmed.replaceAll("[^a-zA-Z0-9._-]", "_");
	}
}
