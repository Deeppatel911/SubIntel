package com.example.subintel.service;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import com.example.subintel.dto.AccountDTO;
import com.example.subintel.model.AccountModel;
import com.example.subintel.repository.AccountRepository;

@Service
public class AccountService {
	private final static Logger logger = LoggerFactory.getLogger(AccountService.class);
	private AccountRepository accountRepository;

	public AccountService(AccountRepository accountRepository) {
		super();
		this.accountRepository = accountRepository;
	}

	public List<AccountDTO> getAccountsForCurrentUser(Long userId) {
		logger.info("Fetching accounts for user ID: {}", userId);
		List<AccountModel> accounts = accountRepository.findByPlaidItemModel_UserModel_Id(userId);

		if (accounts.isEmpty()) {
			logger.info("No accounts found for the user ID: {}", userId);
			return Collections.emptyList();
		}

		List<AccountDTO> accountDTOs = accounts.stream().map(AccountDTO::new).collect(Collectors.toList());
		logger.info("Returning {} accounts for the user ID: {}", accountDTOs.size(), userId);
		return accountDTOs;
	}
}
