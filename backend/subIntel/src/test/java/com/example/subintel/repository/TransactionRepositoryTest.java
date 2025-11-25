package com.example.subintel.repository;

import java.time.LocalDate;
import java.util.List;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import com.example.subintel.model.*;

import static org.assertj.core.api.Assertions.assertThat;

@Testcontainers
@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
class TransactionRepositoryTest {
	@Container
	static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:15-alpine");

	@DynamicPropertySource
	static void configureProperties(DynamicPropertyRegistry registry) {
		registry.add("spring.datasource.url", postgres::getJdbcUrl);
		registry.add("spring.datasource.username", postgres::getUsername);
		registry.add("spring.datasource.password", postgres::getPassword);
	}

	@Autowired
	private TransactionRepository transactionRepository;
	@Autowired
	private UserRepository userRepository;
	@Autowired
	private PlaidItemRepository plaidItemRepository;
	@Autowired
	private AccountRepository accountRepository;

	private UserModel testUser;

	@BeforeEach
	void setUp() {
		// Create a user
		testUser = new UserModel();
		testUser.setEmail("testuser@example.com");
		testUser.setPassword("password");
		testUser.setFirstName("Test");
		testUser.setLastName("User");
		userRepository.save(testUser);

		PlaidItemModel plaidItem = new PlaidItemModel();
		plaidItem.setUserModel(testUser);
		plaidItem.setItemId("item123");
		plaidItem.setAccessToken("token123");
		plaidItemRepository.save(plaidItem);

		AccountModel account = new AccountModel();
		account.setPlaidItemModel(plaidItem);
		account.setAccountId("account123");
		account.setName("Test Account");
		accountRepository.save(account);

		TransactionModel transaction = new TransactionModel();
		transaction.setAccountModel(account);
		transaction.setTransactionId("tx123");
		transaction.setName("Test Transaction");
		transaction.setAmount(-50.0);
		transaction.setLocalDate(LocalDate.now());
		transaction.setCategory("Test");
		transactionRepository.save(transaction);
	}

	@AfterEach
	void tearDown() {
		transactionRepository.deleteAll();
		accountRepository.deleteAll();
		plaidItemRepository.deleteAll();
		userRepository.deleteAll();
	}

	@Test
	void whenFindTransactionsByUserId_thenReturnTransactions() {
		List<TransactionModel> foundTransactions = transactionRepository.findTransactionsByUserId(testUser.getId());

		assertThat(foundTransactions).hasSize(1);
		assertThat(foundTransactions.get(0).getName()).isEqualTo("Test Transaction");
	}
}
