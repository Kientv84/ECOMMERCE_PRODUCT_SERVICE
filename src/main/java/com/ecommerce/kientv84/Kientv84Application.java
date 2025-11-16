package com.ecommerce.kientv84;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableAsync;

@SpringBootApplication
@EnableAsync
public class Kientv84Application {

	public static void main(String[] args) {
		System.out.println("Lang nghe port 3001 .....");
		SpringApplication.run(Kientv84Application.class, args);
	}

}
