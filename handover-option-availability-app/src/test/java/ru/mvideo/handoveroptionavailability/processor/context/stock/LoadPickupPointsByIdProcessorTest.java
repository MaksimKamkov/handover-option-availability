package ru.mvideo.handoveroptionavailability.processor.context.stock;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.math.BigDecimal;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Set;
import org.junit.jupiter.api.Assertions;
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
import ru.mvideo.handoveroptionavailability.model.HandoverOption;
import ru.mvideo.handoveroptionavailability.processor.model.StockObjectsContext;
import ru.mvideo.handoveroptionavailability.service.external.pickuppoints.PickupPointsService;
import ru.mvideo.io.pickup.points.lib.model.enums.PickupPointTypeEnum;
import ru.mvideo.io.pickup.points.lib.model.response.Address;
import ru.mvideo.io.pickup.points.lib.model.response.CellLimit;
import ru.mvideo.io.pickup.points.lib.model.response.PickupPoint;

@ExtendWith(MockitoExtension.class)
public class LoadPickupPointsByIdProcessorTest {

	@InjectMocks
	private LoadPickupPointsByIdProcessor processor;
	@Mock
	private PickupPointsService pickupPointsService;

	@DisplayName("Процессор должен выполниться")
	@Test
	public void shouldRunProcessor() {
		final var context = StockObjectsContext.builder()
				.requestHandoverOptions(Set.of(HandoverOption.PICKUP_PARTNER.getValue()))
				.build();
		assertTrue(processor.shouldRun(context));
	}


	@DisplayName("Процессор не должен выполниться")
	@Test
	public void shouldNotRunProcessor() {
		final var context = StockObjectsContext.builder()
				.requestHandoverOptions(Set.of(HandoverOption.ETA_DELIVERY.getValue()))
				.build();
		Assertions.assertFalse(processor.shouldRun(context));
	}

	@DisplayName("Загрузка данных о ПВЗ в контекст")
	@Test
	public void shouldLoadPickupPoints() {
		final var pickupPoints = List.of(getPickupPoint());
		Mockito.when(pickupPointsService.getPickPoints(ArgumentMatchers.anyList()))
				.thenReturn(Mono.just(pickupPoints));

		final var context = StockObjectsContext.builder()
				.requestHandoverOptions(Set.of(HandoverOption.PICKUP_PARTNER.getValue()))
				.pickupPointId("8e46af69-71ee-465a-8ef2-d19f6721bcb9")
				.build();

		final var result = processor.executeProcessor(context);

		StepVerifier.create(result)
				.consumeNextWith(ctx -> assertEquals(pickupPoints, ctx.pickupPoints()))
				.verifyComplete();

		Mockito.verify(pickupPointsService, Mockito.times(1)).getPickPoints(ArgumentMatchers.anyList());
	}

	private PickupPoint getPickupPoint() {
		CellLimit cellLimit = new CellLimit(
				"101",
				200,
				200,
				200,
				200
		);
		Address address = new Address(
				"zipCode",
				"region",
				"area",
				"city",
				"location",
				"street",
				"house",
				"building",
				"block",
				"porch",
				"metroStation");
		return new PickupPoint(
				"8e46af69-71ee-465a-8ef2-d19f6721bcb9",
				"partnerBrand",
				"partnerId",
				"partnerName",
				"sapCode",
				"eldoSapCode",
				"rimCode",
				"eldoRimCode",
				PickupPointTypeEnum.PICKUP_POINT,
				0.0,
				0.0,
				address,
				false,
				Collections.emptyList(),
				Collections.singletonList(cellLimit),
				BigDecimal.TEN,
				BigDecimal.ZERO,
				3,
				0,
				new HashMap<>(),
				null,
				true,
				true
		);
	}
}
