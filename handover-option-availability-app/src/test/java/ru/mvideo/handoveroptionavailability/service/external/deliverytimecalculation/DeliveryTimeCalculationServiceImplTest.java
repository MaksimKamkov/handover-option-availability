package ru.mvideo.handoveroptionavailability.service.external.deliverytimecalculation;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.when;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;
import ru.mvideo.deliverytimecalculation.client.DeliveryTimeCalculationV1Client;
import ru.mvideo.deliverytimecalculation.model.Coordinate;
import ru.mvideo.deliverytimecalculation.model.DeliveryTimeRequest;
import ru.mvideo.deliverytimecalculation.model.DeliveryTimeResponse;
import ru.mvideo.deliverytimecalculation.model.LocationDescription;
import ru.mvideo.lards.geospatial.model.GeoPoint;

@ExtendWith(MockitoExtension.class)
class DeliveryTimeCalculationServiceImplTest {

	@Mock
	private DeliveryTimeCalculationV1Client client;

	@InjectMocks
	private DeliveryTimeCalculationServiceImpl service;

	@Test
	void testGetDeliveryTime() {
		double expectedDuration = 10.1;
		GeoPoint sourceCoordinates = new GeoPoint(1, 1);
		GeoPoint recipientCoordinates = new GeoPoint(2, 2);
		DeliveryTimeRequest request = DeliveryTimeRequest.builder()
				.mode(DeliveryTimeRequest.ModeEnum.DRIVING)
				.source(createLocationDescription(sourceCoordinates))
				.recipient(createLocationDescription(recipientCoordinates))
				.build();

		when(client.getDeliveryTime(request))
				.thenReturn(Mono.just(DeliveryTimeResponse.builder().duration(expectedDuration).build()));

		StepVerifier.create(service.getDeliveryTime(sourceCoordinates, recipientCoordinates))
				.consumeNextWith(response -> assertEquals(expectedDuration, response.getDuration()))
				.verifyComplete();
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