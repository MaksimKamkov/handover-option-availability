package ru.mvideo.handoveroptionavailability.client;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.when;

import java.net.URI;
import java.nio.file.Path;
import java.util.function.Consumer;
import java.util.function.Function;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.util.DefaultUriBuilderFactory;
import org.springframework.web.util.UriBuilder;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;
import ru.mvideo.handoveroptionavailability.api.ApiContract;
import ru.mvideo.handoveroptionavailability.model.AvailableHandoverOptionRequest;
import ru.mvideo.handoveroptionavailability.model.DeliveryProvidersRequest;
import ru.mvideo.handoveroptionavailability.model.DeliveryRequest;
import ru.mvideo.handoveroptionavailability.model.PickupRequest;
import ru.mvideo.lib.client.LogHelper;

@ExtendWith(MockitoExtension.class)
class HandoverOptionAvailabilityV1ClientTest {

	@InjectMocks
	private HandoverOptionAvailabilityClientV1 service;

	@Mock
	private WebClient webClient;

	@Mock
	WebClient.RequestHeadersUriSpec uriSpec;

	@Mock
	WebClient.RequestBodyUriSpec bodyUriSpec;

	@Mock
	private WebClient.RequestBodySpec requestBodySpec;

	@Mock
	private WebClient.RequestHeadersSpec requestHeadersSpec;

	@Mock
	private WebClient.ResponseSpec responseSpec;

	@Captor
	private ArgumentCaptor<Function<UriBuilder, URI>> uriCaptor;

	@Captor
	private ArgumentCaptor bodyCaptor;

	@Mock
	private LogHelper logHelper;


	@BeforeEach
	public void setUp() {
		when(webClient.post()).thenReturn(bodyUriSpec);
		when(bodyUriSpec.uri(uriCaptor.capture())).thenReturn(requestBodySpec);
		when(requestBodySpec.bodyValue(bodyCaptor.capture())).thenReturn(requestHeadersSpec);
		when(requestHeadersSpec.headers(any(Consumer.class))).thenReturn(requestHeadersSpec);
		when(requestHeadersSpec.retrieve()).thenReturn(responseSpec);
	}

	@Test
	void testHandoverOptions() {
		doReturn(Flux.empty()).when(responseSpec).bodyToFlux(any(ParameterizedTypeReference.class));

		var result = service.handoverOptions(new AvailableHandoverOptionRequest());
		StepVerifier.create(result).verifyComplete();
		var uri = uriCaptor.getValue().apply(new DefaultUriBuilderFactory().builder());

		Assertions.assertEquals(Path.of(ApiContract.BASE_V1, ApiContract.HANDOVER_OPTIONS).toString(), uri.toString());
	}

	@Test
	void testDelivery() {
		doReturn(Mono.empty()).when(responseSpec).bodyToMono(any(ParameterizedTypeReference.class));

		var result = service.delivery(new DeliveryRequest());
		StepVerifier.create(result).verifyComplete();
		var uri = uriCaptor.getValue().apply(new DefaultUriBuilderFactory().builder());

		Assertions.assertEquals(Path.of(ApiContract.BASE_V1, ApiContract.DELIVERY).toString(), uri.toString());
	}

	@Test
	void testDeliveryProviders() {
		doReturn(Mono.empty()).when(responseSpec).bodyToMono(any(ParameterizedTypeReference.class));

		var result = service.deliveryProviders(new DeliveryProvidersRequest());
		StepVerifier.create(result).verifyComplete();
		var uri = uriCaptor.getValue().apply(new DefaultUriBuilderFactory().builder());

		Assertions.assertEquals(Path.of(ApiContract.BASE_V1, ApiContract.DELIVERY_PROVIDERS).toString(), uri.toString());
	}

	@Test
	void testPickup() {
		doReturn(Flux.empty()).when(responseSpec).bodyToFlux(any(ParameterizedTypeReference.class));

		var result = service.pickup(new PickupRequest());
		StepVerifier.create(result).verifyComplete();
		var uri = uriCaptor.getValue().apply(new DefaultUriBuilderFactory().builder());

		Assertions.assertEquals(Path.of(ApiContract.BASE_V1, ApiContract.PICKUP).toString(), uri.toString());
	}
}
