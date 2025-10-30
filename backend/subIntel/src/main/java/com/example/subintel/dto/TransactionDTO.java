package com.example.subintel.dto;

import java.time.LocalDate;

public class TransactionDTO {
	private Long id;
    private String name;
    private Double amount;
    private LocalDate date; 
    private String category;
    private String accountName;
    private String accountId;

    public TransactionDTO(Long id, String name, Double amount, LocalDate date, String category, String accountName, String accountId) {
        this.id = id;
        this.name = name;
        this.amount = amount;
        this.date = date;
        this.category = category;
        this.accountName = accountName;
        this.accountId = accountId;
    }

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public Double getAmount() {
		return amount;
	}

	public void setAmount(Double amount) {
		this.amount = amount;
	}

	public LocalDate getDate() {
		return date;
	}

	public void setDate(LocalDate date) {
		this.date = date;
	}

	public String getCategory() {
		return category;
	}

	public void setCategory(String category) {
		this.category = category;
	}

	public String getAccountName() {
		return accountName;
	}

	public void setAccountName(String accountName) {
		this.accountName = accountName;
	}
	
	public String getAccountId() {
		return accountId;
	}

	public void setAccountId(String accountId) {
		this.accountId = accountId;
	}

}
