package com.example.subintel.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.example.subintel.model.PlaidItemModel;

@Repository
public interface PlaidItemRepository extends JpaRepository<PlaidItemModel, Long> {
	Optional<PlaidItemModel> findByItemIdAndUserModel_Id(String itemId, Long userId);
}