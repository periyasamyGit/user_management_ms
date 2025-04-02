package com.user.exception;

import java.util.HashMap;
import java.util.Map;

import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import com.user.dto.ApiResponse;

@RestControllerAdvice
public class GlobalExceptionHandler {

	// ✅ Handle User Not Found Exception
	@ExceptionHandler(UserNotFoundException.class)
	public ResponseEntity<ApiResponse<String>> handleUserNotFound(UserNotFoundException ex) {
		ApiResponse<String> response = new ApiResponse<>("ERROR", ex.getMessage(), null);
		return new ResponseEntity<>(response, HttpStatus.NOT_FOUND);
	}

	// ✅ Handle Invalid Input Validation Errors
	@ExceptionHandler(MethodArgumentNotValidException.class)
	public ResponseEntity<ApiResponse<Map<String, String>>> handleValidationException(
			MethodArgumentNotValidException ex) {
		Map<String, String> errors = new HashMap<>();
		ex.getBindingResult().getFieldErrors().forEach(error -> {
			errors.put(error.getField(), error.getDefaultMessage());
		});

		ApiResponse<Map<String, String>> response = new ApiResponse<>("ERROR", "Validation failed", errors);
		return new ResponseEntity<>(response, HttpStatus.BAD_REQUEST);
	}

	@ExceptionHandler(DataIntegrityViolationException.class)
	public ResponseEntity<ApiResponse<String>> handleDataIntegrityViolation(DataIntegrityViolationException ex) {
		String message = "Duplicate entry! Field value must be unique.";

		// Extract the violated constraint from exception message
		String errorMessage = ex.getMostSpecificCause().getMessage();

		if (errorMessage.contains("Unique index or primary key violation")) {
			// Extract field name using regex or splitting
			String[] parts = errorMessage.split("on PUBLIC.");
			if (parts.length > 1) {
				String constraintPart = parts[1].split("\\(")[1]; // Get field name part
				String violatedField = constraintPart.split(" ")[0]; // Extract field name
				message = violatedField.toUpperCase() + " is already in use. Please choose another.";
			}
		}

		// Prepare API response
		ApiResponse<String> response = new ApiResponse<>("ERROR", message, null);
		return new ResponseEntity<>(response, HttpStatus.CONFLICT);
	}

	// ✅ Handle Generic Exceptions
	@ExceptionHandler(Exception.class)
	public ResponseEntity<ApiResponse<String>> handleGenericException(Exception ex) {
		ApiResponse<String> response = new ApiResponse<>("ERROR", "An unexpected error occurred!", ex.getMessage());
		return new ResponseEntity<>(response, HttpStatus.INTERNAL_SERVER_ERROR);
	}
}
