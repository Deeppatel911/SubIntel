package com.example.subintel.service;

import java.io.IOException;
import java.util.Collections;
import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import com.example.subintel.dto.CreateLinkTokenResponse;
import com.example.subintel.dto.ExchangePublicToken;
import com.example.subintel.model.PlaidItemModel;
import com.example.subintel.model.UserModel;
import com.example.subintel.repository.PlaidItemRepository;
import com.example.subintel.repository.UserRepository;
import com.plaid.client.model.CountryCode;
import com.plaid.client.model.ItemPublicTokenExchangeRequest;
import com.plaid.client.model.ItemPublicTokenExchangeResponse;
import com.plaid.client.model.LinkTokenCreateRequest;
import com.plaid.client.model.LinkTokenCreateRequestUser;
import com.plaid.client.model.LinkTokenCreateResponse;
import com.plaid.client.model.Products;
import com.plaid.client.request.PlaidApi;

import retrofit2.Response;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Service
public class PlaidService {
	private static final Logger logger = LoggerFactory.getLogger(PlaidService.class);
	
	private UserRepository userRepository;
	private PlaidApi plaidApi;
	private PlaidItemRepository plaidItemRepository;

	public PlaidService(UserRepository userRepository, PlaidApi plaidApi, PlaidItemRepository plaidItemRepository) {
		super();
		this.userRepository = userRepository;
		this.plaidApi = plaidApi;
		this.plaidItemRepository = plaidItemRepository;
	}

	public ResponseEntity<?> createLinkToken() {
		// TODO Auto-generated method stub
		try {
			String userEmail = SecurityContextHolder.getContext().getAuthentication().getName();
			UserModel user = userRepository.findByEmail(userEmail)
					.orElseThrow(() -> new UsernameNotFoundException("User not found"));

			LinkTokenCreateRequestUser userpayload = new LinkTokenCreateRequestUser()
					.clientUserId(String.valueOf(user.getUser_id()));
			LinkTokenCreateRequest linkTokenCreateRequest = new LinkTokenCreateRequest().user(userpayload)
					.clientName("SubIntel").products(List.of(Products.TRANSACTIONS))
					.countryCodes(List.of(CountryCode.US)).language("en");

			Response<LinkTokenCreateResponse> response = plaidApi.linkTokenCreate(linkTokenCreateRequest).execute();

			if (!response.isSuccessful() || response.body() == null) {
				String errorBody = response.errorBody() != null ? response.errorBody().string() : "Unknown error";
                logger.error("Plaid API Error: {}", errorBody);
				
				return ResponseEntity.internalServerError().body("Failed to create Plaid link token.");
			}

			return ResponseEntity.ok(new CreateLinkTokenResponse(response.body().getLinkToken()));
		} catch (IOException e) {
			// TODO: handle exception
			logger.error("An unexpected error occurred during token exchange.", e);
			return ResponseEntity.internalServerError().body("An error occured while creating the link token");
		}
	}

	public ResponseEntity<?> exchangePublicToken(ExchangePublicToken exchangePublicToken) {
		try {
			String userEmail = SecurityContextHolder.getContext().getAuthentication().getName();
			UserModel user = userRepository.findByEmail(userEmail)
					.orElseThrow(() -> new UsernameNotFoundException("User not found"));

			if (exchangePublicToken.getPublicToken() == null || exchangePublicToken.getPublicToken().isBlank()) {
			    return ResponseEntity.badRequest().body("public_token is required");
			}
			
			ItemPublicTokenExchangeRequest itemPublicTokenExchangeRequest = new ItemPublicTokenExchangeRequest()
					.publicToken(exchangePublicToken.getPublicToken());
			Response<ItemPublicTokenExchangeResponse> itemResponse = plaidApi
					.itemPublicTokenExchange(itemPublicTokenExchangeRequest).execute();

			if (!itemResponse.isSuccessful() || itemResponse.body() == null) {
				String errorBody = itemResponse.errorBody() != null ? itemResponse.errorBody().string() : "Unknown error";
			    logger.error("Plaid itemPublicTokenExchange failed: HTTP {} - {}", itemResponse.code(), errorBody);
				return ResponseEntity.internalServerError().body("Failed to exchange public token");
			}

			String accessToken = itemResponse.body().getAccessToken();
			String itemIdString = itemResponse.body().getItemId();

			PlaidItemModel plaidItemModel = new PlaidItemModel();
			plaidItemModel.setUserModel(user);
			plaidItemModel.setAccessToken(accessToken);
			plaidItemModel.setItemId(itemIdString);

			plaidItemRepository.save(plaidItemModel);
			return ResponseEntity.ok(Collections.singletonMap("message", "Account linked successfully"));
		} catch (Exception e) {
			// TODO: handle exception
	        logger.error("An unexpected error occurred during token exchange.", e);
			return ResponseEntity.internalServerError().body("An error occured during token exchange");
		}
	}
}
