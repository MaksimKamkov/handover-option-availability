package ru.mvideo.handoveroptionavailability.controller.v2;

import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;
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
import ru.mvideo.handoveroptionavailability.processor.handler.AccessoriesHandler;
import ru.mvideo.handoveroptionavailability.processor.handler.BatchHandler;
import ru.mvideo.handoveroptionavailability.processor.handler.BriefCache;
import ru.mvideo.handoveroptionavailability.processor.handler.DeliveryHandler;
import ru.mvideo.handoveroptionavailability.processor.handler.DeliveryProvidersHandler;
import ru.mvideo.handoveroptionavailability.processor.handler.PickupHandler;
import ru.mvideo.handoveroptionavailability.processor.handler.StockObjectsHandler;

@RestController
@RequiredArgsConstructor
public class HandoverOptionControllerV2 implements HandoverOptionApiV2 {

	private final BriefCache briefCache;
	private final DeliveryHandler deliveryHandler;
	private final DeliveryProvidersHandler deliveryProvidersHandler;
	private final PickupHandler pickupHandler;
	private final StockObjectsHandler stockObjectsHandler;
	private final BatchHandler batchHandler;
	private final AccessoriesHandler accessoriesHandler;

	//@PreAuthorize("hasAuthority('HANDOVER_OPTIONS')")
	@Override
	public Flux<BriefOptions> handoverOptions(BriefRequest request) {
		return briefCache.handoverOptions(request);
	}

	//@PreAuthorize("hasAuthority('HANDOVER_OPTIONS_DELIVERY')")
	@Override
	public Flux<DeliveryResponse> delivery(DeliveryRequestV2 request) {
		return deliveryHandler.handle(request);
	}

	//@PreAuthorize("hasAuthority('HANDOVER_OPTIONS_PROVIDERS')")
	@Override
	public Mono<DeliveryProvidersResponse> deliveryProviders(DeliveryProvidersRequestV2 request) {
		return Mono.defer(() -> deliveryProvidersHandler.handle(request))
				.publishOn(Schedulers.boundedElastic());
	}

	//@PreAuthorize("hasAuthority('HANDOVER_OPTIONS_PICKUP')")
	@Override
	public Flux<PickupResponseItem> pickup(PickupRequest request) {
		return pickupHandler.handle(request);
	}

	//@PreAuthorize("hasAuthority('HANDOVER_OPTIONS_STOCKS')")
	@Override
	public Flux<StockObjectsResponseItem> stockObjects(StockObjectsRequest request) {
		return stockObjectsHandler.handle(request);
	}

	//@PreAuthorize("hasAuthority('HANDOVER_OPTIONS_BATCH')")
	@Override
	public Flux<MaterialHandoverOptions> batch(BatchRequest request) {
		return batchHandler.handle(request);
	}

	//@PreAuthorize("hasAuthority('HANDOVER_OPTIONS_ACCESSORIES')")
	@Override
	public Flux<AccessoriesItem> accessories(AccessoriesRequest request) {
		return accessoriesHandler.handle(request);
	}
}
