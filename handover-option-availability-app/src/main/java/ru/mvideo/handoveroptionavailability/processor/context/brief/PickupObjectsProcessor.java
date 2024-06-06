package ru.mvideo.handoveroptionavailability.processor.context.brief;

import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;
import ru.mvideo.handoveroptionavailability.model.HandoverOption;
import ru.mvideo.handoveroptionavailability.processor.BaseProcessor;
import ru.mvideo.handoveroptionavailability.processor.model.Context;
import ru.mvideo.handoveroptionavailability.processor.model.Flags;
import ru.mvideo.handoveroptionavailability.processor.model.HandoverObject;
import ru.mvideo.handoveroptionavailability.service.external.zonepickupitem.ZonePickupItemService;

@RequiredArgsConstructor
@Component
public class PickupObjectsProcessor<T extends Context> extends BaseProcessor<T> {

	private final ZonePickupItemService zonePickupItemService;

	@Override
	protected Mono<T> executeProcessor(T context) {
		return zonePickupItemService.getObjectsByZones(context.regionId(), context.retailBrand().getValue())
				.map(handoverObjects -> {
					context.handoverObjects(handoverObjects);
					final var objectIds = handoverObjects.stream()
							.map(HandoverObject::getObjectId)
							.collect(Collectors.toSet());
					context.pickupObjectIds().addAll(objectIds);
					return context;
				});
	}

	@Override
	public boolean shouldRun(T context) {
		return context.hasOption(HandoverOption.ETA_DELIVERY.getValue())
				|| context.hasOption(HandoverOption.EXACTLY_TIME_DELIVERY.getValue())
				|| context.hasOption(HandoverOption.PICKUP_SEAMLESS.getValue())
				|| (context.hasOption(HandoverOption.PICKUP.getValue()) && !context.flags().contains(Flags.PICKUP_HANDOVER_OBJECTS));
	}
}
