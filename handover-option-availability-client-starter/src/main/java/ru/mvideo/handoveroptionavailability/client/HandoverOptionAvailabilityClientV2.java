package ru.mvideo.handoveroptionavailability.client;

import lombok.extern.slf4j.Slf4j;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import ru.mvideo.handoveroptionavailability.api.ApiContract;
import ru.mvideo.handoveroptionavailability.api.v2.HandoverOptionApiV2;
import ru.mvideo.handoveroptionavailability.model.AccessoriesItem;
import ru.mvideo.handoveroptionavailability.model.AccessoriesRequest;
import ru.mvideo.handoveroptionavailability.model.BatchRequest;
import ru.mvideo.handoveroptionavailability.model.BriefOptions;
import ru.mvideo.handoveroptionavailability.model.BriefRequest;
import ru.mvideo.handoveroptionavailability.model.DeliveryProvidersRequestV2;
import ru.mvideo.handoveroptionavailability.model.DeliveryProvidersResponse;
import ru.mvideo.handoveroptionavailability.model.DeliveryRequestV2;
import ru.mvideo.handoveroptionavailability.model.DeliveryResponse;
import ru.mvideo.handoveroptionavailability.model.MaterialHandoverOptions;
import ru.mvideo.handoveroptionavailability.model.PickupRequest;
import ru.mvideo.handoveroptionavailability.model.PickupResponseItem;
import ru.mvideo.handoveroptionavailability.model.StockObjectsRequest;
import ru.mvideo.handoveroptionavailability.model.StockObjectsResponseItem;
import ru.mvideo.lib.client.BaseClient;
import ru.mvideo.lib.client.LogHelper;

@Slf4j
public class HandoverOptionAvailabilityClientV2 extends BaseClient implements HandoverOptionApiV2 {

	public HandoverOptionAvailabilityClientV2(WebClient client, LogHelper logHelper) {
		super(client, logHelper, log);
	}

	@Override
	public Flux<BriefOptions> handoverOptions(BriefRequest requestBody) {
		return postFlux(ApiContract.BASE_V2 + ApiContract.HANDOVER_OPTIONS, requestBody, new ParameterizedTypeReference<>() {});
	}

	@Override
	public Flux<DeliveryResponse> delivery(DeliveryRequestV2 requestBody) {
		return postFlux(ApiContract.BASE_V2 + ApiContract.DELIVERY, requestBody, new ParameterizedTypeReference<>() {});
	}

	@Override
	public Mono<DeliveryProvidersResponse> deliveryProviders(DeliveryProvidersRequestV2 requestBody) {
		return postMono(ApiContract.BASE_V2 + ApiContract.DELIVERY_PROVIDERS, requestBody, new ParameterizedTypeReference<>() {});
	}

	@Override
	public Flux<PickupResponseItem> pickup(PickupRequest requestBody) {
		return postFlux(ApiContract.BASE_V2 + ApiContract.PICKUP, requestBody, new ParameterizedTypeReference<>() {});
	}

	@Override
	public Flux<StockObjectsResponseItem> stockObjects(StockObjectsRequest request) {
		return postFlux(ApiContract.BASE_V2 + ApiContract.STOCK_OBJECTS, request, new ParameterizedTypeReference<>() {});
	}

	@Override
	public Flux<MaterialHandoverOptions> batch(BatchRequest request) {
		return postFlux(ApiContract.BASE_V2 + ApiContract.BATCH, request, new ParameterizedTypeReference<>() {});
	}

	@Override
	public Flux<AccessoriesItem> accessories(AccessoriesRequest request) {
		return postFlux(ApiContract.BASE_V2 + ApiContract.ACCESSORIES, request, new ParameterizedTypeReference<>() {});
	}
}
