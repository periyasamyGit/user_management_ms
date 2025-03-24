package com.user;

import org.springframework.boot.Banner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class UserManagementServiceApplication {

	public static void main(String[] args) {

		SpringApplication app = new SpringApplication(UserManagementServiceApplication.class);
		app.setBannerMode(Banner.Mode.CONSOLE); // Display banner
		app.run(args);
	}

}
