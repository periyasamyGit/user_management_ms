package com.user.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ApiResponse<T> {
	private String status; // "SUCCESS" / "FAILURE"
	private String message; // Detailed message
	private T data; // Actual payload

	public static <T> ApiResponse<T> success(String message, T data) {
		return new ApiResponse<>("SUCCESS", message, data);
	}

	public static <T> ApiResponse<T> failure(String message) {
		return new ApiResponse<>("FAILURE", message, null);
	}
}
