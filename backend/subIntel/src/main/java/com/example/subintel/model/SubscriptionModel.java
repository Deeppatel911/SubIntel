package com.example.subintel.model;

import java.time.LocalDate;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;

@Entity
public class SubscriptionModel {
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long subscriptionId;
	
	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "user_id",nullable = false)
	private UserModel userModel;
	
	@Column(nullable = false)
	private String merchantName;
	
	@Enumerated(EnumType.STRING)
	private Frequency frequency;
	
	private double estimatedAmount;
	
	private LocalDate nextDueDate;
	
	private LocalDate lastPaymentDate;
	
	private boolean isActive=true;

	public Long getSubscriptionId() {
		return subscriptionId;
	}

	public void setSubscriptionId(Long subscriptionId) {
		this.subscriptionId = subscriptionId;
	}

	public UserModel getUserModel() {
		return userModel;
	}

	public void setUserModel(UserModel userModel) {
		this.userModel = userModel;
	}

	public String getMerchantName() {
		return merchantName;
	}

	public void setMerchantName(String merchant) {
		this.merchantName = merchant;
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
