package ru.mvideo.handoveroptionavailability.processor.context;

import java.util.Collections;
import java.util.List;
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
import ru.mvideo.handoveroptionavailability.processor.model.DeliveryContext;
import ru.mvideo.handoveroptionavailability.service.external.providers.ProvidersService;
import ru.mvideo.lards.geospatial.model.GeoPolygon;
import ru.mvideo.lards.handover.option.model.HandoverOption;
import ru.mvideo.lards.handover.option.model.ProviderZoneAttributesHandoverOptions;
import ru.mvideo.lards.zone.model.ZoneBrand;
import ru.mvideo.lards.zone.model.ZoneResponse;
import ru.mvideo.lards.zone.model.ZoneRole;

@ExtendWith(MockitoExtension.class)
public class LoadProvidersProcessorTest {

	@InjectMocks
	private LoadProvidersProcessor<DeliveryContext> processor;

	@Mock
	private ProvidersService providersService;

	@BeforeEach
	public void beforeEach() {
		Mockito.clearInvocations(providersService);
	}

	@DisplayName("Проверка метода shouldRun")
	@Test
	public void test_shouldRun() {
		final var context = DeliveryContext.builder().build();
		Assertions.assertTrue(processor.shouldRun(context));
	}

	@DisplayName("Проверка получения не пустого списка")
	@Test
	public void test_executeProcessor_with_not_emptyResponse() {

		final var context = DeliveryContext.builder()
				.regionId("S002")
				.requestHandoverOptions(Set.of("eta-delivery"))
				.build();
		final var zone = new ZoneResponse("S002", "1", ZoneBrand.MVIDEO, false, ZoneRole.REGULAR, new GeoPolygon(Collections.emptyList()));
		context.zones(List.of(zone));

		final var provider = new ProviderZoneAttributesHandoverOptions();
		provider.setZoneIds(List.of("S002"));
		HandoverOption handoverOption1 = new HandoverOption();
		handoverOption1.setName("eta-delivery");
		provider.setHandoverOptions(List.of(handoverOption1));

		final var providers = List.of(provider);

		Mockito.when(providersService.findProviders(ArgumentMatchers.anySet(), ArgumentMatchers.anySet(), ArgumentMatchers.anySet())).thenReturn(Mono.just(providers));

		StepVerifier.create(processor.executeProcessor(context))
				.consumeNextWith(ctx -> Assertions.assertEquals(providers, ctx.providers()))
				.verifyComplete();
		Mockito.verify(providersService, Mockito.times(1)).findProviders(ArgumentMatchers.anySet(), ArgumentMatchers.anySet(), ArgumentMatchers.anySet());
	}

}
