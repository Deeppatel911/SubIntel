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
public class PlaidItemModel {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long plaidItemId;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "user_id", nullable = false)
	private UserModel userModel;

	@Column(nullable = false)
	private String accessToken;

	@Column(nullable = false, unique = true)
	private String itemId;

	public Long getPlaidItemId() {
		return plaidItemId;
	}

	public void setPlaidItemId(Long plaidItemId) {
		this.plaidItemId = plaidItemId;
	}

	public UserModel getUserModel() {
		return userModel;
	}

	public void setUserModel(UserModel userModel) {
		this.userModel = userModel;
	}

	public String getAccessToken() {
		return accessToken;
	}

	public void setAccessToken(String accessToken) {
		this.accessToken = accessToken;
	}

	public String getItemId() {
		return itemId;
	}

	public void setItemId(String itemId) {
		this.itemId = itemId;
	}
}
