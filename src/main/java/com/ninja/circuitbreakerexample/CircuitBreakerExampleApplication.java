package com.ninja.circuitbreakerexample;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.web.reactive.function.client.WebClient;

@SpringBootApplication
public class CircuitBreakerExampleApplication {

	public static void main(String[] args) {
		SpringApplication.run(CircuitBreakerExampleApplication.class, args);
	}

	@Bean
	WebClient webClient(WebClient.Builder builder) {
		return builder.build();
	}
}

