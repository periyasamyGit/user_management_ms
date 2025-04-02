package com.user.entity;

import java.time.LocalDateTime;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "failed_transactions")
public class FailedTransaction {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@Column(name = "transaction_id", nullable = false, unique = true)
	private String transactionId;

	@Column(name = "username", nullable = false)
	private String username;

	@Column(name = "transaction_type", nullable = false)
	private String transactionType;

	@Column(name = "amount", nullable = false)
	private double amount;

	@Column(name = "error_message", nullable = false, length = 500)
	private String errorMessage;

	@Column(name = "status", nullable = false)
	private String status; // FAILED, REPROCESSED, etc.

	@Column(name = "reprocessed", nullable = false)
	private boolean reprocessed;

	@Column(name = "failed_at", nullable = false)
	private LocalDateTime failedAt = LocalDateTime.now();

	// Constructors
	public FailedTransaction() {
	}

	public FailedTransaction(String transactionId, String username, String transactionType, double amount,
			String errorMessage, String status, boolean reprocessed) {
		this.transactionId = transactionId;
		this.username = username;
		this.transactionType = transactionType;
		this.amount = amount;
		this.errorMessage = errorMessage;
		this.status = status;
		this.reprocessed = reprocessed;
		this.failedAt = LocalDateTime.now();
	}

	// Getters and Setters
	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public String getTransactionId() {
		return transactionId;
	}

	public void setTransactionId(String transactionId) {
		this.transactionId = transactionId;
	}

	public String getUsername() {
		return username;
	}

	public void setUsername(String username) {
		this.username = username;
	}

	public String getTransactionType() {
		return transactionType;
	}

	public void setTransactionType(String transactionType) {
		this.transactionType = transactionType;
	}

	public double getAmount() {
		return amount;
	}

	public void setAmount(double amount) {
		this.amount = amount;
	}

	public String getErrorMessage() {
		return errorMessage;
	}

	public void setErrorMessage(String errorMessage) {
		this.errorMessage = errorMessage;
	}

	public String getStatus() {
		return status;
	}

	public void setStatus(String status) {
		this.status = status;
	}

	public boolean isReprocessed() {
		return reprocessed;
	}

	public void setReprocessed(boolean reprocessed) {
		this.reprocessed = reprocessed;
	}

	public LocalDateTime getFailedAt() {
		return failedAt;
	}

	public void setFailedAt(LocalDateTime failedAt) {
		this.failedAt = failedAt;
	}
}
