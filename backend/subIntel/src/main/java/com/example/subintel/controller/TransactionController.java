package com.example.subintel.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

//import com.example.subintel.model.TransactionModel;
import com.example.subintel.service.TransactionService;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.example.subintel.dto.SpendingTrendDTO;
import com.example.subintel.dto.TransactionDTO;
import com.example.subintel.model.UserModel;
import com.example.subintel.repository.UserRepository;

@RestController
@RequestMapping("/api/transactions")
public class TransactionController {
	private static final Logger logger = LoggerFactory.getLogger(TransactionService.class);

	private final UserRepository userRepository;
	private final TransactionService transactionService;

	public TransactionController(UserRepository userRepository, TransactionService transactionService) {
		super();
		this.userRepository = userRepository;
		this.transactionService = transactionService;
	}

	@GetMapping
	public ResponseEntity<?> getTransactions() {
		try {
			logger.info("Received GET request for transactions");

			String userEmail = SecurityContextHolder.getContext().getAuthentication().getName();
			UserModel user = userRepository.findByEmail(userEmail)
					.orElseThrow(() -> new UsernameNotFoundException("User not found"));
			// List<TransactionModel>
			List<TransactionDTO> transactions = transactionService.getTransactionsForCurrentUser(user.getId());
			logger.info("Returning {} transactions", transactions.size());
			return ResponseEntity.ok(transactions);
		} catch (Exception e) { // Add catch block
			logger.error("Error processing GET /api/transactions", e);
			return ResponseEntity.internalServerError().build();
		}

		// return transactionService.getTransactionsForCurrentUser();
	}

	@GetMapping("/trends")
	public ResponseEntity<List<SpendingTrendDTO>> getSpendingTrend() {
		try {
			String userEmail = SecurityContextHolder.getContext().getAuthentication().getName();
			UserModel user = userRepository.findByEmail(userEmail)
					.orElseThrow(() -> new UsernameNotFoundException("User not found"));

			List<SpendingTrendDTO> trendData = transactionService.getSpendingTrendForCurrentUser(user.getId());
			return ResponseEntity.ok(trendData);
		} catch (Exception e) {
			// TODO: handle exception
			logger.error("Error fetching spending trend", e);
			return ResponseEntity.internalServerError().build();
		}
	}
}
