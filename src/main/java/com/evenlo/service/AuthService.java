package com.evenlo.service;

import com.evenlo.dto.auth.AuthResponse;
import com.evenlo.dto.auth.LoginRequest;
import com.evenlo.dto.auth.RegisterRequest;
import com.evenlo.exception.BadRequestException;
import com.evenlo.exception.ForbiddenException;
import com.evenlo.model.PlatformRole;
import com.evenlo.model.User;
import com.evenlo.repository.UserRepository;
import java.util.UUID;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class AuthService {
	private final UserRepository userRepository;
	private final PasswordEncoder passwordEncoder;
	private final JwtService jwtService;

	public AuthService(UserRepository userRepository, PasswordEncoder passwordEncoder, JwtService jwtService) {
		this.userRepository = userRepository;
		this.passwordEncoder = passwordEncoder;
		this.jwtService = jwtService;
	}

	@Transactional

	public AuthResponse register(RegisterRequest request) {
		String email = request.email().trim().toLowerCase();
		if (userRepository.existsByEmail(email)) {
			throw new BadRequestException("email_already_registered");
		}

		User user = new User();
		user.setId(UUID.randomUUID());
		user.setEmail(email);
		user.setDisplayName(request.displayName().trim());
		user.setPasswordHash(passwordEncoder.encode(request.password()));
		user.setPlatformRole(PlatformRole.USER);

		User saved = userRepository.save(user);
		return AuthResponse.bearer(jwtService.createAccessToken(saved));
	}

	@Transactional(readOnly = true)
	public AuthResponse login(LoginRequest request) {
		String email = request.email().trim().toLowerCase();
		User user = userRepository.findByEmail(email).orElseThrow(() -> new ForbiddenException("invalid_credentials"));
		if (!passwordEncoder.matches(request.password(), user.getPasswordHash())) {
			throw new ForbiddenException("invalid_credentials");
		}
		return AuthResponse.bearer(jwtService.createAccessToken(user));
	}
}
