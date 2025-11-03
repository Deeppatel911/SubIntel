package com.example.subintel.dto;

import java.time.LocalDate;
import com.example.subintel.model.Frequency;
import com.example.subintel.model.SubscriptionModel;

import lombok.Data;

@Data
public class SubscriptionDTO {
	private Long subscriptionId;
	private String merchantName;
	private Frequency frequency;
	private double estimatedAmount;
	private LocalDate nextDueDate;
	private LocalDate lastPaymentDate;
	private boolean isActive;
	
	public SubscriptionDTO(SubscriptionModel entity) {
		this.subscriptionId = entity.getSubscriptionId();
		this.merchantName = entity.getMerchantName();
		this.frequency = entity.getFrequency();
		this.estimatedAmount = entity.getEstimatedAmount();
		this.nextDueDate = entity.getNextDueDate();
		this.lastPaymentDate = entity.getLastPaymentDate();
		this.isActive = entity.isActive();
	}
}
