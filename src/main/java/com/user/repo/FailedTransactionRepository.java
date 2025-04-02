package com.user.repo;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.user.entity.FailedTransaction;

@Repository
public interface FailedTransactionRepository extends JpaRepository<FailedTransaction, Long> {
}
