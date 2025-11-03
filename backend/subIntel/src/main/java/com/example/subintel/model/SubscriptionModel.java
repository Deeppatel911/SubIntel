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
import lombok.Data;

@Data
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
}
