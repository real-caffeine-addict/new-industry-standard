package com.psw.identities;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication(scanBasePackages = "com.psw")
public class IdentitiesApplication {

	public static void main(String[] args) {
		SpringApplication.run(IdentitiesApplication.class, args);
	}

}
