package com.example.subintel.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import com.example.subintel.model.UserModel;

@Repository
public interface UserRepository extends JpaRepository<UserModel, Long> {
	Optional<UserModel> findByEmail(String email);
	
	@Query("SELECT u FROM UserModel u LEFT JOIN FETCH u.plaidItems")
    List<UserModel> findAllWithPlaidItems();
}
