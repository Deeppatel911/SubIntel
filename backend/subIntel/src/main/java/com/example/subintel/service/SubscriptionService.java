package com.example.subintel.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.subintel.dto.SubscriptionDTO;
import com.example.subintel.dto.SubscriptionRequestDTO;
import com.example.subintel.model.Frequency;
import com.example.subintel.model.SubscriptionModel;
import com.example.subintel.model.TransactionModel;
import com.example.subintel.model.UserModel;
import com.example.subintel.repository.SubscriptionRepository;
import com.example.subintel.repository.TransactionRepository;
import com.example.subintel.repository.UserRepository;

import jakarta.persistence.EntityNotFoundException;
import jakarta.validation.Valid;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class SubscriptionService {
	private static final Logger logger = LoggerFactory.getLogger(SubscriptionService.class);

	private final TransactionRepository transactionRepository;
	private final SubscriptionRepository subscriptionRepository;
	private final UserRepository userRepository;

	public SubscriptionService(TransactionRepository transactionRepository,
			SubscriptionRepository subscriptionRepository, UserRepository userRepository) {
		super();
		this.transactionRepository = transactionRepository;
		this.subscriptionRepository = subscriptionRepository;
		this.userRepository = userRepository;
	}

	@Transactional
	public void detectAndSaveSubscriptionsForUser(Long userId) { // List<Map<String, Object>>

		logger.info("Starting subscription detection for user ID: {}", userId);
		UserModel user = userRepository.findById(userId)
				.orElseThrow(() -> new UsernameNotFoundException("User not found with ID: " + userId));

		List<TransactionModel> transactions = transactionRepository.findTransactionsByUserIdOrderByLocalDateAsc(userId);

		if (transactions.isEmpty()) {
			logger.info("No transactions found for user ID: {}", userId);
		}

		Map<String, List<TransactionModel>> groupedByName = transactions.stream()
				.filter(t -> t.getAmount() < 0 && t.getName() != null)
				.collect(Collectors.groupingBy(TransactionModel::getName));

		groupedByName.forEach((name, transactionList) -> {
			logger.debug("Analyzing group: {}, Count: {}", name, transactionList.size());
			if (transactionList.size() < 3) {
				return;
			}

			Optional<DetectedPattern> patternOpt = findRecurringPattern(transactionList);
			if (patternOpt.isPresent()) {
				DetectedPattern pattern = patternOpt.get();
				TransactionModel latestTransaction = pattern.latestTransaction;

				logger.info("Pattern found for '{}': Frequency={}, Avg Amount={:.2f}, Last Date={}", name,
						pattern.frequency, pattern.averageAmount, latestTransaction.getLocalDate());

				Optional<SubscriptionModel> existingSubOpt = subscriptionRepository
						.findByUserModel_IdAndMerchantName(userId, name);

				SubscriptionModel subToSave = existingSubOpt.orElse(new SubscriptionModel());

				subToSave.setUserModel(user);
				subToSave.setMerchantName(name);
				subToSave.setEstimatedAmount(latestTransaction.getAmount());
				subToSave.setFrequency(pattern.frequency);
				subToSave.setLastPaymentDate(latestTransaction.getLocalDate());
				subToSave.setNextDueDate(calculateNextDueDate(latestTransaction.getLocalDate(), pattern.frequency));
				subToSave.setActive(true);

				subscriptionRepository.save(subToSave);
				logger.info("Saved/Updated subscription: {}", name);
			}
		});

		logger.info("Finished subscription detection for user ID: {}", userId);
		deactivateLapsedSubscriptions(userId, transactions);
	}

	private Optional<DetectedPattern> findRecurringPattern(List<TransactionModel> transactions) {
		transactions.sort(Comparator.comparing(TransactionModel::getLocalDate));
		String merchantName = transactions.get(0).getName();

		List<Long> daysBetweenPaymentsRaw = new ArrayList<>();
		List<Double> amounts = transactions.stream().map(TransactionModel::getAmount).collect(Collectors.toList());

		for (int i = 0; i < transactions.size() - 1; i++) {
			TransactionModel current = transactions.get(i);
			TransactionModel next = transactions.get(i + 1);
			daysBetweenPaymentsRaw.add(ChronoUnit.DAYS.between(current.getLocalDate(), next.getLocalDate()));
		}

		logger.debug("[{}] Transaction Count: {}", merchantName, transactions.size());
		logger.debug("[{}] Intervals (days): {}", merchantName, daysBetweenPaymentsRaw);
		logger.debug("[{}] Amounts: {}", merchantName, amounts);

		List<Long> daysBetweenPaymentsFiltered = daysBetweenPaymentsRaw.stream().filter(days -> days > 5)
				.collect(Collectors.toList());
		logger.debug("[{}] Filtered Intervals (days > 5): {}", merchantName, daysBetweenPaymentsFiltered);

		if (daysBetweenPaymentsFiltered.size() < 2) {
			logger.debug("[{}] Failed check: Not enough intervals ({})", merchantName,
					daysBetweenPaymentsFiltered.size());
			return Optional.empty();
		}

		double averageInterval = daysBetweenPaymentsFiltered.stream().mapToLong(Long::longValue).average().orElse(0.0);
		double averageAmount = amounts.stream().mapToDouble(Double::doubleValue).average().orElse(0.0);
		double maxAmountDeviation = amounts.stream().mapToDouble(amount -> Math.abs(amount - averageAmount)).max()
				.orElse(0.0);

		double amountTolerance = Math.max(Math.abs(averageAmount * 0.20), 2.0);
		logger.debug("[{}] Avg Interval: {:.1f}, Avg Amount: {:.2f}, Max Amount Dev: {:.2f}, Amount Tolerance: {:.2f}",
				merchantName, averageInterval, averageAmount, maxAmountDeviation, amountTolerance);

		if (maxAmountDeviation > amountTolerance) {
			logger.debug("Failed check: Amounts vary too much for '{}'. Max deviation {} > tolerance {}",
					transactions.get(0).getName(), maxAmountDeviation, amountTolerance);
			return Optional.empty();
		}

		Frequency initialDetection = Frequency.UNKNOWN;
		if (averageInterval >= 6 && averageInterval <= 9) {
			initialDetection = Frequency.WEEKLY;
		} else if (averageInterval >= 25 && averageInterval <= 35) {
			initialDetection = Frequency.MONTHLY;
		} else if (averageInterval >= 360 && averageInterval <= 370) {
			initialDetection = Frequency.YEARLY;
		}

		final Frequency detectedFrequency = initialDetection;
		logger.debug("[{}] Detected Frequency based on avg interval: {}", merchantName, detectedFrequency);
		if (detectedFrequency == Frequency.UNKNOWN) {
			logger.debug("[{}] Failed check: No standard frequency detected from avg interval {:.1f}", merchantName,
					averageInterval);
			return Optional.empty();
		}
		long matchingIntervals = daysBetweenPaymentsFiltered.stream().filter(days -> {
			if (detectedFrequency == Frequency.WEEKLY)
				return days >= 6 && days <= 9;
			if (detectedFrequency == Frequency.MONTHLY)
				return days >= 25 && days <= 35;
			if (detectedFrequency == Frequency.YEARLY)
				return days >= 360 && days <= 370;
			return false;
		}).count();

		double matchPercentage = daysBetweenPaymentsFiltered.isEmpty() ? 0
				: (double) matchingIntervals / daysBetweenPaymentsFiltered.size();

		logger.debug("[{}] Interval Consistency Check: {}/{} intervals match {}. Percentage: {:.2f}", merchantName,
				matchingIntervals, daysBetweenPaymentsFiltered.size(), detectedFrequency, matchPercentage);

		if (matchPercentage >= 0.65) {
			logger.debug("[{}] Success: Consistent pattern found!", merchantName);
			return Optional.of(
					new DetectedPattern(detectedFrequency, averageAmount, transactions.get(transactions.size() - 1)));
		} else {
			logger.debug("[{}] Failed check: Intervals inconsistent (Match % {:.2f} < 70%)", merchantName,
					matchPercentage * 100);
			return Optional.empty();
		}
	}

	private LocalDate calculateNextDueDate(LocalDate lastPaymentDate, Frequency frequency) {
		if (lastPaymentDate == null)
			return null;

		return switch (frequency) {
		case WEEKLY -> lastPaymentDate.plusWeeks(1);
		case MONTHLY -> lastPaymentDate.plusMonths(1);
		case YEARLY -> lastPaymentDate.plusYears(1);
		default -> null;
		};
	}

	private static class DetectedPattern {
		final Frequency frequency;
		final double averageAmount;
		final TransactionModel latestTransaction;

		public DetectedPattern(Frequency frequency, double averageAmount, TransactionModel latestTransaction) {
			super();
			this.frequency = frequency;
			this.averageAmount = averageAmount;
			this.latestTransaction = latestTransaction;
		}
	}

	public void deactivateLapsedSubscriptions(Long userId, List<TransactionModel> allTransactions) {
		logger.info("Checking for lapsed subscriptions for user ID: {}", userId);

		List<SubscriptionModel> activSubscriptions = subscriptionRepository.findByUserModel_IdAndIsActiveTrue(userId);

		for (SubscriptionModel sub : activSubscriptions) {
			boolean isOverDue = sub.getNextDueDate() != null
					&& sub.getNextDueDate().isBefore(LocalDate.now().minusDays(7));

			if (isOverDue) {
				boolean paymentFound = allTransactions.stream()
						.filter(t -> t.getName() != null && t.getName().equals(sub.getMerchantName()))
						.filter(t -> t.getAmount() < 0)
						.anyMatch(t -> t.getLocalDate().isAfter(sub.getLastPaymentDate()));

				if (!paymentFound) {
					logger.info("Deactivating lapsed subscription: '{}' (ID: {}) for user ID: {}",
							sub.getMerchantName(), sub.getSubscriptionId(), userId);
					sub.setActive(false);
					subscriptionRepository.save(sub);
				}
			}
		}
	}

	public List<SubscriptionDTO> getSubscriptionsForUser(Long userId) {
		logger.info("Fetching subscriptions for user ID: {}", userId);

		List<SubscriptionModel> subscriptions = subscriptionRepository.findByUserModel_Id(userId);

		if (subscriptions.isEmpty()) {
			logger.info("No subscriptions found for user ID: {}", userId);
			return Collections.emptyList();
		}

		List<SubscriptionDTO> subscriptionDTOs = subscriptions.stream().map(SubscriptionDTO::new)
				.collect(Collectors.toList());

		logger.info("Returning {} subscriptions for user ID: {}", subscriptionDTOs.size(), userId);
		return subscriptionDTOs;
	}

	public Map<String, Double> getSpendingBySubscription(Long userId) {
		logger.info("Calculating spending by subscription for user ID: {}", userId);

		List<SubscriptionModel> subscriptions = subscriptionRepository.findByUserModel_Id(userId);
		Map<String, Double> spendingMap = subscriptions.stream().filter(SubscriptionModel::isActive)
				.collect(Collectors.groupingBy(SubscriptionModel::getMerchantName,
						Collectors.summingDouble(sub -> Math.abs(sub.getEstimatedAmount()))));

		logger.info("Calculated spending for {} merchants for user ID: {}", spendingMap.size(), userId);
		return spendingMap;
	}

	@Transactional
	public SubscriptionDTO createSubscription(long userId, @Valid SubscriptionRequestDTO requestDTO) {
		// TODO Auto-generated method stub
		UserModel user = userRepository.findById(userId)
				.orElseThrow(() -> new UsernameNotFoundException("User not found with ID: " + userId));

		subscriptionRepository.findByUserModel_IdAndMerchantName(userId, requestDTO.getMerchantName())
				.ifPresent(sub -> {
					throw new IllegalArgumentException(
							"Subscription already exists for merchant: " + requestDTO.getMerchantName());
				});

		SubscriptionModel newSubscription = new SubscriptionModel();
		newSubscription.setUserModel(user);
		newSubscription.setMerchantName(requestDTO.getMerchantName());
		newSubscription.setEstimatedAmount(requestDTO.getEstimatedAmount());
		newSubscription.setFrequency(requestDTO.getFrequency());
		newSubscription.setLastPaymentDate(requestDTO.getLastPaymentDate());
		LocalDate nextDue = (requestDTO.getNextDueDate() != null) ? requestDTO.getNextDueDate()
				: calculateNextDueDate(
						requestDTO.getLastPaymentDate() != null ? requestDTO.getLastPaymentDate() : LocalDate.now(),
						requestDTO.getFrequency());
		newSubscription.setNextDueDate(nextDue);
		newSubscription.setActive(true);

		SubscriptionModel savedSubscription = subscriptionRepository.save(newSubscription);
		logger.info("Manually created subscription '{}' for user ID: {}", savedSubscription.getMerchantName(), userId);
		return new SubscriptionDTO(savedSubscription);
	}

	@Transactional
	public SubscriptionDTO updateSubscription(long userId, Long subscriptionId,
			@Valid SubscriptionRequestDTO requestDTO) {
		// TODO Auto-generated method stub
		SubscriptionModel existingSubscription = subscriptionRepository.findById(subscriptionId)
				.orElseThrow(() -> new EntityNotFoundException("Subscription not found with ID: " + subscriptionId));

		if (existingSubscription.getUserModel().getId() != userId) {
			throw new SecurityException("User does not have permission to delete this subscription.");
		}

		existingSubscription.setMerchantName(requestDTO.getMerchantName());
		existingSubscription.setEstimatedAmount(requestDTO.getEstimatedAmount());
		existingSubscription.setFrequency(requestDTO.getFrequency());
		existingSubscription.setLastPaymentDate(requestDTO.getLastPaymentDate());
		LocalDate nextDue = (requestDTO.getNextDueDate() != null) ? requestDTO.getNextDueDate()
				: calculateNextDueDate(
						requestDTO.getLastPaymentDate() != null ? requestDTO.getLastPaymentDate() : LocalDate.now(),
						requestDTO.getFrequency());
		existingSubscription.setNextDueDate(nextDue);

		SubscriptionModel updatedSubscription = subscriptionRepository.save(existingSubscription);
		logger.info("Updated subscription ID {} for user ID: {}", subscriptionId, userId);
		return new SubscriptionDTO(updatedSubscription);
	}

	@Transactional
	public void deleteSubscription(long userId, Long subscriptionId) {
		// TODO Auto-generated method stub
		SubscriptionModel existingSubscription = subscriptionRepository.findById(subscriptionId)
				.orElseThrow(() -> new EntityNotFoundException("Subscription not found with ID: " + subscriptionId));

		if (existingSubscription.getUserModel().getId() != userId) {
			throw new SecurityException("User does not have permission to delete this subscription.");
		}

		subscriptionRepository.delete(existingSubscription);
		logger.info("Deleted subscription ID {} for user ID: {}", subscriptionId, userId);
	}
}
