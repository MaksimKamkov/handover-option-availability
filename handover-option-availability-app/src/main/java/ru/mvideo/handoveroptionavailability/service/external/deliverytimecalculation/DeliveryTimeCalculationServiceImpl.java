package ru.mvideo.handoveroptionavailability.service.external.deliverytimecalculation;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;
import ru.mvideo.deliverytimecalculation.client.DeliveryTimeCalculationV1Client;
import ru.mvideo.deliverytimecalculation.model.Coordinate;
import ru.mvideo.deliverytimecalculation.model.DeliveryTimeRequest;
import ru.mvideo.deliverytimecalculation.model.DeliveryTimeResponse;
import ru.mvideo.deliverytimecalculation.model.LocationDescription;
import ru.mvideo.lards.geospatial.model.GeoPoint;

@Service
@RequiredArgsConstructor
public class DeliveryTimeCalculationServiceImpl implements DeliveryTimeCalculationService {

	private static final DeliveryTimeRequest.ModeEnum REQUEST_MODE = DeliveryTimeRequest.ModeEnum.DRIVING;

	private final DeliveryTimeCalculationV1Client client;

	@Override
	public Mono<DeliveryTimeResponse> getDeliveryTime(GeoPoint sourceCoordinates, GeoPoint recipientCoordinates) {
		DeliveryTimeRequest request = DeliveryTimeRequest.builder()
				.mode(REQUEST_MODE)
				.source(createLocationDescription(sourceCoordinates))
				.recipient(createLocationDescription(recipientCoordinates))
				.build();

		return client.getDeliveryTime(request)
				.publishOn(Schedulers.parallel());
	}


	private LocationDescription createLocationDescription(GeoPoint coordinates) {
		return LocationDescription.builder()
				.coordinate(Coordinate.builder()
						.latitude(coordinates.getLatitude())
						.longitude(coordinates.getLatitude())
						.build())
				.build();
	}
}
