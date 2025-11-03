package com.example.subintel.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import lombok.Data;

@Data
@Entity
public class AccountModel {
	
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private long id;
	
	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "plaid_item_id", nullable = false)
	private PlaidItemModel plaidItemModel;
	
	@Column(unique = true, nullable = false)
	private String accountId;
	private String name;
	private String officialName;
	private String type;
	private String subType;
	private Double balance;
}
