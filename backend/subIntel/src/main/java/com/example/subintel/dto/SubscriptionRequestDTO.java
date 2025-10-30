package com.example.subintel.dto;

import java.time.LocalDate;

import com.example.subintel.model.Frequency;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class SubscriptionRequestDTO {
	@NotBlank(message = "Merchant name cannot be blank")
	private String merchantName;
	@NotNull(message = "Estimated amount cannot be null")
	private Double estimatedAmount;
	@NotNull(message = "Frequency cannot be null")
	private Frequency frequency;
	private LocalDate nextDueDate;
	private LocalDate lastPaymentDate;
}
