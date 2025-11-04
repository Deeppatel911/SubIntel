package com.example.subintel.service;

import java.io.IOException;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.subintel.dto.CreateLinkTokenResponse;
import com.example.subintel.dto.ExchangePublicToken;
import com.example.subintel.model.AccountModel;
import com.example.subintel.model.PlaidItemModel;
import com.example.subintel.model.TransactionModel;
import com.example.subintel.model.UserModel;
import com.example.subintel.repository.AccountRepository;
import com.example.subintel.repository.PlaidItemRepository;
import com.example.subintel.repository.TransactionRepository;
import com.example.subintel.repository.UserRepository;
import com.plaid.client.model.AccountBase;
import com.plaid.client.model.CountryCode;
import com.plaid.client.model.ItemPublicTokenExchangeRequest;
import com.plaid.client.model.ItemPublicTokenExchangeResponse;
import com.plaid.client.model.LinkTokenCreateRequest;
import com.plaid.client.model.LinkTokenCreateRequestUser;
import com.plaid.client.model.LinkTokenCreateResponse;
import com.plaid.client.model.Products;
import com.plaid.client.model.Transaction;
import com.plaid.client.model.TransactionsSyncRequest;
import com.plaid.client.model.TransactionsSyncResponse;
import com.plaid.client.request.PlaidApi;

import com.plaid.client.model.ItemRemoveRequest;
import com.plaid.client.model.ItemRemoveResponse;
import jakarta.persistence.EntityNotFoundException;

import retrofit2.Response;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Service
public class PlaidService {
	private static final Logger logger = LoggerFactory.getLogger(PlaidService.class);

	private UserRepository userRepository;
	private PlaidApi plaidApi;
	private PlaidItemRepository plaidItemRepository;
	private AccountRepository accountRepository;
	private TransactionRepository transactionRepository;

	public PlaidService(UserRepository userRepository, PlaidApi plaidApi, PlaidItemRepository plaidItemRepository,
			AccountRepository accountRepository, TransactionRepository transactionRepository) {
		super();
		this.userRepository = userRepository;
		this.plaidApi = plaidApi;
		this.plaidItemRepository = plaidItemRepository;
		this.accountRepository = accountRepository;
		this.transactionRepository = transactionRepository;
	}

