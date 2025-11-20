package com.example.subintel.service;

import java.util.List;
import java.util.stream.Collectors;

//import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import com.example.subintel.dto.SpendingTrendDTO;
import com.example.subintel.dto.TransactionDTO;
import com.example.subintel.model.TransactionModel;
import com.example.subintel.model.UserModel;
import com.example.subintel.repository.TransactionRepository;
import com.example.subintel.repository.UserRepository;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Service
public class TransactionService {
	private static final Logger logger = LoggerFactory.getLogger(TransactionService.class);

	private TransactionRepository transactionRepository;
	private UserRepository userRepository;

	public TransactionService(TransactionRepository transactionRepository, UserRepository userRepository) {
		super();
		this.transactionRepository = transactionRepository;
		this.userRepository = userRepository;
	}

	public List<TransactionDTO> getTransactionsForCurrentUser() {// ResponseEntity<?> getTransactionsForCurrentUser() {
		try { // Add try block
			String userEmail = SecurityContextHolder.getContext().getAuthentication().getName();
			UserModel user = userRepository.findByEmail(userEmail)
					.orElseThrow(() -> new UsernameNotFoundException("User not found"));

			logger.info("Fetching transactions for user ID: {}", user.getId()); // Log user ID
			List<TransactionModel> transactions = transactionRepository.findTransactionsByUserId(user.getId());
			logger.info("Found {} transactions for user ID: {}", transactions.size(), user.getId()); // Log count

			return transactions.stream()
					.map(entity -> new TransactionDTO(entity.getId(), entity.getName(), entity.getAmount(),
							entity.getLocalDate(), entity.getCategory(), entity.getAccountModel().getName(),
							entity.getAccountModel().getAccountId()))
					.collect(Collectors.toList());
			// return ResponseEntity.ok(transactions);

		} catch (Exception e) { // Add catch block
			logger.error("Error fetching transactions for current user", e);
			throw e; // Re-throw the exception so Spring handles it
		}

		// String userEmail =
		// SecurityContextHolder.getContext().getAuthentication().getName();
		// UserModel user = userRepository.findByEmail(userEmail).orElseThrow(() -> new
		// UsernameNotFoundException(userEmail));
		// List<TransactionModel> transactions =
		// transactionRepository.findTransactionsByUserId(user.getUser_id());
		// return ResponseEntity.ok(transactions);
	}

	public List<SpendingTrendDTO> getSpendingTrendForCurrentUser(Long userId) {
		logger.info("Fetching spending trend for user ID: {}", userId);
		return transactionRepository.findSpendingTrendByUserId(userId);
	}
}
