package com.example.subintel.dto;

import com.example.subintel.model.AccountModel;

import lombok.Data;

@Data
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
}
