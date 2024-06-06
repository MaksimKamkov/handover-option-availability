package ru.mvideo.handoveroptionavailability.api.v2;

import javax.validation.Valid;
import javax.validation.constraints.NotNull;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import ru.mvideo.handoveroptionavailability.api.ApiContract;
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

@RequestMapping(value = ApiContract.BASE_V2,
		produces = {MediaType.APPLICATION_JSON_VALUE},
		consumes = {MediaType.APPLICATION_JSON_VALUE}
)
public interface HandoverOptionApiV2 {

	@PostMapping(value = ApiContract.HANDOVER_OPTIONS)
	@NotNull
	Flux<BriefOptions> handoverOptions(@NotNull @Valid @RequestBody BriefRequest requestBody);

	@PostMapping(value = ApiContract.DELIVERY)
	@NotNull
	Flux<DeliveryResponse> delivery(@NotNull @Valid @RequestBody DeliveryRequestV2 requestBody);

	@PostMapping(value = ApiContract.DELIVERY_PROVIDERS)
	@NotNull
	Mono<DeliveryProvidersResponse> deliveryProviders(@NotNull @Valid @RequestBody DeliveryProvidersRequestV2 requestBody);

	@PostMapping(value = ApiContract.PICKUP)
	@NotNull
	Flux<PickupResponseItem> pickup(@NotNull @Valid @RequestBody PickupRequest requestBody);

	@PostMapping(value = ApiContract.STOCK_OBJECTS)
	@NotNull
	Flux<StockObjectsResponseItem> stockObjects(@NotNull @Valid @RequestBody StockObjectsRequest request);

	@PostMapping(value = ApiContract.BATCH)
	@NotNull
	Flux<MaterialHandoverOptions> batch(@NotNull @Valid @RequestBody BatchRequest request);

	@PostMapping(value = ApiContract.ACCESSORIES)
	@NotNull
	Flux<AccessoriesItem> accessories(@NotNull @Valid @RequestBody AccessoriesRequest request);
}