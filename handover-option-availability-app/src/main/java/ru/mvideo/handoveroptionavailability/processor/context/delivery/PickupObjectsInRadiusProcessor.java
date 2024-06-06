package ru.mvideo.handoveroptionavailability.processor.context.delivery;

import java.util.Comparator;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import ru.mvideo.handoveroptionavailability.model.HandoverOption;
import ru.mvideo.handoveroptionavailability.processor.BaseProcessor;
import ru.mvideo.handoveroptionavailability.processor.model.DeliveryContext;
import ru.mvideo.handoveroptionavailability.processor.model.HandoverObject;
import ru.mvideo.handoveroptionavailability.processor.utils.ProviderUtil;
import ru.mvideo.handoveroptionavailability.service.external.zonepickupitem.ZonePickupItemService;
import ru.mvideo.lards.zone.pickup.item.model.ZoneHandoverPickupObjectDistanceResponse;

@Component
@RequiredArgsConstructor
public class PickupObjectsInRadiusProcessor extends BaseProcessor<DeliveryContext> {

	private final ZonePickupItemService zonePickupItemService;

	@Override
	protected Mono<DeliveryContext> executeProcessor(DeliveryContext context) {
		return Mono.justOrEmpty(ProviderUtil.findMaxRadius(context.providers()))
				.flatMap(radius -> zonePickupItemService.getObjectsByRadius(context.regionId(), context.retailBrand().getValue(),
								context.geoPoint(), radius)
						.flatMap(this::toHandoverObjects)
						.distinct()
						.filter(handoverObjectPredicate(context))
						.sort(Comparator.comparing(HandoverObject::getDistance))
						.collectList()
						.map(handoverObjects -> {
							if (handoverObjects.isEmpty()) {
								var reason = String.format("Pickup objects not found in radius [%f]", radius);
								context.disableOption(HandoverOption.EXACTLY_TIME_DELIVERY.getValue(), reason);
								context.disableOption(HandoverOption.ETA_DELIVERY.getValue(), reason);
							} else {
								context.handoverObjects(handoverObjects);
								var objectIds = handoverObjects.stream()
										.map(HandoverObject::getObjectId)
										.collect(Collectors.toSet());
								context.pickupObjectIds().addAll(objectIds);
							}
							return context;
						}))
				.switchIfEmpty(Mono.just(context)
						.doOnNext(ctx -> ctx.disableOption(HandoverOption.EXACTLY_TIME_DELIVERY.getValue(), "Pickup objects not found"))
						.doOnNext(ctx -> ctx.disableOption(HandoverOption.ETA_DELIVERY.getValue(), "Pickup objects not found")));
	}

	@Override
	public boolean shouldRun(DeliveryContext context) {
		return context.hasOption(HandoverOption.ETA_DELIVERY.getValue()) || context.hasOption(HandoverOption.EXACTLY_TIME_DELIVERY.getValue());
	}

	private Flux<HandoverObject> toHandoverObjects(ZoneHandoverPickupObjectDistanceResponse zoneHandoverPickupObject) {
		return Flux.fromIterable(zoneHandoverPickupObject.getHandoverPickupObjects())
				.map(pickupObject -> HandoverObject.builder()
						.objectId(pickupObject.getObjectId())
						.distance(pickupObject.getDistance())
						.coordinate(pickupObject.getCoordinate())
						.build()
				);
	}

	private Predicate<HandoverObject> handoverObjectPredicate(DeliveryContext context) {
		return context.stockHandoverObjects().isEmpty() ? test -> true : test ->
				context.stockHandoverObjects().contains(test.getObjectId()) && context.stockObjects().containsKey(test.getObjectId());
	}
}
