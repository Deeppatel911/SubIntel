package com.example.subintel.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class SpendingTrendDTO {
	private String month;
	private Double totalAmount;
}
