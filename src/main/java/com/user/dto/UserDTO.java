package com.user.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class UserDTO {

	@NotBlank(message = "Username cannot be empty or null")
	private String username;

	@NotBlank(message = "Email cannot be empty or null")
	@Email(message = "Invalid email format")
	private String email;

	@NotBlank(message = "Phone number cannot be empty or null")
	@Pattern(regexp = "^[0-9]{10}$", message = "Phone number must be exactly 10 digits")
	private String phone;

//	@NotNull(message = "Transaction limit cannot be null")
	private Double transactionLimit;
}