	public ResponseEntity<?> createLinkToken() {
		// TODO Auto-generated method stub
		try {
			String userEmail = SecurityContextHolder.getContext().getAuthentication().getName();
			UserModel user = userRepository.findByEmail(userEmail)
					.orElseThrow(() -> new UsernameNotFoundException("User not found"));

			LinkTokenCreateRequestUser userpayload = new LinkTokenCreateRequestUser()
					.clientUserId(String.valueOf(user.getId()));
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
				String errorBody = itemResponse.errorBody() != null ? itemResponse.errorBody().string()
						: "Unknown error";
				logger.error("Plaid itemPublicTokenExchange failed: HTTP {} - {}", itemResponse.code(), errorBody);
				return ResponseEntity.internalServerError().body("Failed to exchange public token");
			}

			String accessToken = itemResponse.body().getAccessToken();
			String itemIdString = itemResponse.body().getItemId();

			Optional<PlaidItemModel> existingItem = plaidItemRepository.findByItemIdAndUserModel_Id(itemIdString,
					user.getId());

			if (existingItem.isPresent()) {
				logger.info("Item ID {} already exists for user {}. Skipping save.", itemIdString, user.getId());

				return ResponseEntity.ok(Collections.singletonMap("message", "Account already linked."));
			}

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

	@Transactional
	public void fetchAndSaveTransactions(UserModel user) throws IOException {
		try {
			// String userEmail =
			// SecurityContextHolder.getContext().getAuthentication().getName();
			// UserModel user = userRepository.findByEmail(userEmail)
			// .orElseThrow(() -> new UsernameNotFoundException("User not found"));

			List<PlaidItemModel> plaidItems = user.getPlaidItems();// .stream().findFirst()
			// .orElseThrow(() -> new IllegalStateException("No linked account found for
			// this user."));

			if (plaidItems == null || plaidItems.isEmpty()) {
				// return ResponseEntity.badRequest().body("No linked accounts found for this
				// user");
				logger.warn("No linked accounts found for user ID: {},. Skipping sync.", user.getId());
				return;
			}

			for (PlaidItemModel plaidItem : plaidItems) {
				logger.info("Syncing transactions for item ID: {}", plaidItem.getItemId());
				try {
					TransactionsSyncRequest request = new TransactionsSyncRequest()
							.accessToken(plaidItem.getAccessToken());
					TransactionsSyncResponse response = plaidApi.transactionsSync(request).execute().body();

					if (response == null) {
						// return ResponseEntity.internalServerError().body("Received an empty response
						// from Plaid.");
						logger.warn("Received an empty response from Plaid.");
						return;
					}

					for (AccountBase acc : response.getAccounts()) {
						Optional<AccountModel> accountModel = accountRepository.findByAccountId(acc.getAccountId());
						AccountModel accountToSave = accountModel.orElse(new AccountModel());

						// AccountModel accountModel = new AccountModel();

						accountToSave.setPlaidItemModel(plaidItem);
						accountToSave.setAccountId(acc.getAccountId());
						accountToSave.setName(acc.getName());
						accountToSave.setOfficialName(acc.getOfficialName());
						accountToSave.setType(acc.getType().getValue());
						accountToSave.setSubType(acc.getSubtype().getValue());
						accountToSave.setBalance(acc.getBalances().getCurrent());

						accountRepository.save(accountToSave);
					}

					for (Transaction trans : response.getAdded()) {
						if (!transactionRepository.existsByTransactionId(trans.getTransactionId())) {
							Optional<AccountModel> accountOpt = accountRepository.findByAccountId(trans.getAccountId());

							if (accountOpt.isPresent()) {
								TransactionModel transactionModel = new TransactionModel();

								transactionModel.setAccountModel(accountOpt.get());
								transactionModel.setTransactionId(trans.getTransactionId());
								transactionModel.setName(trans.getName());
								transactionModel.setAmount(trans.getAmount());
								transactionModel.setLocalDate(trans.getDate());

								List<String> categories = trans.getCategory();
								logger.info("Transaction '{}', Categories from Plaid: {}", trans.getName(), categories);

								String categoryString = (categories != null && !categories.isEmpty())
										? String.join(", ", categories)
										: "Uncategorized";
								transactionModel.setCategory(categoryString);
								// transactionModel.setCategory(String.join(", ", trans.getCategory()));

								transactionRepository.save(transactionModel);
							}
						}
					}
					logger.info("Successfully synced transactions for item ID: {}", plaidItem.getItemId());
				} catch (Exception itemSyncException) {
					// TODO: handle exception
					logger.error("Error syncing transactions for item ID {}: {}", plaidItem.getItemId(),
							itemSyncException.getMessage(), itemSyncException);
				}
			}
			// return ResponseEntity.ok(Collections.singletonMap("message", "Transactions
			// synced successfully!"));

		} catch (Exception e) {
			logger.error("An unexpected error occurred during transaction sync.", e);
			return;
			// return ResponseEntity.internalServerError().body("An error occurred during
			// transaction sync.");
		}
	}

	public ResponseEntity<?> fetchAndSaveTransactionsForCurrentUser() {
		try {
			String userEmail = SecurityContextHolder.getContext().getAuthentication().getName();
			UserModel user = userRepository.findByEmail(userEmail)
					.orElseThrow(() -> new UsernameNotFoundException("User not found"));

			fetchAndSaveTransactions(user);
			return ResponseEntity.ok(Collections.singletonMap("message", "Transaction sync initiated for all items."));
		} catch (Exception e) {
			// TODO: handle exception
			logger.error("An unexpected error occurred during the user-triggered transaction sync.", e);
			return ResponseEntity.internalServerError().body("An error occurred during transaction sync.");
		}
	}

	@Transactional
	public void unlinkItem(UserModel user, String itemId) throws IOException {
		logger.info("Attempting to unlink item ID: {} for user ID: {}", itemId, user.getId());

		PlaidItemModel plaidItem = plaidItemRepository.findByItemIdAndUserModel_Id(itemId, user.getId()).orElseThrow(
				() -> new EntityNotFoundException("No Plaid item found with ID " + itemId + " for this user."));

		try {
			ItemRemoveRequest request = new ItemRemoveRequest().accessToken(plaidItem.getAccessToken());
			Response<ItemRemoveResponse> response = plaidApi.itemRemove(request).execute();
			if (response.isSuccessful()) {
				logger.info("Successfully removed item {} from Plaid.", itemId);
			} else {
				logger.warn("Could not remove item {} from Plaid. Response: {}", itemId, response.errorBody().string());
			}
		} catch (Exception e) {
			// TODO: handle exception
			logger.error("Error calling Plaid's /item/remove API: {}", e.getMessage());
		}

		plaidItemRepository.delete(plaidItem);
		logger.info("Successfully unlinked item ID: {} from local database for user ID: {}", itemId, user.getId());
	}
}
