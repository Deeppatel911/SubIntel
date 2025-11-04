package com.example.subintel.dto;

import lombok.Data;

@Data
public class ResetPasswordRequestDTO {
	private String token;
	private String newPassword;
}
