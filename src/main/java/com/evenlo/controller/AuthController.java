package com.evenlo.controller;

import com.evenlo.dto.auth.AuthResponse;
import com.evenlo.dto.auth.LoginRequest;
import com.evenlo.dto.auth.MeResponse;
import com.evenlo.dto.auth.RegisterRequest;
import com.evenlo.exception.ForbiddenException;
import com.evenlo.model.User;
import com.evenlo.repository.UserRepository;
import com.evenlo.service.AuthService;
import com.evenlo.util.SecurityUtil;
import jakarta.validation.Valid;
import java.util.UUID;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/auth")
public class AuthController {
	private final AuthService authService;
	private final UserRepository userRepository;

	public AuthController(AuthService authService, UserRepository userRepository) {
		this.authService = authService;
		this.userRepository = userRepository;
	}

	@PostMapping("/register")
	public AuthResponse register(@Valid @RequestBody RegisterRequest request) {
		return authService.register(request);
	}

	@PostMapping("/login")
	public AuthResponse login(@Valid @RequestBody LoginRequest request) {
		return authService.login(request);
	}

	@GetMapping("/me")
	public MeResponse me() {
		UUID userId = SecurityUtil.currentUserId();
		User user = userRepository.findById(userId).orElseThrow(() -> new ForbiddenException("unauthenticated"));
		return new MeResponse(user.getId(), user.getEmail(), user.getDisplayName(), user.getPlatformRole());
	}
}
