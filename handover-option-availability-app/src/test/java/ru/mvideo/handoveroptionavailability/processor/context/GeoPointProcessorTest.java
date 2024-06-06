package ru.mvideo.handoveroptionavailability.processor.context;

import java.util.Set;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentMatchers;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;
import ru.mvideo.handoveroptionavailability.model.CoordinatePoint;
import ru.mvideo.handoveroptionavailability.processor.model.DeliveryContext;
import ru.mvideo.handoveroptionavailability.service.external.geocoder.GeoCoderService;
import ru.mvideo.lards.geospatial.model.GeoPoint;

@ExtendWith(MockitoExtension.class)
public class GeoPointProcessorTest {

	@InjectMocks
	private GeoPointProcessor processor;

	@Mock
	private GeoCoderService geoCoderService;

	@BeforeEach
	public void beforeEach() {
		Mockito.clearInvocations(geoCoderService);
	}

	@DisplayName("Проверка случая, когда в контексте содержится текст с адресом и не удается получить координаты из сервиса")
	@Test
	public void test_executeProcessor_with_null_coordinatePoint() {
		final var context = DeliveryContext.builder()
				.coordinatePoint(null)
				.destination("any")
				.build();

		final var geoPoint = new GeoPoint(1, 2);
		Mockito.when(geoCoderService.convertAddressToCoordinates(ArgumentMatchers.anyString())).thenReturn(Mono.just(geoPoint));

		Mono<DeliveryContext> result = processor.executeProcessor(context);

		StepVerifier.create(result)
				.consumeNextWith(ctx -> Assertions.assertEquals(geoPoint, ctx.geoPoint()))
				.verifyComplete();
		Mockito.verify(geoCoderService, Mockito.times(1)).convertAddressToCoordinates(ArgumentMatchers.eq(context.destination()));
	}

	@DisplayName("Проверка случая, когда в контексте содержится текст с адресом и удается получить координаты из сервиса")
	@Test
	public void test_executeProcessor_with_coordinatePoint() {
		final var coordinatePoint = new CoordinatePoint(10D, 20D, CoordinatePoint.QcGeoEnum.EXACTLY);
		final var context = DeliveryContext.builder()
				.coordinatePoint(coordinatePoint)
				.destination("any")
				.build();

		Mono<DeliveryContext> result = processor.executeProcessor(context);

		StepVerifier.create(result)
				.consumeNextWith(ctx -> {
					Assertions.assertEquals(coordinatePoint.getLatitude(), ctx.geoPoint().getLatitude());
					Assertions.assertEquals(coordinatePoint.getLongitude(), ctx.geoPoint().getLongitude());
				})
				.verifyComplete();
	}

	@DisplayName("Проверка метода shouldRun - опции соответствуют разрешенным")
	@Test
	public void test_shouldRun_with_etaDelivery_context() {
		final var handoverOptions = Set.of("eta-delivery", "exactly-time-delivery", "dpd-delivery");
		final var context =
				DeliveryContext.builder()
						.requestHandoverOptions(handoverOptions)
						.build();

		Assertions.assertTrue(processor.shouldRun(context));
	}

	@DisplayName("Проверка метода shouldRun - опции не соответствуют разрешенным")
	@Test
	public void test_shouldRun_without_etaDelivery_context() {
		final var handoverOptions = Set.of("dpd-delivery", "quota-delivery-regular");
		final var context =
				DeliveryContext.builder()
						.requestHandoverOptions(handoverOptions)
						.build();

		Assertions.assertFalse(processor.shouldRun(context));
	}
}
