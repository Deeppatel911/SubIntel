package com.example.subintel.model;

import java.util.List;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import lombok.Data;

@Data
@Entity
public class PlaidItemModel {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "user_id", nullable = false)
	private UserModel userModel;

	@Column(nullable = false)
	private String accessToken;

	@Column(nullable = false, unique = true)
	private String itemId;
	
	@OneToMany(mappedBy = "plaidItemModel", cascade = CascadeType.ALL, fetch = FetchType.LAZY, orphanRemoval = true)
	private List<AccountModel> accountModels;
}
