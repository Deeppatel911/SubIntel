package com.example.subintel.service;

import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.subintel.dto.JWTResponse;
import com.example.subintel.dto.LoginRequest;
import com.example.subintel.dto.RegisterRequest;
import com.example.subintel.model.UserModel;
import com.example.subintel.repository.UserRepository;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.UUID;
import jakarta.persistence.EntityNotFoundException;

@Service
public class AuthService {
	private final UserRepository userRepository;
	private final PasswordEncoder passwdEncoder;
	private final AuthenticationManager authenticationManager;
	private final JWTService jwtService;
	private final EmailService emailService;

	public AuthService(UserRepository userRepository, PasswordEncoder passwdEncoder,
			AuthenticationManager authenticationManager, JWTService jwtService, EmailService emailService) {
		super();
		this.userRepository = userRepository;
		this.passwdEncoder = passwdEncoder;
		this.authenticationManager = authenticationManager;
		this.jwtService = jwtService;
		this.emailService = emailService;
	}

	public ResponseEntity<?> registerUser(RegisterRequest registerRequest) {
		if (userRepository.findByEmail(registerRequest.getEmail()).isPresent()) {
			return ResponseEntity.badRequest().body("Error: Email is already in use");
		}

		UserModel userModel = new UserModel();
		userModel.setFirstName(registerRequest.getFirstName());
		userModel.setLastName(registerRequest.getLastName());
		userModel.setEmail(registerRequest.getEmail());
		userModel.setPassword(passwdEncoder.encode(registerRequest.getPassword()));
		userRepository.save(userModel);

		return ResponseEntity.ok("User Registered Successfully");
	}

	public ResponseEntity<?> loginUser(LoginRequest loginRequest) {
		Authentication authentication = authenticationManager.authenticate(
				new UsernamePasswordAuthenticationToken(loginRequest.getEmail(), loginRequest.getPassword()));

		SecurityContextHolder.getContext().setAuthentication(authentication);
		String jwt = jwtService.generateToken(loginRequest.getEmail());

		return ResponseEntity.ok(new JWTResponse(jwt));
	}

	@Transactional
	public ResponseEntity<?> forgotPassword(String email) {
		UserModel user = userRepository.findByEmail(email)
				.orElseThrow(() -> new EntityNotFoundException("User not found with email: " + email));

		String token = UUID.randomUUID().toString();
		user.setResetPasswordToken(token);
		user.setResetPasswordTokenExpiry(LocalDateTime.now().plusHours(1));

		userRepository.save(user);

		emailService.sendPasswordResetEmail(user, token);
		return ResponseEntity.ok(Collections.singletonMap("message", "Password reset link sent to your email."));
	}

	@Transactional
	public ResponseEntity<?> resetPassword(String token, String newPassword) {
		UserModel user = userRepository.findByResetPasswordToken(token)
				.orElseThrow(() -> new EntityNotFoundException("Invalid or expired reset token."));

		if (user.getResetPasswordTokenExpiry() == null
				|| user.getResetPasswordTokenExpiry().isBefore(LocalDateTime.now())) {
			throw new IllegalArgumentException("Reset token has expired.");
		}

		user.setPassword(passwdEncoder.encode(newPassword));
		user.setResetPasswordToken(null);
		user.setResetPasswordTokenExpiry(null);

		userRepository.save(user);
		return ResponseEntity.ok(Collections.singletonMap("message", "Password reset successfully"));
	}
}
