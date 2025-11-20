package com.example.subintel.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import com.example.subintel.dto.SpendingTrendDTO;
import com.example.subintel.model.TransactionModel;

@Repository
public interface TransactionRepository extends JpaRepository<TransactionModel, Long> {
	@Query("SELECT t FROM TransactionModel t WHERE t.accountModel.plaidItemModel.userModel.id = :userId ORDER BY t.localDate DESC")
	List<TransactionModel> findTransactionsByUserId(Long userId);

	Optional<TransactionModel> findByTransactionId(String transactionId);

	boolean existsByTransactionId(String transactionId);

	@Query("SELECT t FROM TransactionModel t WHERE t.accountModel.plaidItemModel.userModel.id = :userId ORDER BY t.localDate ASC")
	List<TransactionModel> findTransactionsByUserIdOrderByLocalDateAsc(Long userId);

	@Query("SELECT new com.example.subintel.dto.SpendingTrendDTO(TO_CHAR(t.localDate, 'YYYY-MM'), SUM(t.amount)) "
			+ "FROM TransactionModel t " + "WHERE t.accountModel.plaidItemModel.userModel.id = :userId "
			+ "AND t.amount < 0 " + "GROUP BY TO_CHAR(t.localDate, 'YYYY-MM') "
			+ "ORDER BY TO_CHAR(t.localDate, 'YYYY-MM') ASC")
	List<SpendingTrendDTO> findSpendingTrendByUserId(Long userId);
}
