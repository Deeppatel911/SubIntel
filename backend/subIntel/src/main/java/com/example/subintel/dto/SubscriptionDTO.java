package com.example.subintel.dto;

import java.time.LocalDate;
import com.example.subintel.model.Frequency;
import com.example.subintel.model.SubscriptionModel;

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
	
	public Long getSubscriptionId() {
		return subscriptionId;
	}
	public void setSubscriptionId(Long subscriptionId) {
		this.subscriptionId = subscriptionId;
	}
	public String getMerchantName() {
		return merchantName;
	}
	public void setMerchantName(String merchantName) {
		this.merchantName = merchantName;
	}
	public Frequency getFrequency() {
		return frequency;
	}
	public void setFrequency(Frequency frequency) {
		this.frequency = frequency;
	}
	public double getEstimatedAmount() {
		return estimatedAmount;
	}
	public void setEstimatedAmount(double estimatedAmount) {
		this.estimatedAmount = estimatedAmount;
	}
	public LocalDate getNextDueDate() {
		return nextDueDate;
	}
	public void setNextDueDate(LocalDate nextDueDate) {
		this.nextDueDate = nextDueDate;
	}
	public LocalDate getLastPaymentDate() {
		return lastPaymentDate;
	}
	public void setLastPaymentDate(LocalDate lastPaymentDate) {
		this.lastPaymentDate = lastPaymentDate;
	}
	public boolean isActive() {
		return isActive;
	}
	public void setActive(boolean isActive) {
		this.isActive = isActive;
	}
}
