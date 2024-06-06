package ru.mvideo.handoveroptionavailability.client;

import lombok.extern.slf4j.Slf4j;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import ru.mvideo.handoveroptionavailability.api.ApiContract;
import ru.mvideo.handoveroptionavailability.api.v1.HandoverOptionApiV1;
import ru.mvideo.handoveroptionavailability.model.AvailableHandoverOptionRequest;
import ru.mvideo.handoveroptionavailability.model.DeliveryProvidersRequest;
import ru.mvideo.handoveroptionavailability.model.DeliveryProvidersResponse;
import ru.mvideo.handoveroptionavailability.model.DeliveryRequest;
import ru.mvideo.handoveroptionavailability.model.DeliveryResponse;
import ru.mvideo.handoveroptionavailability.model.HandoverOptionsBriefResponse;
import ru.mvideo.handoveroptionavailability.model.PickupRequest;
import ru.mvideo.handoveroptionavailability.model.PickupResponseItem;
import ru.mvideo.lib.client.BaseClient;
import ru.mvideo.lib.client.LogHelper;

@Slf4j
public class HandoverOptionAvailabilityClientV1 extends BaseClient implements HandoverOptionApiV1 {

	public HandoverOptionAvailabilityClientV1(WebClient client, LogHelper logHelper) {
		super(client, logHelper, log);
	}

	@Override
	public Flux<HandoverOptionsBriefResponse> handoverOptions(AvailableHandoverOptionRequest requestBody) {
		return postFlux(ApiContract.BASE_V1 + ApiContract.HANDOVER_OPTIONS, requestBody, new ParameterizedTypeReference<>() {});
	}

	@Override
	public Mono<DeliveryResponse> delivery(DeliveryRequest requestBody) {
		return postMono(ApiContract.BASE_V1 + ApiContract.DELIVERY, requestBody, new ParameterizedTypeReference<>() {});
	}

	@Override
	public Mono<DeliveryProvidersResponse> deliveryProviders(DeliveryProvidersRequest requestBody) {
		return postMono(ApiContract.BASE_V1 + ApiContract.DELIVERY_PROVIDERS, requestBody, new ParameterizedTypeReference<>() {});
	}

	@Override
	public Flux<PickupResponseItem> pickup(PickupRequest requestBody) {
		return postFlux(ApiContract.BASE_V1 + ApiContract.PICKUP, requestBody, new ParameterizedTypeReference<>() {});
	}
}
