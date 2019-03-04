package com.ninja.circuitbreakerexample;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import io.github.resilience4j.circuitbreaker.CircuitBreaker;
import io.github.resilience4j.circuitbreaker.CircuitBreakerConfig;
import io.github.resilience4j.circuitbreaker.CircuitBreakerOpenException;
import io.github.resilience4j.circuitbreaker.CircuitBreakerRegistry;
import io.github.resilience4j.circuitbreaker.autoconfigure.CircuitBreakerProperties;
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
		CircuitBreakerRegistry mockRegistry = mock(CircuitBreakerRegistry.class);

		when(mockRegistry.circuitBreaker(anyString(), any(CircuitBreakerConfig.class)))
						.thenReturn(CircuitBreaker.of(
								"foo",
								CircuitBreakerConfig.custom()
										.ringBufferSizeInClosedState(3)
										.build()));

		webController = new WebController(
				WebClient.create(server.url("/").toString()),
				mockRegistry,
				new CircuitBreakerProperties()
		);

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
