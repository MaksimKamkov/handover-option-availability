package ru.mvideo.handoveroptionavailability.processor.context.deliveryproviders;

import java.util.List;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;
import ru.mvideo.handoveroptionavailability.model.HandoverOption;
import ru.mvideo.handoveroptionavailability.processor.BaseProcessor;
import ru.mvideo.handoveroptionavailability.processor.model.DeliveryProvidersContext;
import ru.mvideo.handoveroptionavailability.service.external.zone.ZoneInfoService;
import ru.mvideo.lards.geospatial.utils.GeospatialExtensionKt;

@Slf4j
@Component
@RequiredArgsConstructor
public class IntersectionZoneProcessor extends BaseProcessor<DeliveryProvidersContext> {

	private final ZoneInfoService zoneInfo;

	private static final List<String> OPTIONS = List.of(
			HandoverOption.ETA_DELIVERY.getValue(),
			HandoverOption.EXACTLY_TIME_DELIVERY.getValue(),
			HandoverOption.PICKUP_SEAMLESS.getValue()
	);

	@Override
	protected Mono<DeliveryProvidersContext> executeProcessor(DeliveryProvidersContext context) {
		return Mono.defer(() -> zoneInfo.getIncludedZonesByCoordinates(context.getCoordinatesSource(), context.retailBrand().getValue())
				.publishOn(Schedulers.parallel())
				.flatMap(response -> {
					final var intersection = response.stream()
							.filter(zoneResponse -> GeospatialExtensionKt.containsPoint(zoneResponse.getArea(),
									context.getCoordinatesRecipient()))
							.collect(Collectors.toList());
					if (intersection.isEmpty()) {
						context.disableOptions(OPTIONS, "Source and recipient are in different zones");
					}
					context.zones(intersection);
					return Mono.just(context);
				})
		);
	}

	@Override
	public boolean shouldRun(DeliveryProvidersContext context) {
		return true;
	}
}
