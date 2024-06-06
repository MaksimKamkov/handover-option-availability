package ru.mvideo.handoveroptionavailability.processor.filter.stock;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Set;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import reactor.test.StepVerifier;
import ru.mvideo.handoveroptionavailability.model.HandoverOption;
import ru.mvideo.handoveroptionavailability.model.Material;
import ru.mvideo.handoveroptionavailability.model.RetailBrand;
import ru.mvideo.handoveroptionavailability.model.external.msp.logistic.response.AvailabilityOption;
import ru.mvideo.handoveroptionavailability.processor.model.StockObjectsContext;
import ru.mvideo.io.pickup.points.lib.model.enums.PickupPointTypeEnum;
import ru.mvideo.io.pickup.points.lib.model.response.Address;
import ru.mvideo.io.pickup.points.lib.model.response.CellLimit;
import ru.mvideo.io.pickup.points.lib.model.response.PickupPoint;

public class PickupPartnerStockFilterProcessorTest {

	private final PickupPartnerStockFilterProcessor processor = new PickupPartnerStockFilterProcessor();

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
		assertFalse(processor.shouldRun(context));
	}

	@DisplayName("Весь заказ доступен на одном объекте")
	@Test
	public void shouldSuccessCheckEntireOrderInOnePlace() {
		final var context = StockObjectsContext.builder()
				.requestHandoverOptions(Set.of(HandoverOption.PICKUP_PARTNER.getValue()))
				.retailBrand(RetailBrand.MVIDEO)
				.materials(List.of(Material.builder()
						.material("1234567890")
						.price(1000.0)
						.qty(1)
						.build()))
				.build();
		context.pickupPoints(List.of(getPickupPoint()));

		final var option = new AvailabilityOption();
		option.setHandoverObject("S022");
		option.setStockObject("S022");
		option.setMaterial("1234567890");
		option.setAvailableStock(2);
		option.setShowCaseStock(0);
		option.setAvailableDate(LocalDate.now());
		option.setValidTo(LocalDateTime.now());
		context.availabilityOptions(List.of(option));

		final var result = processor.executeProcessor(context);

		StepVerifier.create(result)
				.consumeNextWith(ctx -> assertTrue(ctx.options().contains(HandoverOption.PICKUP_PARTNER.getValue())))
				.verifyComplete();
	}

	@DisplayName("Не весь заказ доступен на одном объекте")
	@Test
	public void shouldFailCheckEntireOrderInOnePlace() {
		final var context = StockObjectsContext.builder()
				.requestHandoverOptions(Set.of(HandoverOption.PICKUP_PARTNER.getValue()))
				.retailBrand(RetailBrand.MVIDEO)
				.materials(List.of(
								Material.builder()
										.material("1234567890")
										.price(1000.0)
										.qty(1)
										.build(),
								Material.builder()
										.material("1234567891")
										.price(1000.0)
										.qty(1)
										.build()
						)
				)
				.build();
		context.pickupPoints(List.of(getPickupPoint()));

		final var option1 = new AvailabilityOption();
		option1.setHandoverObject("S022");
		option1.setStockObject("S022");
		option1.setMaterial("1234567890");
		option1.setAvailableStock(2);
		option1.setShowCaseStock(0);
		option1.setAvailableDate(LocalDate.now());
		option1.setValidTo(LocalDateTime.now());

		final var option2 = new AvailabilityOption();
		option2.setHandoverObject("S023");
		option2.setStockObject("S023");
		option2.setMaterial("1234567891");
		option2.setAvailableStock(2);
		option2.setShowCaseStock(0);
		option2.setAvailableDate(LocalDate.now());
		option2.setValidTo(LocalDateTime.now());

		context.availabilityOptions(List.of(option1, option2));

		final var result = processor.executeProcessor(context);

		StepVerifier.create(result)
				.consumeNextWith(ctx -> {
					assertFalse(ctx.options().contains(HandoverOption.PICKUP_PARTNER.getValue()));
					assertTrue(ctx.options().isEmpty());
				})
				.verifyComplete();
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
				"S022",
				"S022",
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
