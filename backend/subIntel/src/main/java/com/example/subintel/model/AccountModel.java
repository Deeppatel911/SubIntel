package com.example.subintel.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
//import lombok.Data;

//@Data
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
	
	public long getId() {
		return id;
	}
	public void setId(long id) {
		this.id = id;
	}
	
	public PlaidItemModel getPlaidItemModel() {
		return plaidItemModel;
	}
	public void setPlaidItemModel(PlaidItemModel plaidItemModel) {
		this.plaidItemModel = plaidItemModel;
	}
	public String getAccountId() {
		return accountId;
	}
	public void setAccountId(String accountId) {
		this.accountId = accountId;
	}
	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}
	public String getOfficialName() {
		return officialName;
	}
	public void setOfficialName(String officialName) {
		this.officialName = officialName;
	}
	public String getType() {
		return type;
	}
	public void setType(String type) {
		this.type = type;
	}
	public String getSubType() {
		return subType;
	}
	public void setSubType(String subType) {
		this.subType = subType;
	}
	public Double getBalance() {
		return balance;
	}
	public void setBalance(Double balance) {
		this.balance = balance;
	}
}
