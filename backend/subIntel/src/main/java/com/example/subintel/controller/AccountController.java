package com.example.subintel.controller;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.example.subintel.dto.AccountDTO;
import com.example.subintel.model.UserModel;
import com.example.subintel.repository.UserRepository;
import com.example.subintel.service.AccountService;

@RestController
@RequestMapping("/api/accounts")
public class AccountController {
	private static final Logger logger = LoggerFactory.getLogger(AccountController.class);
	private final AccountService accountService;
	private final UserRepository userRepository;

	public AccountController(AccountService accountService, UserRepository userRepository) {
		super();
		this.accountService = accountService;
		this.userRepository = userRepository;
	}

	@GetMapping
	public ResponseEntity<List<AccountDTO>> getUserAccounts() {
		try {
			String userEmail = SecurityContextHolder.getContext().getAuthentication().getName();
			UserModel user = userRepository.findByEmail(userEmail)
					.orElseThrow(() -> new UsernameNotFoundException("User not found"));

			List<AccountDTO> accounts = accountService.getAccountsForCurrentUser(user.getId());
			return ResponseEntity.ok(accounts);
		} catch (UsernameNotFoundException e) {
			// TODO: handle exception
			logger.warn("Attempt to fetch accounts for non-existent user.", e);
			return ResponseEntity.status(401).build();
		} catch (Exception e) {
			// TODO: handle exception
			logger.warn("Error fetching user accounts", e);
			return ResponseEntity.internalServerError().build();
		}
	}
}
