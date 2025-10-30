package com.example.subintel.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.example.subintel.model.SubscriptionModel;

@Repository
public interface SubscriptionRepository extends JpaRepository<SubscriptionModel, Long>{
	List<SubscriptionModel> findByUserModel_Id(Long userId);
	
	Optional<SubscriptionModel> findByUserModel_IdAndMerchantName(Long userId, String merchantName);
}
