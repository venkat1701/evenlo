package com.evenlo.controller;

import com.evenlo.dto.file.FileUploadResponse;
import com.evenlo.model.StoredFile;
import com.evenlo.service.FileStorageService;
import java.util.UUID;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/api/v1/files")
public class FileController {
	private final FileStorageService fileStorageService;

	public FileController(FileStorageService fileStorageService) {
		this.fileStorageService = fileStorageService;
	}

	@PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
	public FileUploadResponse upload(@RequestParam("file") MultipartFile file) {
		return fileStorageService.upload(file);
	}

	@GetMapping("/{id}")
	public ResponseEntity<Resource> download(@PathVariable UUID id) {
		StoredFile meta = fileStorageService.metadata(id);
		Resource resource = fileStorageService.load(id);
		return ResponseEntity.ok()
				.contentType(MediaType.parseMediaType(meta.getContentType()))
				.header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + meta.getOriginalFilename() + "\"")
				.body(resource);
	}
}
