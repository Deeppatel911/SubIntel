package com.example.subintel.dto;

import lombok.Data;

@Data
public class CreateLinkTokenResponse {
    private String link_token;

    public CreateLinkTokenResponse(String link_token) {
        this.link_token = link_token;
    }

	public String getLink_token() {
		return link_token;
	}

	public void setLink_token(String link_token) {
		this.link_token = link_token;
	}
    
}