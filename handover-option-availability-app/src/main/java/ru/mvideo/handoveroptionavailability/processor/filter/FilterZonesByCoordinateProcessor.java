package ru.mvideo.handoveroptionavailability.processor.filter;

import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;
import ru.mvideo.handoveroptionavailability.model.HandoverOption;
import ru.mvideo.handoveroptionavailability.processor.BaseProcessor;
import ru.mvideo.handoveroptionavailability.processor.model.DeliveryContext;
import ru.mvideo.lards.geospatial.utils.GeospatialExtensionKt;
import ru.mvideo.lards.handover.option.model.ProviderZoneAttributesHandoverOptions;
import ru.mvideo.lards.zone.model.ZoneResponse;

@Component
@RequiredArgsConstructor
public class FilterZonesByCoordinateProcessor extends BaseProcessor<DeliveryContext> {

	private static final Set<String> ETA_AND_EXACTLY = Set.of(
			HandoverOption.ETA_DELIVERY.getValue(),
			HandoverOption.EXACTLY_TIME_DELIVERY.getValue()
	);

	@Override
	protected Mono<DeliveryContext> executeProcessor(DeliveryContext context) {
		return Mono.fromCallable(() -> {
			final var deliveryCoordinate = context.geoPoint();
			final var regionalZone = context.regionDetails();
			final var zones = context.zones();

			final var zonesContainCoordinate = zones.stream()
					.filter(zone -> GeospatialExtensionKt.containsPoint(zone.getArea(), deliveryCoordinate))
					.map(ZoneResponse::getId)
					.collect(Collectors.toSet());
			if (GeospatialExtensionKt.containsPoint(regionalZone.getArea(), deliveryCoordinate)) {
				zonesContainCoordinate.add(regionalZone.getId());
			}

			for (ProviderZoneAttributesHandoverOptions provider : context.providers()) {
				final var intersection = new HashSet<>(provider.getZoneIds());
				intersection.retainAll(zonesContainCoordinate);
				if (intersection.isEmpty()) {
					final var options = provider.getHandoverOptions().stream()
							.filter(handoverOption -> !ETA_AND_EXACTLY.contains(handoverOption.getName()))
							.collect(Collectors.toList());
					provider.setHandoverOptions(options);
				}
			}

			final var providers = context.providers().stream()
					.filter(provider -> !provider.getHandoverOptions().isEmpty())
					.collect(Collectors.toList());
			context.providers(providers);

			return context;
		});
	}

	@Override
	public boolean shouldRun(DeliveryContext context) {
		return context.hasOption(HandoverOption.ETA_DELIVERY.getValue()) || context.hasOption(HandoverOption.EXACTLY_TIME_DELIVERY.getValue());
	}
}
