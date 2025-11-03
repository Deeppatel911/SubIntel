package com.example.subintel.service;

import java.time.LocalDate;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import com.example.subintel.model.SubscriptionModel;
import com.example.subintel.model.UserModel;
import com.example.subintel.repository.SubscriptionRepository;
import com.example.subintel.repository.UserRepository;

@Service
public class ScheduledTaskService {
	private static final Logger logger = LoggerFactory.getLogger(ScheduledTaskService.class);
	private final UserRepository userRepository;
	private final PlaidService plaidService;
	private final SubscriptionService subscriptionService;
	private final SubscriptionRepository subscriptionRepository;
	private final EmailService emailService;

	public ScheduledTaskService(UserRepository userRepository, PlaidService plaidService,
			SubscriptionService subscriptionService, SubscriptionRepository subscriptionRepository,
			EmailService emailService) {
		super();
		this.userRepository = userRepository;
		this.plaidService = plaidService;
		this.subscriptionService = subscriptionService;
		this.subscriptionRepository = subscriptionRepository;
		this.emailService = emailService;
	}

	@Scheduled(cron = "0 0 5 * * ?")
	public void syncUserData() {
		logger.info("Starting daily scheduled job: Syncing all user data...");

		List<UserModel> allUsers = userRepository.findAllWithPlaidItems();
		for (UserModel user : allUsers) {
			logger.info("Processing user ID: {}", user.getId());
			try {
				plaidService.fetchAndSaveTransactions(user);
				subscriptionService.detectAndSaveSubscriptionsForUser(user.getId());

				logger.info("Processing user ID: {}", user.getId());
			} catch (Exception e) {
				// TODO: handle exception
				logger.info("Processing user ID: {}", user.getId());
			}
		}
		logger.info("Finished daily scheduled job.");
	}

	@Scheduled(cron = "0 0 9 * * ?")
	public void sendUpcomingSubscriptionReminders() {
		logger.info("Starting daily subscription reminder job...");

		LocalDate today = LocalDate.now();
		LocalDate threeDaysFromNow = today.plusDays(3);

		List<SubscriptionModel> subscriptionsDueSoon = subscriptionRepository
				.findByIsActiveTrueAndNextDueDateBetween(today, threeDaysFromNow);

		logger.info("Found {} subscriptions due soon. Sending reminders...", subscriptionsDueSoon.size());

		for (SubscriptionModel subscription : subscriptionsDueSoon) {
			try {
				UserModel user = subscription.getUserModel();
				if (user != null && user.getEmail() != null) {
					logger.info("Sending reminder for subscription ID {} ('{}') to user ID {}",
							subscription.getSubscriptionId(), subscription.getMerchantName(), user.getId());
					emailService.sendSubscriptionReminder(user, subscription);
				} else {
					logger.warn("Subscription ID {} has no associated user or user has no email. Skipping.",
							subscription.getSubscriptionId());
				}
			} catch (Exception e) {
				// TODO: handle exception
				logger.error("Failed to send reminder for subscription ID {}: {}", subscription.getSubscriptionId(),
						e.getMessage(), e);
			}
		}
	}
}
