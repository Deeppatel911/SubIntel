package com.example.subintel.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.Data;

@Data
public class ExchangePublicToken {
	@JsonProperty("public_token")
	private String publicToken;

	public ExchangePublicToken(String publicToken) {
		super();
		this.publicToken = publicToken;
	}

	public String getPublicToken() {
		return publicToken;
	}

	public void setPublicToken(String publicToken) {
		this.publicToken = publicToken;
	}
}
