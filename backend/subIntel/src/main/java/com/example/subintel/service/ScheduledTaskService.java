package com.example.subintel.service;

import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import com.example.subintel.model.UserModel;
import com.example.subintel.repository.UserRepository;

@Service
public class ScheduledTaskService {
	private static final Logger logger = LoggerFactory.getLogger(ScheduledTaskService.class);
	private final UserRepository userRepository;
	private final PlaidService plaidService;
	private final SubscriptionService subscriptionService;

	public ScheduledTaskService(UserRepository userRepository, PlaidService plaidService,
			SubscriptionService subscriptionService) {
		super();
		this.userRepository = userRepository;
		this.plaidService = plaidService;
		this.subscriptionService = subscriptionService;
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
}
