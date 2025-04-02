package com.user.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.user.dto.ApiResponse;
import com.user.dto.UserDTO;
import com.user.repo.UserRepository;
import com.user.service.UserService;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/users")
public class UserController {

	@Autowired
	private UserService userService;

	@Autowired
	private UserRepository userRepository;

	@PostMapping("/register")
	public ApiResponse<String> registerUser(@Valid @RequestBody UserDTO userDTO) {
		return userService.registerUser(userDTO);
	}

	@PutMapping("/update")
	public ApiResponse<String> updateUser(@Valid @RequestBody UserDTO userDTO) {
		return userService.updateUser(userDTO);
	}

	@GetMapping("/validate/{username}")
	public boolean validateUser(@PathVariable String username) {
		return userRepository.findByUsername(username).isPresent();
	}

}
