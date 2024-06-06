package ru.mvideo.handoveroptionavailability.processor.context;

import java.util.ArrayList;
import java.util.List;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;
import ru.mvideo.handoveroptionavailability.exception.HandoverOptionAvailabilityValidationException;
import ru.mvideo.handoveroptionavailability.model.RetailBrand;
import ru.mvideo.handoveroptionavailability.processor.model.DeliveryContext;
import ru.mvideo.handoveroptionavailability.service.external.zone.ZoneInfoService;
import ru.mvideo.lards.geospatial.model.GeoPolygon;
import ru.mvideo.lards.zone.model.AttributeDataType;
import ru.mvideo.lards.zone.model.ZoneAttributeResponse;
import ru.mvideo.lards.zone.model.ZoneBrand;
import ru.mvideo.lards.zone.model.ZoneDetailResponse;
import ru.mvideo.lards.zone.model.ZoneRole;

@ExtendWith(MockitoExtension.class)
public class LoadRegionalZoneAttributesProcessorTest {

	@InjectMocks
	private LoadRegionalZoneAttributesProcessor<DeliveryContext> processor;

	@Mock
	private ZoneInfoService zoneInfoService;

	@BeforeEach
	public void beforeEach() {
		Mockito.clearInvocations(zoneInfoService);
	}

	@DisplayName("Проверка метода shouldRun")
	@Test
	public void test_shouldRun() {
		Assertions.assertTrue(processor.shouldRun(DeliveryContext.builder().build()));
	}

	@DisplayName("Проверка получения ответа, содержащего корректные данные бренда")
	@Test
	public void test_executeProcessor_correctData() {
		final var context = DeliveryContext.builder()
				.retailBrand(RetailBrand.MVIDEO)
				.regionId("S147")
				.build();

		final var zoneAttribute = new ZoneAttributeResponse("1", "1", AttributeDataType.STRING, "1", "1");
		final var zoneAttributes = List.of(zoneAttribute);

		final var zoneDetailResponse =
				new ZoneDetailResponse("S147", "1", ZoneBrand.MVIDEO, false, ZoneRole.REGULAR, new GeoPolygon(new ArrayList<>()), zoneAttributes);
		Mockito.when(zoneInfoService.getRegionZoneDetails(Mockito.anyString())).thenReturn(Mono.just(zoneDetailResponse));

		Mono<DeliveryContext> result = processor.executeProcessor(context);

		StepVerifier.create(result)
				.consumeNextWith(ctx -> Assertions.assertEquals(zoneDetailResponse, context.regionDetails()))
				.verifyComplete();
		Mockito.verify(zoneInfoService, Mockito.times(1)).getRegionZoneDetails(context.regionId());
	}

	@DisplayName("Проверка получения ответа, содержащего некорректные данные бренда")
	@Test
	public void test_executeProcessor_not_correctData() {
		final var context = DeliveryContext.builder()
				.retailBrand(RetailBrand.ELDORADO)
				.regionId("S147")
				.build();

		final var zoneAttribute = new ZoneAttributeResponse("1", "1", AttributeDataType.STRING, "1", "1");
		final var zoneAttributes = List.of(zoneAttribute);

		final var zoneDetailResponse =
				new ZoneDetailResponse("S147", "1", ZoneBrand.MVIDEO, false, ZoneRole.REGULAR, new GeoPolygon(new ArrayList<>()), zoneAttributes);
		Mockito.when(zoneInfoService.getRegionZoneDetails(Mockito.anyString())).thenReturn(Mono.just(zoneDetailResponse));

		Mono<DeliveryContext> result = processor.executeProcessor(context);

		StepVerifier.create(result)
				.verifyError(HandoverOptionAvailabilityValidationException.class);
		Mockito.verify(zoneInfoService, Mockito.times(1)).getRegionZoneDetails(context.regionId());
	}
}
