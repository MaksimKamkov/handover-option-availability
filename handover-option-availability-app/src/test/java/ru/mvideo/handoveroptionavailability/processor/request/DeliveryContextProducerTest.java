package ru.mvideo.handoveroptionavailability.processor.request;

import java.util.List;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import reactor.test.StepVerifier;
import ru.mvideo.handoveroptionavailability.exception.HandoverOptionAvailabilityValidationException;
import ru.mvideo.handoveroptionavailability.model.DeliveryRequestV2;
import ru.mvideo.handoveroptionavailability.model.HandoverOption;
import ru.mvideo.handoveroptionavailability.model.Material;
import ru.mvideo.handoveroptionavailability.model.RetailBrand;

public class DeliveryContextProducerTest {

	private final DeliveryContextProducer producer = new DeliveryContextProducer();

	@DisplayName("Успешное создание контекста из запроса")
	@Test
	public void shouldReturnContext() {
		final var request = DeliveryRequestV2.builder()
				.retailBrand(RetailBrand.MVIDEO)
				.regionId("S002")
				.handoverOption(List.of(HandoverOption.DPD_DELIVERY))
				.includeStocks(false)
				.materials(List.of(Material.builder()
						.material("1234567890")
						.price(1000.0)
						.qty(1)
						.build()))
				.build();

		StepVerifier.create(producer.produce(request))
				.assertNext(context -> {
					Assertions.assertTrue(context.requestHandoverOptions().contains(HandoverOption.DPD_DELIVERY.getValue()));
					Assertions.assertEquals("S002", context.regionId());
					Assertions.assertEquals(RetailBrand.MVIDEO, context.retailBrand());
					Assertions.assertNull(context.coordinatePoint());
					Assertions.assertNull(context.destination());
					Assertions.assertEquals(1, context.materials().size());
				})
				.verifyComplete();
	}

	@DisplayName("Проверка обязательности адреса")
	@Test
	public void shouldCheckRequiredAddressForEtaDelivery() {
		final var request = DeliveryRequestV2.builder()
				.handoverOption(List.of(HandoverOption.ETA_DELIVERY))
				.build();

		StepVerifier.create(producer.produce(request))
				.verifyError(HandoverOptionAvailabilityValidationException.class);
	}

	@DisplayName("Проверка того что отсутствие адреса исключает опции, но не приводит к ошибке")
	@Test
	public void shouldDisableOptionsInCaseOfMissedAddress() {
		final var request = DeliveryRequestV2.builder()
				.includeStocks(false)
				.build();

		StepVerifier.create(producer.produce(request))
				.assertNext(context -> {
					Assertions.assertFalse(context.requestHandoverOptions().contains(HandoverOption.ETA_DELIVERY.getValue()));
					Assertions.assertFalse(context.requestHandoverOptions().contains(HandoverOption.EXACTLY_TIME_DELIVERY.getValue()));
					Assertions.assertTrue(context.requestHandoverOptions().contains(HandoverOption.ELECTRONIC_DELIVERY.getValue()));
				})
				.verifyComplete();
	}

}
