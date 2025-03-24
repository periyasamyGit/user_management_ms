package com.user.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class TransactionEvent {
	private String transactionId;
	private String username;
	private double amount;
	private String transactionType; // CREDIT, DEBIT
}
