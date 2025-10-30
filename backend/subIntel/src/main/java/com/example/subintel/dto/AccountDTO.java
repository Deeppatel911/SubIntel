package com.example.subintel.dto;

import com.example.subintel.model.AccountModel;

public class AccountDTO {
	private String accountId;
	private String name;
	private String officialName;
	private String type;
	private String subType;
	private Double balance;

	public AccountDTO(AccountModel accountModel) {
		// TODO Auto-generated constructor stub
		this.accountId = accountModel.getAccountId();
		this.name = accountModel.getName();
		this.officialName = accountModel.getOfficialName();
		this.type = accountModel.getType();
		this.subType = accountModel.getSubType();
		this.balance = accountModel.getBalance();
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
