package com.example.subintel.model;

import java.security.Timestamp;
import java.time.LocalDateTime;
import java.util.List;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import lombok.Data;

@Data
@Entity
public class UserModel {
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private long id;
	@Column(nullable = false)
	private String firstName;
	@Column(nullable = false)
	private String lastName;
	@Column(nullable = false, unique = true)
	private String email;
	@Column(nullable = false)
	private String password;
	@Enumerated(EnumType.STRING)
	@Column(nullable = false)

	private Role role = Role.USER;

	private String resetPasswordToken;

	@Column(columnDefinition = "TIMESTAMP")
	private LocalDateTime resetPasswordTokenExpiry;

	@OneToMany(mappedBy = "userModel", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
	private List<PlaidItemModel> plaidItems;
}
