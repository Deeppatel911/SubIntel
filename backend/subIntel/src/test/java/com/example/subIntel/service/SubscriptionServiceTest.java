package com.example.subintel.service;

import java.time.LocalDate;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.example.subintel.model.Frequency;
import com.example.subintel.model.SubscriptionModel;
import com.example.subintel.model.TransactionModel;
import com.example.subintel.model.UserModel;
import com.example.subintel.repository.SubscriptionRepository;
import com.example.subintel.repository.TransactionRepository;
import com.example.subintel.repository.UserRepository;

import static org.mockito.Mockito.*;
import static org.assertj.core.api.Assertions.assertThat;

@ExtendWith(MockitoExtension.class)
public class SubscriptionServiceTest {
	@Mock
	private TransactionRepository transactionRepository;
	@Mock
	private SubscriptionRepository subscriptionRepository;
	@Mock
	private UserRepository userRepository;

	@InjectMocks
	private SubscriptionService subscriptionService;

	@Test
	void whenMonthlyPatternDetected_thenSaveSubscription() {
		Long userId = 1L;
		UserModel user = new UserModel();
		user.setId(userId);

		when(userRepository.findById(userId)).thenReturn(Optional.of(user));

		TransactionModel t1 = new TransactionModel();
		t1.setName("United Airlines");
		t1.setAmount(-50.0);
		t1.setLocalDate(LocalDate.of(2025, 1, 15));

		TransactionModel t2 = new TransactionModel();
		t2.setName("United Airlines");
		t2.setAmount(-50.0);
		t2.setLocalDate(LocalDate.of(2025, 2, 15));

		TransactionModel t3 = new TransactionModel();
		t3.setName("United Airlines");
		t3.setAmount(-50.0);
		t3.setLocalDate(LocalDate.of(2025, 3, 15));

		List<TransactionModel> transactions = Arrays.asList(t1, t2, t3);

		when(transactionRepository.findTransactionsByUserIdOrderByLocalDateAsc(userId)).thenReturn(transactions);

		subscriptionService.detectAndSaveSubscriptionsForUser(userId);

		ArgumentCaptor<SubscriptionModel> subscriptionCaptor = ArgumentCaptor.forClass(SubscriptionModel.class);
		verify(subscriptionRepository, times(1)).save(subscriptionCaptor.capture());

		SubscriptionModel savedSub = subscriptionCaptor.getValue();
		assertThat(savedSub.getMerchantName()).isEqualTo("United Airlines");
		assertThat(savedSub.getFrequency()).isEqualTo(Frequency.MONTHLY);
		assertThat(savedSub.getEstimatedAmount()).isEqualTo(-50.0);
	}
}
