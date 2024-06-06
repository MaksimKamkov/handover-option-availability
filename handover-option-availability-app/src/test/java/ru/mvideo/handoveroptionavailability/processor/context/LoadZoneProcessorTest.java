package ru.mvideo.handoveroptionavailability.processor.context;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

import java.util.Collections;
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
import ru.mvideo.handoveroptionavailability.model.RetailBrand;
import ru.mvideo.handoveroptionavailability.processor.model.DeliveryContext;
import ru.mvideo.handoveroptionavailability.service.external.zone.ZoneInfoService;
import ru.mvideo.lards.geospatial.model.GeoPolygon;
import ru.mvideo.lards.zone.model.ZoneBrand;
import ru.mvideo.lards.zone.model.ZoneResponse;
import ru.mvideo.lards.zone.model.ZoneRole;

@ExtendWith(MockitoExtension.class)
public class LoadZoneProcessorTest {

	@InjectMocks
	private LoadZoneProcessor<DeliveryContext> processor;

	@Mock
	private ZoneInfoService zoneInfo;

	@BeforeEach
	public void beforeEach() {
		Mockito.clearInvocations(zoneInfo);
	}

	@DisplayName("Проверка метода shouldRun")
	@Test
	public void test_shouldRun_with_etaDelivery_context() {
		final var context = DeliveryContext.builder().build();
		Assertions.assertTrue(processor.shouldRun(context));
	}

	@DisplayName("Проверка получения не пустого списка")
	@Test
	public void test_executeProcessor_with_not_empty_zones() {

		final var context = DeliveryContext.builder()
				.regionId("S002")
				.retailBrand(RetailBrand.MVIDEO)
				.build();

		final var zone = new ZoneResponse("S002", "1", ZoneBrand.MVIDEO, false, ZoneRole.REGULAR, new GeoPolygon(Collections.emptyList()));
		final var zones = List.of(zone);

		when(zoneInfo.getIncludedZones(anyString(), anyString()))
				.thenReturn(Mono.just(zones));

		final var result = processor.executeProcessor(context);

		StepVerifier.create(result)
				.consumeNextWith(ctx -> assertEquals(zones, ctx.zones()))
				.verifyComplete();
		Mockito.verify(zoneInfo, Mockito.times(1)).getIncludedZones(
				context.regionId(),
				context.retailBrand().getValue()
		);
	}

	@DisplayName("Проверка получения пустого списка")
	@Test
	public void test_executeProcessor_with_empty_zones() {
		final var context = DeliveryContext.builder()
				.regionId("S002")
				.retailBrand(ru.mvideo.handoveroptionavailability.model.RetailBrand.MVIDEO)
				.build();

		when(zoneInfo.getIncludedZones(anyString(), anyString())).thenReturn(Mono.just(Collections.emptyList()));

		final var result = processor.executeProcessor(context);

		StepVerifier.create(result)
				.consumeNextWith(ctx -> assertEquals(Collections.emptyList(), ctx.zones()))
				.verifyComplete();
		Mockito.verify(zoneInfo, Mockito.times(1))
				.getIncludedZones(context.regionId(), context.retailBrand().getValue());
	}
}
