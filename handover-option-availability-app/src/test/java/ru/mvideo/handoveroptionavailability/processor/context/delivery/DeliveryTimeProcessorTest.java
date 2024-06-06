package ru.mvideo.handoveroptionavailability.processor.context.delivery;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;
import ru.mvideo.deliverytimecalculation.model.DeliveryTimeResponse;
import ru.mvideo.handoveroptionavailability.exception.FailedToGetDeliveryCoordinatesException;
import ru.mvideo.handoveroptionavailability.model.CoordinatePoint;
import ru.mvideo.handoveroptionavailability.model.HandoverOption;
import ru.mvideo.handoveroptionavailability.model.external.msp.logistic.response.AvailabilityOption;
import ru.mvideo.handoveroptionavailability.processor.model.DeliveryContext;
import ru.mvideo.handoveroptionavailability.processor.model.HandoverObject;
import ru.mvideo.handoveroptionavailability.processor.model.OptionContext;
import ru.mvideo.handoveroptionavailability.service.external.deliverytimecalculation.DeliveryTimeCalculationService;
import ru.mvideo.lards.geospatial.model.GeoPoint;

@ExtendWith(MockitoExtension.class)
public class DeliveryTimeProcessorTest {

	@Mock
	private DeliveryTimeCalculationService deliveryTimeCalculationService;

	@InjectMocks
	private DeliveryTimeProcessor processor;

	@DisplayName("Время доставки не определено, так как в контексте нет требуемых опций")
	@Test
	void testWithoutRequiredOptions() {
		final var context = DeliveryContext.builder()
				.requestHandoverOptions(Collections.emptySet())
				.build();

		StepVerifier.create(processor.executeProcessor(context))
				.consumeNextWith(ctx -> assertNull(context.getDeliveryDuration()))
				.verifyComplete();

		verify(deliveryTimeCalculationService, times(0))
				.getDeliveryTime(any(GeoPoint.class), any(GeoPoint.class));
	}

	@DisplayName("Время доставки определено")
	@Test
	void testWithRequiredOptions() {
		Set<String> options = Set.of(HandoverOption.ETA_DELIVERY.getValue(), HandoverOption.ELECTRONIC_DELIVERY.getValue());
		HandoverObject nearestHandoverObject = HandoverObject.builder()
				.objectId("S2")
				.distance(10.5)
				.coordinate(new GeoPoint(2, 2))
				.build();
		Map<String, OptionContext> handoverOptionContext = options.stream()
				.map(opt -> {
					OptionContext optionContext = new OptionContext();
					optionContext.setHandoverOption(opt);
					optionContext.setHandoverObjects(List.of(
							HandoverObject.builder()
									.objectId("S1")
									.distance(30d)
									.coordinate(new GeoPoint(1, 1))
									.build(),
							nearestHandoverObject,
							HandoverObject.builder()
									.objectId("S3")
									.distance(11d)
									.coordinate(new GeoPoint(3, 3))
									.build(),
							HandoverObject.builder()
									.objectId("S4")
									.distance(1d)
									.coordinate(new GeoPoint(4, 4))
									.build()
					));
					optionContext.setAvailabilityOptions(List.of(
							AvailabilityOption.builder()
									.handoverObject("S1")
									.build(),
							AvailabilityOption.builder()
									.handoverObject("S2")
									.build(),
							AvailabilityOption.builder()
									.handoverObject("S3")
									.build()
					));
					return optionContext;
				}).collect(Collectors.toMap(OptionContext::getHandoverOption, Function.identity()));
		final var context = DeliveryContext.builder()
				.requestHandoverOptions(options)
				.coordinatePoint(CoordinatePoint.builder()
						.latitude(5d)
						.longitude(5d)
						.build())
				.handoverOptionContext(handoverOptionContext)
				.build();
		GeoPoint geoPointFromRequest = new GeoPoint(context.coordinatePoint().getLatitude(), context.coordinatePoint().getLongitude());
		when(deliveryTimeCalculationService.getDeliveryTime(
						nearestHandoverObject.getCoordinate(),
						geoPointFromRequest
				)
		).thenReturn(Mono.just(DeliveryTimeResponse.builder().duration(57.2).build()));

		StepVerifier.create(processor.executeProcessor(context))
				.consumeNextWith(ctx -> {
					assertNotNull(context.getDeliveryDuration());
					assertEquals(60, context.getDeliveryDuration());
				})
				.verifyComplete();

		verify(deliveryTimeCalculationService, times(1))
				.getDeliveryTime(nearestHandoverObject.getCoordinate(), geoPointFromRequest);
	}

	@DisplayName("Невозможно определить координаты получателя")
	@Test
	void testWithMissedRecipientCoordinates() {
		Set<String> options = Set.of(HandoverOption.ETA_DELIVERY.getValue(), HandoverOption.ELECTRONIC_DELIVERY.getValue());
		Map<String, OptionContext> handoverOptionContext = options.stream()
				.map(opt -> {
					OptionContext optionContext = new OptionContext();
					optionContext.setHandoverOption(opt);
					optionContext.setHandoverObjects(List.of(
							HandoverObject.builder()
									.objectId("S1")
									.distance(30d)
									.coordinate(new GeoPoint(1, 1))
									.build(),
							HandoverObject.builder()
									.objectId("S2")
									.distance(10.5)
									.coordinate(new GeoPoint(2, 2))
									.build(),
							HandoverObject.builder()
									.objectId("S3")
									.distance(11d)
									.coordinate(new GeoPoint(3, 3))
									.build(),
							HandoverObject.builder()
									.objectId("S4")
									.distance(1d)
									.coordinate(new GeoPoint(4, 4))
									.build()
					));
					optionContext.setAvailabilityOptions(List.of(
							AvailabilityOption.builder()
									.handoverObject("S1")
									.build(),
							AvailabilityOption.builder()
									.handoverObject("S2")
									.build(),
							AvailabilityOption.builder()
									.handoverObject("S3")
									.build()
					));
					return optionContext;
				}).collect(Collectors.toMap(OptionContext::getHandoverOption, Function.identity()));
		final var context = DeliveryContext.builder()
				.requestHandoverOptions(options)
				.handoverOptionContext(handoverOptionContext)
				.build();

		StepVerifier.create(processor.executeProcessor(context))
				.verifyError(FailedToGetDeliveryCoordinatesException.class);

		verify(deliveryTimeCalculationService, times(0))
				.getDeliveryTime(any(GeoPoint.class), any(GeoPoint.class));
	}
}
