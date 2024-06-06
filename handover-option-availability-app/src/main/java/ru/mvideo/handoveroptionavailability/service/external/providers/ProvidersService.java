package ru.mvideo.handoveroptionavailability.service.external.providers;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;
import ru.mvideo.handoveroptionavailability.exception.HandoverOptionAvailabilityException;
import ru.mvideo.lards.handover.option.api.v1.ProviderZoneHandoverOptionAttributePublicApi;
import ru.mvideo.lards.handover.option.model.FindZonesHandoverOptionsRequest;
import ru.mvideo.lards.handover.option.model.HandoverOptionEnum;
import ru.mvideo.lards.handover.option.model.PickupPointBrand;
import ru.mvideo.lards.handover.option.model.ProviderZoneAttributesHandoverOptions;

@Service
@RequiredArgsConstructor
public class ProvidersService {

	private final ProviderZoneHandoverOptionAttributePublicApi clientV2;

	public Mono<List<ProviderZoneAttributesHandoverOptions>> findProviders(Set<String> zoneIds,
	                                                                       Set<String> handoverOptions,
	                                                                       Set<String> pickupPointBrands) {
		final var request = new FindZonesHandoverOptionsRequest();
		request.setZoneIds(zoneIds);

		final var options = handoverOptions.stream()
				.map(HandoverOptionEnum::fromValue)
				.collect(Collectors.toSet());

		request.setHandoverOptions(options);

		if (pickupPointBrands != null) {
			final var brands = pickupPointBrands.stream()
					.map(PickupPointBrand::fromValue)
					.collect(Collectors.toSet());
			request.setPickupPointBrands(brands);
		}

		return clientV2.findProviders(request)
				.collectList()
				.switchIfEmpty(Mono.defer(() -> {
					throw new HandoverOptionAvailabilityException("Отсутствуют данные в ответе сервиса Handover-option", 1104);
				}));
	}
}
