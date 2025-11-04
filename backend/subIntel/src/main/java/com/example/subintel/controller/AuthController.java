package com.example.subintel.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.example.subintel.dto.ForgotPasswordRequestDTO;
import com.example.subintel.dto.LoginRequest;
import com.example.subintel.dto.RegisterRequest;
import com.example.subintel.dto.ResetPasswordRequestDTO;
import com.example.subintel.service.AuthService;

import jakarta.persistence.EntityNotFoundException;

@RestController
@RequestMapping("/api/auth")
public class AuthController {
	private AuthService authService;

	public AuthController(AuthService authService) {
		super();
		this.authService = authService;
	}

	@PostMapping("/register")
	public ResponseEntity<?> registerUser(@RequestBody RegisterRequest registerRequest) {
		return authService.registerUser(registerRequest);
	}

	@PostMapping("/login")
	public ResponseEntity<?> loginUser(@RequestBody LoginRequest loginRequest) {
		return authService.loginUser(loginRequest);
	}

	@PostMapping("/forgot-password")
	public ResponseEntity<?> forgotPassword(@RequestBody ForgotPasswordRequestDTO request) {
		try {
			return authService.forgotPassword(request.getEmail());
		} catch (EntityNotFoundException e) {
			// TODO: handle exception
			return ResponseEntity.status(404).body(e.getMessage());
		} catch (Exception e) {
			// TODO: handle exception
			return ResponseEntity.internalServerError().body(e.getMessage());
		}
	}

	@PostMapping("/reset-password")
	public ResponseEntity<?> resetPassword(@RequestBody ResetPasswordRequestDTO request) {
		try {
			return authService.resetPassword(request.getToken(), request.getNewPassword());
		} catch (EntityNotFoundException | IllegalArgumentException e) {
			// TODO: handle exception
			return ResponseEntity.badRequest().body(e.getMessage());

		} catch (Exception e) {
			// TODO: handle exception
			return ResponseEntity.internalServerError().body(e.getMessage());
		}
	}
}
