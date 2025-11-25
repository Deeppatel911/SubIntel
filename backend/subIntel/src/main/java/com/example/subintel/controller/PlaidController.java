package com.example.subintel.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.example.subintel.dto.ExchangePublicToken;
import com.example.subintel.model.UserModel;
import com.example.subintel.repository.UserRepository;
import com.example.subintel.service.PlaidService;

import jakarta.persistence.EntityNotFoundException;

import java.util.Collections;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@RestController
@RequestMapping("/api/plaid")
public class PlaidController {
	private Logger logger = LoggerFactory.getLogger(PlaidController.class);
	private final PlaidService plaidService;
	private final UserRepository userRepository;

	public PlaidController(PlaidService plaidService, UserRepository userRepository) {
		super();
		this.plaidService = plaidService;
		this.userRepository = userRepository;
	}

	@PostMapping("/create_link_token")
	public ResponseEntity<?> createLinkToken() {
		String userEmail = SecurityContextHolder.getContext().getAuthentication().getName();
		UserModel user = userRepository.findByEmail(userEmail)
				.orElseThrow(() -> new UsernameNotFoundException("User not found"));

		return plaidService.createLinkToken(user.getId());
	}

	@PostMapping("/exchange_public_token")
	public ResponseEntity<?> exchangePublicToken(@RequestBody ExchangePublicToken publicTokenRequest) {
		String userEmail = SecurityContextHolder.getContext().getAuthentication().getName();
		UserModel user = userRepository.findByEmail(userEmail)
				.orElseThrow(() -> new UsernameNotFoundException("User not found"));

		return plaidService.exchangePublicToken(user, publicTokenRequest);
	}

	@PostMapping("/transactions")
	public ResponseEntity<?> syncTransactions() {
		String userEmail = SecurityContextHolder.getContext().getAuthentication().getName();
		UserModel user = userRepository.findByEmail(userEmail)
				.orElseThrow(() -> new UsernameNotFoundException("User not found"));

		return plaidService.fetchAndSaveTransactionsForCurrentUser(user);
	}

	@DeleteMapping("/item/{itemId}")
	public ResponseEntity<?> unlinkPlaidItem(@PathVariable String itemId) {
		try {
			String email = SecurityContextHolder.getContext().getAuthentication().getName();
			UserModel user = userRepository.findByEmail(email)
					.orElseThrow(() -> new UsernameNotFoundException("User not found"));

			plaidService.unlinkItem(user, itemId);
			return ResponseEntity.ok(Collections.singletonMap("message", "Item unlinked successfully."));
		} catch (UsernameNotFoundException e) {
			// TODO: handle exception
			logger.warn("Unlink attempt by non-existent user.", e);
			return ResponseEntity.status(401).build();
		} catch (EntityNotFoundException e) {
			// TODO: handle exception
			logger.warn("Failed to unlink item: {}", e.getMessage());
			return ResponseEntity.status(404).build();
		} catch (Exception e) {
			// TODO: handle exception
			logger.error("Error unlinking item {}: {}", itemId, e.getMessage());
			return ResponseEntity.internalServerError().body("An error occurred while unlinking the item.");
		}
	}
}
