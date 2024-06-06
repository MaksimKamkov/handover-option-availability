package ru.mvideo.handoveroptionavailability.processor.request;

import java.util.List;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import reactor.test.StepVerifier;
import ru.mvideo.handoveroptionavailability.exception.UnsupportedOptionException;
import ru.mvideo.handoveroptionavailability.model.HandoverOption;
import ru.mvideo.handoveroptionavailability.model.Material;
import ru.mvideo.handoveroptionavailability.model.RetailBrand;
import ru.mvideo.handoveroptionavailability.model.StockObjectsRequest;

public class StockObjectsContextProducerTest {

	private final StockObjectsContextProducer producer = new StockObjectsContextProducer();

	@DisplayName("Успешное создание контекста из запроса")
	@Test
	public void shouldReturnContext() {
		final var request = StockObjectsRequest.builder()
				.regionId("S002")
				.retailBrand(RetailBrand.MVIDEO)
				.handoverOption(HandoverOption.PICKUP)
				.handoverObject("S026")
				.materials(List.of(Material.builder()
						.material("1234567890")
						.price(1000.0)
						.qty(1)
						.build()))
				.build();

		StepVerifier.create(producer.produce(request))
				.assertNext(context -> {
					Assertions.assertTrue(context.requestHandoverOptions().contains(HandoverOption.PICKUP.getValue()));
					Assertions.assertEquals("S002", context.regionId());
					Assertions.assertEquals(RetailBrand.MVIDEO, context.retailBrand());
					Assertions.assertTrue(context.pickupObjectIds().contains("S026"));
					Assertions.assertEquals(1, context.materials().size());
				})
				.verifyComplete();
	}

	@DisplayName("Неподдерживаемая опция")
	@Test
	public void shouldFailUnsupportedOption() {
		final var request = StockObjectsRequest.builder()
				.regionId("S002")
				.retailBrand(RetailBrand.MVIDEO)
				.handoverOption(HandoverOption.ETA_DELIVERY)
				.handoverObject("S026")
				.materials(List.of(Material.builder()
						.material("1234567890")
						.price(1000.0)
						.qty(1)
						.build()))
				.build();

		StepVerifier.create(producer.produce(request))
				.verifyError(UnsupportedOptionException.class);
	}

	@DisplayName("Не заполнено поле pickupPointId для опции pickup-partner")
	@Test
	public void shouldFailMissedPickupPartner() {
		final var request = StockObjectsRequest.builder()
				.regionId("S002")
				.retailBrand(RetailBrand.MVIDEO)
				.handoverOption(HandoverOption.PICKUP_PARTNER)
				.handoverObject("S026")
				.materials(List.of(Material.builder()
						.material("1234567890")
						.price(1000.0)
						.qty(1)
						.build()))
				.build();

		StepVerifier.create(producer.produce(request))
				.verifyError(UnsupportedOptionException.class);
	}
}
