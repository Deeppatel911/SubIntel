package com.example.subintel.dto;

import lombok.Data;

@Data
public class JWTResponse {
	private String token;
	
	public JWTResponse(String token) {
		super();
		this.token = token;
	}

	public String getToken() {
		return token;
	}

	public void setToken(String token) {
		this.token = token;
	}
	
}
