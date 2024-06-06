package ru.mvideo.handoveroptionavailability.processor.context;

import java.util.Set;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;
import ru.mvideo.handoveroptionavailability.exception.FailedToGetDeliveryCoordinatesException;
import ru.mvideo.handoveroptionavailability.model.HandoverOption;
import ru.mvideo.handoveroptionavailability.processor.BaseProcessor;
import ru.mvideo.handoveroptionavailability.processor.model.DeliveryContext;
import ru.mvideo.handoveroptionavailability.service.external.geocoder.GeoCoderService;
import ru.mvideo.lards.geospatial.model.GeoPoint;

@Component
@RequiredArgsConstructor
public class GeoPointProcessor extends BaseProcessor<DeliveryContext> {

	private static final Set<String> ETA_AND_EXACTLY = Set.of(
			HandoverOption.ETA_DELIVERY.getValue(),
			HandoverOption.EXACTLY_TIME_DELIVERY.getValue()
	);

	private final GeoCoderService geoCoderService;

	@Override
	protected Mono<DeliveryContext> executeProcessor(DeliveryContext context) {
		return getCoordinate(context)
				.doOnSuccess(geoPoint -> {
					if (geoPoint != null) {
						context.geoPoint(geoPoint);
					}
				}).then(Mono.just(context));
	}

	private Mono<GeoPoint> getCoordinate(DeliveryContext context) {
		if (context.coordinatePoint() == null) {
			return geoCoderService.convertAddressToCoordinates(context.destination())
					.onErrorResume(FailedToGetDeliveryCoordinatesException.class, fallback -> {
						for (String opt : context.options()) {
							if (ETA_AND_EXACTLY.contains(opt)) {
								context.disableOption(opt, "Не удалось получить географические координаты адреса доставки");
							}
						}
						if (context.options().isEmpty()) {
							throw new FailedToGetDeliveryCoordinatesException(context.destination());
						}
						return Mono.empty();
					});
		} else {
			return Mono.fromCallable(() -> {
				final var latitude = context.coordinatePoint().getLatitude();
				final var longitude = context.coordinatePoint().getLongitude();
				return new GeoPoint(latitude, longitude);
			});
		}
	}

	@Override
	public boolean shouldRun(DeliveryContext context) {
		return context.hasOption(HandoverOption.ETA_DELIVERY.getValue()) || context.hasOption(HandoverOption.EXACTLY_TIME_DELIVERY.getValue());
	}
}
