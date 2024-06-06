package ru.mvideo.handoveroptionavailability.controller.v1;

import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;
import ru.mvideo.handoveroptionavailability.api.v1.HandoverOptionApiV1;
import ru.mvideo.handoveroptionavailability.mapper.HandoverOptionRqRsMapper;
import ru.mvideo.handoveroptionavailability.model.AvailableHandoverOptionRequest;
import ru.mvideo.handoveroptionavailability.model.DeliveryProvidersRequest;
import ru.mvideo.handoveroptionavailability.model.DeliveryProvidersResponse;
import ru.mvideo.handoveroptionavailability.model.DeliveryRequest;
import ru.mvideo.handoveroptionavailability.model.DeliveryResponse;
import ru.mvideo.handoveroptionavailability.model.HandoverOptionsBriefResponse;
import ru.mvideo.handoveroptionavailability.model.PickupRequest;
import ru.mvideo.handoveroptionavailability.model.PickupResponseItem;
import ru.mvideo.handoveroptionavailability.processor.handler.BriefCache;
import ru.mvideo.handoveroptionavailability.processor.handler.DeliveryHandler;
import ru.mvideo.handoveroptionavailability.processor.handler.DeliveryProvidersHandler;
import ru.mvideo.handoveroptionavailability.processor.handler.PickupHandler;

@RestController
@RequiredArgsConstructor
public class HandoverOptionControllerV1 implements HandoverOptionApiV1 {

	private final BriefCache briefCache;
	private final DeliveryHandler deliveryHandler;
	private final PickupHandler pickupHandler;
	private final DeliveryProvidersHandler deliveryProvidersHandler;
	private final HandoverOptionRqRsMapper rqRsMapper;

	@Override
	public Flux<HandoverOptionsBriefResponse> handoverOptions(AvailableHandoverOptionRequest requestBody) {
		return Mono.just(requestBody)
				.map(rqRsMapper::availableHandoverOptionRequest2BriefRequest)
				.flatMapMany(briefCache::handoverOptions)
				.map(rqRsMapper::briefOptions2HandoverOptionsBriefResponse)
				.subscribeOn(Schedulers.boundedElastic());
	}

	@Override
	public Mono<DeliveryResponse> delivery(DeliveryRequest requestBody) {
		return Mono.just(requestBody)
				.map(rqRsMapper::deliveryRequest2DeliveryRequestV2)
				.flatMapMany(deliveryHandler::handle)
				.next()
				.subscribeOn(Schedulers.boundedElastic());
	}

	@Override
	public Mono<DeliveryProvidersResponse> deliveryProviders(DeliveryProvidersRequest requestBody) {
		return Mono.just(requestBody)
				.map(rqRsMapper::deliveryProvidersRequest2DeliveryProvidersRequestV2)
				.flatMap(deliveryProvidersHandler::handle)
				.subscribeOn(Schedulers.boundedElastic());
	}

	@Override
	public Flux<PickupResponseItem> pickup(PickupRequest requestBody) {
		return Mono.just(requestBody)
				.flatMapMany(pickupHandler::handle)
				.subscribeOn(Schedulers.boundedElastic());
	}
}
