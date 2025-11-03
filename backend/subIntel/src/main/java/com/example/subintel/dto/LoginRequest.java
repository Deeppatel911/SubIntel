package com.example.subintel.dto;

import lombok.Data;

@Data
public class LoginRequest {
	private String email;
	private String password;
	
	public LoginRequest(String email, String password) {
		super();
		this.email = email;
		this.password = password;
	}
}
