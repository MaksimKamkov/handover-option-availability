package ru.mvideo.handoveroptionavailability.api.v1;

import javax.validation.Valid;
import javax.validation.constraints.NotNull;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import ru.mvideo.handoveroptionavailability.api.ApiContract;
import ru.mvideo.handoveroptionavailability.model.AvailableHandoverOptionRequest;
import ru.mvideo.handoveroptionavailability.model.DeliveryProvidersRequest;
import ru.mvideo.handoveroptionavailability.model.DeliveryProvidersResponse;
import ru.mvideo.handoveroptionavailability.model.DeliveryRequest;
import ru.mvideo.handoveroptionavailability.model.DeliveryResponse;
import ru.mvideo.handoveroptionavailability.model.HandoverOptionsBriefResponse;
import ru.mvideo.handoveroptionavailability.model.PickupRequest;
import ru.mvideo.handoveroptionavailability.model.PickupResponseItem;

@RequestMapping(value = ApiContract.BASE_V1,
		produces = {MediaType.APPLICATION_JSON_VALUE},
		consumes = {MediaType.APPLICATION_JSON_VALUE}
)
public interface HandoverOptionApiV1 {

	@PostMapping(value = ApiContract.HANDOVER_OPTIONS)
	@NotNull
	Flux<HandoverOptionsBriefResponse> handoverOptions(@NotNull @Valid @RequestBody AvailableHandoverOptionRequest requestBody);

	@PostMapping(value = ApiContract.DELIVERY)
	@NotNull
	Mono<DeliveryResponse> delivery(@NotNull @Valid @RequestBody DeliveryRequest requestBody);

	@PostMapping(value = ApiContract.DELIVERY_PROVIDERS)
	@NotNull
	Mono<DeliveryProvidersResponse> deliveryProviders(@NotNull @Valid @RequestBody DeliveryProvidersRequest requestBody);

	@PostMapping(value = ApiContract.PICKUP)
	@NotNull
	Flux<PickupResponseItem> pickup(@NotNull @Valid @RequestBody PickupRequest requestBody);
}