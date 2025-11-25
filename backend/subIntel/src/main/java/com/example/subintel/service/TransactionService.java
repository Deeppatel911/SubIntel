package com.example.subintel.service;

import java.util.List;
import java.util.stream.Collectors;

//import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import com.example.subintel.dto.SpendingTrendDTO;
import com.example.subintel.dto.TransactionDTO;
import com.example.subintel.model.TransactionModel;
import com.example.subintel.repository.TransactionRepository;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Service
public class TransactionService {
	private static final Logger logger = LoggerFactory.getLogger(TransactionService.class);

	private TransactionRepository transactionRepository;

	public TransactionService(TransactionRepository transactionRepository) {
		super();
		this.transactionRepository = transactionRepository;
	}

	public List<TransactionDTO> getTransactionsForCurrentUser(Long userId) {// ResponseEntity<?>
																			// getTransactionsForCurrentUser() {
		try { // Add try block

			logger.info("Fetching transactions for user ID: {}", userId);
			List<TransactionModel> transactions = transactionRepository.findTransactionsByUserId(userId);
			logger.info("Found {} transactions for user ID: {}", transactions.size(), userId); // Log count

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

		// List<TransactionModel> transactions =
		// transactionRepository.findTransactionsByUserId(user.getUser_id());
		// return ResponseEntity.ok(transactions);
	}

	public List<SpendingTrendDTO> getSpendingTrendForCurrentUser(Long userId) {
		logger.info("Fetching spending trend for user ID: {}", userId);
		return transactionRepository.findSpendingTrendByUserId(userId);
	}
}
