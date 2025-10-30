package com.example.subintel.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.example.subintel.model.AccountModel;

@Repository
public interface AccountRepository extends JpaRepository<AccountModel, Long> {
	Optional<AccountModel> findByAccountId(String accountId);

	List<AccountModel> findByPlaidItemModel_UserModel_Id(Long userId);
}
