package com.example.subintel.controller;

import java.util.Collections;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.example.subintel.dto.SubscriptionDTO;
import com.example.subintel.dto.SubscriptionRequestDTO;
import com.example.subintel.model.UserModel;
import com.example.subintel.repository.UserRepository;
import com.example.subintel.service.SubscriptionService;

import jakarta.persistence.EntityNotFoundException;
import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/subscriptions")
public class SubscriptionController {
	private static final Logger logger = LoggerFactory.getLogger(SubscriptionController.class);
	private final SubscriptionService subscriptionService;
	private final UserRepository userRepository;

	public SubscriptionController(SubscriptionService subscriptionService, UserRepository userRepository) {
		super();
		this.subscriptionService = subscriptionService;
		this.userRepository = userRepository;
	}

	@GetMapping
	public ResponseEntity<List<SubscriptionDTO>> getUserSubscriptions() {
		try {
			String userEmail = SecurityContextHolder.getContext().getAuthentication().getName();
			UserModel user = userRepository.findByEmail(userEmail)
					.orElseThrow(() -> new UsernameNotFoundException("User not found"));

			logger.info("Received GET request for subscriptions for user ID: {}", user.getId());
			List<SubscriptionDTO> subscriptions = subscriptionService.getSubscriptionsForUser(user.getId());

			return ResponseEntity.ok(subscriptions);
		} catch (UsernameNotFoundException e) {
			// TODO: handle exception
			logger.warn("Attempt to fetch subscriptions for non-existent user.", e);
			return ResponseEntity.status(401).build();
		} catch (Exception e) {
			// TODO: handle exception
			logger.warn("Attempt to fetch subscriptions for non-existent user.", e);
			return ResponseEntity.internalServerError().build();
		}
	}

	@PostMapping("/detect")
	public ResponseEntity<?> detectSubscriptions() {
		try {
			String userEmail = SecurityContextHolder.getContext().getAuthentication().getName();
			UserModel user = userRepository.findByEmail(userEmail)
					.orElseThrow(() -> new UsernameNotFoundException("User not found"));

			logger.info("Received GET request for subscriptions for user ID: {}", user.getId());
			subscriptionService.detectAndSaveSubscriptionsForUser(user.getId());

			return ResponseEntity.ok(Collections.singletonMap("message", "Subscription detection complete."));
		} catch (UsernameNotFoundException e) {
			// TODO: handle exception
			logger.warn("Attempt to fetch subscriptions for non-existent user.", e);
			return ResponseEntity.status(401).build();
		} catch (Exception e) {
			// TODO: handle exception
			logger.warn("Attempt to fetch subscriptions for non-existent user.", e);
			return ResponseEntity.internalServerError().build();
		}
	}

	@GetMapping("/spending-summary")
	public ResponseEntity<Map<String, Double>> getSubscriptionSpendingSummary() {
		logger.info(">>> HIT: GET /api/subscriptions/spending-summary");
		try {
			String userEmail = SecurityContextHolder.getContext().getAuthentication().getName();
			UserModel user = userRepository.findByEmail(userEmail)
					.orElseThrow(() -> new UsernameNotFoundException("User not found"));

			logger.info("Received GET request for spending summary for user ID: {}", user.getId());
			Map<String, Double> spendingData = subscriptionService.getSpendingBySubscription(user.getId());

			return ResponseEntity.ok(spendingData);
		} catch (UsernameNotFoundException e) {
			// TODO: handle exception
			logger.warn("Attempt to fetch spending summary for non-existent user.", e);
			return ResponseEntity.status(401).build();
		} catch (Exception e) {
			// TODO: handle exception
			logger.error("Error fetching spending summary", e);
			return ResponseEntity.internalServerError().build();
		}
	}

	@PostMapping
	public ResponseEntity<?> createSubscription(@Valid @RequestBody SubscriptionRequestDTO requestDTO) {
		try {
			String userEmail = SecurityContextHolder.getContext().getAuthentication().getName();
			UserModel user = userRepository.findByEmail(userEmail)
					.orElseThrow(() -> new UsernameNotFoundException("User not found"));

			SubscriptionDTO createdSubscription = subscriptionService.createSubscription(user.getId(), requestDTO);
			return ResponseEntity.status(201).body(createdSubscription);
		} catch (UsernameNotFoundException e) {
			// TODO: handle exception
			logger.warn("Attempt to create subscription for non-existent user.", e);
			return ResponseEntity.status(401).build();
		} catch (IllegalArgumentException e) {
			// TODO: handle exception
			logger.warn("Attempt to create duplicate subscription: {}", e.getMessage());
			return ResponseEntity.badRequest().body(e.getMessage());
		} catch (Exception e) {
			// TODO: handle exception
			logger.error("Error creating subscription", e);
			return ResponseEntity.internalServerError().build();
		}
	}

	@PutMapping("/{id}")
	public ResponseEntity<?> updateSubscription(@PathVariable Long id,
			@Valid @RequestBody SubscriptionRequestDTO requestDTO) {
		try {
			String userEmail = SecurityContextHolder.getContext().getAuthentication().getName();
			UserModel user = userRepository.findByEmail(userEmail)
					.orElseThrow(() -> new UsernameNotFoundException("User not found"));

			SubscriptionDTO updatedSubscription = subscriptionService.updateSubscription(user.getId(), id, requestDTO);
			return ResponseEntity.status(201).body(updatedSubscription);
		} catch (UsernameNotFoundException e) {
			// TODO: handle exception
			logger.warn("Attempt to update subscription for non-existent user.", e);
			return ResponseEntity.status(401).build();
		} catch (EntityNotFoundException e) {
			// TODO: handle exception
			logger.warn("Attempt to update non-existent subscription (ID: {}): {}", id, e.getMessage());
			return ResponseEntity.notFound().build();
		} catch (SecurityException e) {
			logger.warn("Security violation: Attempt to update subscription (ID: {}) belonging to another user.", id);
			return ResponseEntity.status(403).build(); // 403 Forbidden
		} catch (Exception e) {
			// TODO: handle exception
			logger.error("Error updating subscription", e);
			return ResponseEntity.internalServerError().build();
		}
	}

	@DeleteMapping("/{id}")
	public ResponseEntity<?> deleteSubscription(@PathVariable Long id) {
		try {
			String userEmail = SecurityContextHolder.getContext().getAuthentication().getName();
			UserModel user = userRepository.findByEmail(userEmail)
					.orElseThrow(() -> new UsernameNotFoundException("User not found"));

			subscriptionService.deleteSubscription(user.getId(), id);
			return ResponseEntity.noContent().build();
		} catch (UsernameNotFoundException e) {
			// TODO: handle exception
			logger.warn("Attempt to delete subscription for non-existent user.", e);
			return ResponseEntity.status(401).build();
		} catch (EntityNotFoundException e) {
			logger.warn("Attempt to delete non-existent subscription (ID: {}): {}", id, e.getMessage());
			return ResponseEntity.notFound().build(); // 404 Not Found
		} catch (SecurityException e) {
			logger.warn("Security violation: Attempt to delete subscription (ID: {}) belonging to another user.", id);
			return ResponseEntity.status(403).build(); // 403 Forbidden
		} catch (Exception e) {
			// TODO: handle exception
			logger.error("Error deleting subscription", e);
			return ResponseEntity.internalServerError().build();
		}
	}
}
