package ru.mvideo.handoveroptionavailability.processor.context.delivery;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;
import ru.mvideo.handoveroptionavailability.model.RetailBrand;
import ru.mvideo.handoveroptionavailability.processor.model.DeliveryContext;
import ru.mvideo.handoveroptionavailability.service.external.zonepickupitem.ZonePickupItemServiceImpl;
import ru.mvideo.lards.geospatial.model.GeoPoint;
import ru.mvideo.lards.handover.option.model.ProviderAttribute;
import ru.mvideo.lards.handover.option.model.ProviderZoneAttributesHandoverOptions;
import ru.mvideo.lards.zone.pickup.item.model.HandoverPickupObjectDistanceResponse;
import ru.mvideo.lards.zone.pickup.item.model.ObjectType;
import ru.mvideo.lards.zone.pickup.item.model.ZoneHandoverPickupObjectDistanceResponse;

@ExtendWith(MockitoExtension.class)
class PickupObjectsInRadiusProcessorTest {

	@InjectMocks
	private PickupObjectsInRadiusProcessor processor;

	@Mock
	private ZonePickupItemServiceImpl zonePickupItemService;

	@DisplayName("Проверка случая, когда от zonePickupItemService получаем не пустой ответ")
	@Test
	public void test_executeProcessor_with_notEmptyResponse() {

		final var context = DeliveryContext.builder()
				.regionId("S002")
				.retailBrand(RetailBrand.MVIDEO)
				.requestHandoverOptions(Set.of("eta-delivery"))
				.build();

		var provider = new ProviderZoneAttributesHandoverOptions();
		var attribute = new ProviderAttribute();
		attribute.setName("source_radius");
		attribute.setValue("50");
		provider.setProviderAttributes(List.of(attribute));

		context.providers(List.of(provider));
		context.geoPoint(new GeoPoint(1, 2));

		HandoverPickupObjectDistanceResponse distanceResponse = new HandoverPickupObjectDistanceResponse("s1", ObjectType.SHOP, new GeoPoint(1, 2), 70);
		List<ZoneHandoverPickupObjectDistanceResponse> zoneHandoverPickupObjects = List.of(new ZoneHandoverPickupObjectDistanceResponse("S002", List.of(distanceResponse)));


		Mockito.when(zonePickupItemService.getObjectsByRadius(context.regionId(), context.retailBrand().getValue(), context.geoPoint(), 50.0)).thenReturn(Flux.fromIterable(zoneHandoverPickupObjects));

		Mono<DeliveryContext> result = processor.executeProcessor(context);

		StepVerifier.create(result)
				.consumeNextWith(ctx -> {
					Assertions.assertEquals(1, ctx.options().size());
					Assertions.assertEquals(1, ctx.handoverObjects().size());
				})
				.verifyComplete();
	}

	@DisplayName("Проверка случая, когда от zonePickupItemService получаем пустой ответ")
	@Test
	public void test_executeProcessor_with_emptyResponse() {

		final var context = DeliveryContext.builder()
				.regionId("S002")
				.retailBrand(RetailBrand.MVIDEO)
				.requestHandoverOptions(Set.of("eta-delivery"))
				.build();

		var provider = new ProviderZoneAttributesHandoverOptions();
		var attribute = new ProviderAttribute();
		attribute.setName("source_radius");
		attribute.setValue("50");
		provider.setProviderAttributes(List.of(attribute));

		context.providers(List.of(provider));
		context.geoPoint(new GeoPoint(1, 2));

		List<ZoneHandoverPickupObjectDistanceResponse> zoneHandoverPickupObjects = List.of(new ZoneHandoverPickupObjectDistanceResponse("S002", new ArrayList<>()));

		Mockito.when(zonePickupItemService.getObjectsByRadius(context.regionId(), context.retailBrand().getValue(), context.geoPoint(), 50.0)).thenReturn(Flux.fromIterable(zoneHandoverPickupObjects));

		Mono<DeliveryContext> result = processor.executeProcessor(context);

		StepVerifier.create(result)
				.consumeNextWith(ctx -> Assertions.assertEquals(0, ctx.options().size()))
				.verifyComplete();
	}
}