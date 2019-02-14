package com.ninja.circuitbreakerexample;

import static org.assertj.core.api.Assertions.assertThatThrownBy;

import io.github.resilience4j.circuitbreaker.CircuitBreaker;
import io.github.resilience4j.circuitbreaker.CircuitBreakerConfig;
import io.github.resilience4j.circuitbreaker.CircuitBreakerOpenException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import okhttp3.mockwebserver.MockResponse;
import okhttp3.mockwebserver.MockWebServer;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.web.reactive.function.client.WebClient;

class WebControllerTest {
	private WebController webController;
	private MockWebServer server;

	@BeforeEach
	void setup() {
		server = new MockWebServer();
		webController = new WebController(WebClient.create(server.url("/").toString()));

		webController.setWebClientCircuitBreaker(CircuitBreaker.of(
				"foo",
				CircuitBreakerConfig.custom()
						.ringBufferSizeInClosedState(3)
						.build()
		));
	}

	@Test
	void test() {
		server.enqueue(new MockResponse()
				.setBody("{\"msg\": \"hello\"}")
				.setHeader("Content-Type", "application/json")
				.setBodyDelay(1, TimeUnit.SECONDS)
		);

		// after 3 times time out error, circuit breaker will throw exception
		for (int i = 0; i < 3; i++) {
			assertThatThrownBy(() -> webController.callApi().block()).hasCauseInstanceOf(TimeoutException.class);
		}
		assertThatThrownBy(() -> webController.callApi().block()).isInstanceOf(CircuitBreakerOpenException.class);
	}
}
