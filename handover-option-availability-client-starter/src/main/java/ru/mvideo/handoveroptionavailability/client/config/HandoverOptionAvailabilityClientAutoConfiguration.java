package ru.mvideo.handoveroptionavailability.client.config;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import io.netty.channel.ChannelOption;
import io.netty.handler.timeout.ReadTimeoutHandler;
import java.util.concurrent.TimeUnit;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.client.reactive.ReactorClientHttpConnector;
import org.springframework.http.codec.json.Jackson2JsonDecoder;
import org.springframework.http.codec.json.Jackson2JsonEncoder;
import org.springframework.web.reactive.function.client.ExchangeFilterFunction;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.netty.http.client.HttpClient;
import reactor.netty.resources.ConnectionProvider;
import ru.mvideo.handoveroptionavailability.client.HandoverOptionAvailabilityClientV1;
import ru.mvideo.handoveroptionavailability.client.HandoverOptionAvailabilityClientV2;
import ru.mvideo.handoveroptionavailability.client.config.properties.HandoverOptionAvailabilityClientProperties;
import ru.mvideo.lib.client.LogHelper;

@Configuration
public class HandoverOptionAvailabilityClientAutoConfiguration {

	private static final String CONNECTION_NAME = "handover-option-availability-client-starter";

	@Bean
	public HandoverOptionAvailabilityClientProperties handoverOptionAvailabilityClientProperties() {
		return new HandoverOptionAvailabilityClientProperties();
	}

	public ObjectMapper handoverOptionAvailabilityClientObjectMapper() {
		return new ObjectMapper()
				.registerModule(new JavaTimeModule())
				.setSerializationInclusion(JsonInclude.Include.NON_NULL)
				.setSerializationInclusion(JsonInclude.Include.NON_EMPTY)
				.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false)
				.configure(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS, false);
	}

	@Bean
	public LogHelper handoverOptionAvailabilityClientLogHelper() {
		return new LogHelper(handoverOptionAvailabilityClientObjectMapper());
	}

	@Bean
	public WebClient handoverOptionAvailabilityWebClient(WebClient.Builder clientBuilder,
	                                                     HandoverOptionAvailabilityClientProperties handoverOptionAvailabilityClientProperties,
	                                                     @Qualifier("kAuthExchangeFilterFunction") ExchangeFilterFunction exchangeFilterFunction) {
		var objectMapper = handoverOptionAvailabilityClientObjectMapper();

		var connectionProvider = ConnectionProvider.builder(CONNECTION_NAME)
				.maxIdleTime(handoverOptionAvailabilityClientProperties.getMaxIdleTime())
				.build();

		var httpClient = HttpClient.create(connectionProvider)
				.option(
						ChannelOption.CONNECT_TIMEOUT_MILLIS,
						(int) handoverOptionAvailabilityClientProperties.getConnectionTimeout().toMillis()
				).doOnConnected(connection ->
						connection.addHandlerLast(
								new ReadTimeoutHandler(handoverOptionAvailabilityClientProperties.getReadTimeout().toMillis(),
										TimeUnit.MILLISECONDS))
				);

		var clientConnector = new ReactorClientHttpConnector(httpClient);

		return clientBuilder
				.defaultHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
				.baseUrl(handoverOptionAvailabilityClientProperties.getHost())
				.clientConnector(clientConnector)
				.codecs(clientDefaultCodecsConfigurer -> {
					clientDefaultCodecsConfigurer.defaultCodecs()
							.jackson2JsonEncoder(new Jackson2JsonEncoder(objectMapper, MediaType.APPLICATION_JSON));
					clientDefaultCodecsConfigurer.defaultCodecs()
							.jackson2JsonDecoder(new Jackson2JsonDecoder(objectMapper, MediaType.APPLICATION_JSON));
				})
				.filter(exchangeFilterFunction)
				.build();
	}

	@Bean
	public HandoverOptionAvailabilityClientV1 handoverOptionAvailabilityClientV1(
			@Qualifier("handoverOptionAvailabilityWebClient") WebClient handoverOptionAvailabilityWebClient,
			@Qualifier("handoverOptionAvailabilityClientLogHelper") LogHelper handoverOptionAvailAbilityClientLogHelper) {

		return new HandoverOptionAvailabilityClientV1(handoverOptionAvailabilityWebClient, handoverOptionAvailAbilityClientLogHelper);
	}

	@Bean
	public HandoverOptionAvailabilityClientV2 handoverOptionAvailabilityClientV2(
			@Qualifier("handoverOptionAvailabilityWebClient") WebClient handoverOptionAvailabilityWebClient,
			@Qualifier("handoverOptionAvailabilityClientLogHelper") LogHelper handoverOptionAvailAbilityClientLogHelper) {

		return new HandoverOptionAvailabilityClientV2(handoverOptionAvailabilityWebClient, handoverOptionAvailAbilityClientLogHelper);
	}

}
