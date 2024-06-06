package ru.mvideo.handoveroptionavailability.processor.context.deliveryproviders;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;
import ru.mvideo.handoveroptionavailability.model.LocationDescriptionV2;
import ru.mvideo.handoveroptionavailability.processor.BaseProcessor;
import ru.mvideo.handoveroptionavailability.processor.model.DeliveryProvidersContext;
import ru.mvideo.handoveroptionavailability.service.external.geocoder.GeoCoderService;
import ru.mvideo.handoveroptionavailability.service.external.objectspublic.ObjectsPublicClientService;
import ru.mvideo.lards.geospatial.model.GeoPoint;

@Slf4j
@Component
@RequiredArgsConstructor
public class FindCoordinateProcessor extends BaseProcessor<DeliveryProvidersContext> {

	private final GeoCoderService geoCoderService;
	private final ObjectsPublicClientService objectsPublicClientService;

	@Override
	protected Mono<DeliveryProvidersContext> executeProcessor(DeliveryProvidersContext context) {
		return Mono.defer(() -> Mono.zip(setCoordinateSource(context), setCoordinatesRecipient(context)).map(tuple -> context));
	}

	private Mono<DeliveryProvidersContext> setCoordinateSource(DeliveryProvidersContext context) {
		return getCoordinates(context.getSource())
				.map(geoPoint -> {
					context.setCoordinatesSource(geoPoint);
					return context;
				});
	}

	private Mono<DeliveryProvidersContext> setCoordinatesRecipient(DeliveryProvidersContext context) {
		return getCoordinates(context.getRecipient())
				.map(geoPoint -> {
					context.setCoordinatesRecipient(geoPoint);
					return context;
				});
	}

	private Mono<GeoPoint> getCoordinates(LocationDescriptionV2 locationDescription) {
		if (locationDescription.getAddress() != null && locationDescription.getAddress().getCoordinate() != null) {
			return Mono.just(
					new GeoPoint(
							locationDescription.getAddress().getCoordinate().getLatitude(),
							locationDescription.getAddress().getCoordinate().getLongitude()
					));
		} else if (locationDescription.getObjectId() != null) {
			return objectsPublicClientService.getCoordinateHandoverObject(locationDescription.getObjectId());
		} else {
			return geoCoderService.convertAddressToCoordinates(locationDescription.getAddress().getRepresentation());
		}
	}

	@Override
	public boolean shouldRun(DeliveryProvidersContext context) {
		return true;
	}
}
