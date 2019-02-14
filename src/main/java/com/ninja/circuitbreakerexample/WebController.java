package com.ninja.circuitbreakerexample;

import io.github.resilience4j.circuitbreaker.CircuitBreaker;
import io.github.resilience4j.circuitbreaker.autoconfigure.CircuitBreakerProperties;
import io.github.resilience4j.reactor.circuitbreaker.operator.CircuitBreakerOperator;
import java.time.Duration;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

/**
 * Below PR will simplify the code.
 * https://github.com/resilience4j/resilience4j/pull/303
 */
@Slf4j
@RestController
@RequiredArgsConstructor
public class WebController {
	private final WebClient webClient;

	@Autowired // props will be auto configured from application.yml
	public void setCircuitBreakerProperties(CircuitBreakerProperties circuitBreakerProperties) {
		webClientCircuitBreaker = CircuitBreaker.of("webClientCircuitBreaker", circuitBreakerProperties.createCircuitBreakerConfig("webClientCircuitBreaker"));
	}

	@Setter // test purpose
	private CircuitBreaker webClientCircuitBreaker;

	@GetMapping("/callApi")
	Mono<Map<String, Object>> callApi() {
		return webClient.get()
				.uri("/timeout")
				.retrieve()
				.bodyToMono(new ParameterizedTypeReference<Map<String, Object>>() {})
				.timeout(Duration.ofMillis(500))
				.transform(CircuitBreakerOperator.of(webClientCircuitBreaker));
	}
}
