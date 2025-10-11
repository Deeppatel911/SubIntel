package com.example.subintel.service;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import com.example.subintel.dto.JWTResponse;
import com.example.subintel.dto.LoginRequest;
import com.example.subintel.dto.RegisterRequest;
import com.example.subintel.model.UserModel;
import com.example.subintel.repository.UserRepository;

@Service
public class AuthService {
	private UserRepository userRepository;
	private PasswordEncoder passwdEncoder;
	private AuthenticationManager authenticationManager;
	private JWTService jwtService;
	
	public AuthService(UserRepository userRepository, PasswordEncoder passwdEncoder,
			AuthenticationManager authenticationManager, JWTService jwtService) {
		super();
		this.userRepository = userRepository;
		this.passwdEncoder = passwdEncoder;
		this.authenticationManager = authenticationManager;
		this.jwtService = jwtService;
	}

	public ResponseEntity<?> registerUser(RegisterRequest registerRequest) {
		if(userRepository.findByEmail(registerRequest.getEmail()).isPresent()) {
			return ResponseEntity.badRequest().body("Error: Email is already in use");
		}
		
		UserModel userModel=new UserModel();
		userModel.setFirstName(registerRequest.getFirstName());
		userModel.setLastName(registerRequest.getLastName());
		userModel.setEmail(registerRequest.getEmail());
		userModel.setPassword(passwdEncoder.encode(registerRequest.getPassword()));
		userRepository.save(userModel);
		
		return ResponseEntity.ok("User Registered Successfully");
	}
	
	public ResponseEntity<?> loginUser(LoginRequest loginRequest) {
		Authentication authentication = authenticationManager.authenticate(new UsernamePasswordAuthenticationToken(loginRequest.getEmail(), loginRequest.getPassword()));
	
		SecurityContextHolder.getContext().setAuthentication(authentication);
	    String jwt = jwtService.generateToken(loginRequest.getEmail());

	    return ResponseEntity.ok(new JWTResponse(jwt));
	}
}
