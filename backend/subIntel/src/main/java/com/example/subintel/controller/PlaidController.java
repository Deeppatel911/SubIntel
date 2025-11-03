package com.example.subintel.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.example.subintel.dto.ExchangePublicToken;
import com.example.subintel.service.PlaidService;

@RestController
@RequestMapping("/api/plaid")
public class PlaidController {
	private final PlaidService plaidService;

	public PlaidController(PlaidService plaidService) {
		super();
		this.plaidService = plaidService;
	}
	
	@PostMapping("/create_link_token")
	public ResponseEntity<?> createLinkToken(){
		return plaidService.createLinkToken();
	}
	
	@PostMapping("/exchange_public_token")
	public ResponseEntity<?> exchangePublicToken(@RequestBody ExchangePublicToken publicTokenRequest){
		return plaidService.exchangePublicToken(publicTokenRequest);
	}
	
	@PostMapping("/transactions")
	public ResponseEntity<?> syncTransactions(){
		return plaidService.fetchAndSaveTransactionsForCurrentUser();
	}
}
