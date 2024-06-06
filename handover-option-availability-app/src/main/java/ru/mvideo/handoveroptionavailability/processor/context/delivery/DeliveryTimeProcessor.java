package ru.mvideo.handoveroptionavailability.processor.context.delivery;

import java.util.Comparator;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;
import ru.mvideo.handoveroptionavailability.exception.FailedToGetDeliveryCoordinatesException;
import ru.mvideo.handoveroptionavailability.model.CoordinatePoint;
import ru.mvideo.handoveroptionavailability.model.HandoverOption;
import ru.mvideo.handoveroptionavailability.model.external.msp.logistic.response.AvailabilityOption;
import ru.mvideo.handoveroptionavailability.processor.BaseProcessor;
import ru.mvideo.handoveroptionavailability.processor.model.DeliveryContext;
import ru.mvideo.handoveroptionavailability.processor.model.HandoverObject;
import ru.mvideo.handoveroptionavailability.processor.model.OptionContext;
import ru.mvideo.handoveroptionavailability.service.external.deliverytimecalculation.DeliveryTimeCalculationService;
import ru.mvideo.lards.geospatial.model.GeoPoint;

@Component
@RequiredArgsConstructor
public class DeliveryTimeProcessor extends BaseProcessor<DeliveryContext> {

	private static final Set<String> ETA_AND_EXACTLY = Set.of(
			HandoverOption.ETA_DELIVERY.getValue(),
			HandoverOption.EXACTLY_TIME_DELIVERY.getValue()
	);

	private final DeliveryTimeCalculationService deliveryTimeCalculationService;

	@Override
	protected Mono<DeliveryContext> executeProcessor(DeliveryContext context) {
		return Flux.fromIterable(context.options())
				.filter(ETA_AND_EXACTLY::contains)
				.map(opt -> context.handoverOptionContext().get(opt))
				.map(this::calculateNearestPickupObjectCoordinates)
				.flatMap(Mono::justOrEmpty)
				.next()
				.flatMap(pickupCoordinates -> {
					GeoPoint  recipientCoordinates = getRecipientCoordinate(context);
					return deliveryTimeCalculationService.getDeliveryTime(pickupCoordinates, recipientCoordinates)
							.doOnSuccess(deliveryTimeResponse -> {
								Integer deliveryDuration = roundDeliveryDuration(deliveryTimeResponse.getDuration());
								context.deliveryDuration(deliveryDuration);
							}).thenReturn(context);
				})
				.switchIfEmpty(Mono.just(context))
				.subscribeOn(Schedulers.boundedElastic());
	}

	@Override
	public boolean shouldRun(DeliveryContext context) {
		return !context.stockHandoverObjects().isEmpty()
				&& (context.hasOption(HandoverOption.ETA_DELIVERY.getValue())
				|| context.hasOption(HandoverOption.EXACTLY_TIME_DELIVERY.getValue()));
	}


	private Optional<GeoPoint> calculateNearestPickupObjectCoordinates(OptionContext context) {
		final var handoverObjectsWithStocks = context.getAvailabilityOptions().stream()
				.map(AvailabilityOption::getHandoverObject)
				.collect(Collectors.toSet());
		return context.getHandoverObjects().stream()
				.filter(handoverObject -> handoverObjectsWithStocks.contains(handoverObject.getObjectId()))
				.filter(handoverObject -> handoverObject.getDistance() != null)
				.min(Comparator.comparingDouble(HandoverObject::getDistance))
				.map(HandoverObject::getCoordinate);
	}

	private GeoPoint getRecipientCoordinate(DeliveryContext context) {
		CoordinatePoint coordinatePointFromRequest = context.coordinatePoint();
		GeoPoint calculatedCoordinates = context.geoPoint();
		if (coordinatePointFromRequest == null && calculatedCoordinates == null) {
			throw new FailedToGetDeliveryCoordinatesException(context.destination());
		}
		return coordinatePointFromRequest == null
				? calculatedCoordinates
				: new GeoPoint(coordinatePointFromRequest.getLatitude(), coordinatePointFromRequest.getLongitude());
	}

	private Integer roundDeliveryDuration(Double duration) {
		return duration == null ? null : (int) (Math.ceil(duration / 5d) * 5);
	}
}
