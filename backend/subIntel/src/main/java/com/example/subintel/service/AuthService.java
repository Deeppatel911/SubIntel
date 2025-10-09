package com.example.subintel.service;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import com.example.subintel.dto.RegisterRequest;
import com.example.subintel.model.UserModel;
import com.example.subintel.repository.UserRepository;

@Service
public class AuthService {
	private UserRepository userRepository;
	private PasswordEncoder passwdEncoder;

	public AuthService(UserRepository userRepository, PasswordEncoder passwdEncoder) {
		super();
		this.userRepository = userRepository;
		this.passwdEncoder = passwdEncoder;
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
}
