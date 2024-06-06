package ru.mvideo.handoveroptionavailability.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.reactive.function.client.ExchangeFilterFunction;

@Configuration
public class SecurityConfig {

	@Bean("kAuthExchangeFilterFunction")
	public ExchangeFilterFunction kAuthAuthorizedClientExchangeFilterFunction() {
		return new DummyExchangeFilterFunction();
	}
}
