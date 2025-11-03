package com.example.subintel.dto;

import lombok.Data;

@Data
public class RegisterRequest {
	private String firstName;
	private String lastName;
	private String email;
	private String password;
	
	public RegisterRequest(String firstName, String lastName, String email, String password) {
		super();
		this.firstName = firstName;
		this.lastName = lastName;
		this.email = email;
		this.password = password;
	}
}
