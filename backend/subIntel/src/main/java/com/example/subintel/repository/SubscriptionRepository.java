package com.example.subintel.repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import com.example.subintel.model.SubscriptionModel;

@Repository
public interface SubscriptionRepository extends JpaRepository<SubscriptionModel, Long> {
	List<SubscriptionModel> findByUserModel_Id(Long userId);

	Optional<SubscriptionModel> findByUserModel_IdAndMerchantName(Long userId, String merchantName);
	
	List<SubscriptionModel> findByUserModel_IdAndIsActiveTrue(Long userId);

	@Query("SELECT s FROM SubscriptionModel s JOIN FETCH s.userModel WHERE s.isActive = true AND s.nextDueDate BETWEEN :start AND :end")
	List<SubscriptionModel> findByIsActiveTrueAndNextDueDateBetween(LocalDate start, LocalDate end);
}
