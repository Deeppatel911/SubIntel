package com.example.subintel.dto;

import java.time.LocalDate;

import lombok.Data;

@Data
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
}
