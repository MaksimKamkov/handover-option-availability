package ru.mvideo.handoveroptionavailability.config.externalclient;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.reactive.function.client.WebClient;
import ru.mvideo.lib.client.config.ClientProperties;
import ru.mvideo.lib.client.config.WebClientFactory;

@Configuration
public class MspLogisticClientConfig {

	@Bean
	public WebClient mspLogisticWebClient(
			WebClient.Builder webClientBuilder,
			@Qualifier("mspLogisticClientProperties") ClientProperties properties) {
		return WebClientFactory.createClient(webClientBuilder, properties);
	}

	@Bean
	@ConfigurationProperties(prefix = "ru.mvideo.msp")
	public AuthorizedClientProperties mspLogisticClientProperties() {
		return new AuthorizedClientProperties();
	}
}
