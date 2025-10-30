package com.example.subintel.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

//import com.example.subintel.model.TransactionModel;
import com.example.subintel.service.TransactionService;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.example.subintel.dto.TransactionDTO;

@RestController
@RequestMapping("/api/transactions")
public class TransactionController {
	private static final Logger logger = LoggerFactory.getLogger(TransactionService.class);
	
	private TransactionService transactionService;

	public TransactionController(TransactionService transactionService) {
		super();
		this.transactionService = transactionService;
	}
	
	@GetMapping
	public ResponseEntity<?> getTransactions(){
		try { // Add try block
            logger.info("Received GET request for transactions");
            //List<TransactionModel> 
            List<TransactionDTO> transactions = transactionService.getTransactionsForCurrentUser();
            logger.info("Returning {} transactions", transactions.size());
            return ResponseEntity.ok(transactions);
        } catch (Exception e) { // Add catch block
            logger.error("Error processing GET /api/transactions", e);
            // Return a 500 error explicitly
            return ResponseEntity.internalServerError().build();
        }
		
		//return transactionService.getTransactionsForCurrentUser();
	}
}
