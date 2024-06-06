package ru.mvideo.handoveroptionavailability.processor.validation;

import java.util.List;
import java.util.Set;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import reactor.test.StepVerifier;
import ru.mvideo.handoveroptionavailability.model.HandoverOption;
import ru.mvideo.handoveroptionavailability.processor.filter.fit.ExtendedProduct;
import ru.mvideo.handoveroptionavailability.processor.model.BriefAndPickupContext;
import ru.mvideo.product.model.ProductDto;

@ExtendWith(MockitoExtension.class)
public class ProductAttributesValidationProcessorTest {

	private final ProductAttributesValidationProcessor<BriefAndPickupContext> processor = new ProductAttributesValidationProcessor<>();

	@DisplayName("Процессор должен запуститься для опции PICKUP_PARTNER")
	@Test
	public void shouldRun() {
		final BriefAndPickupContext context = BriefAndPickupContext.builder()
				.requestHandoverOptions(Set.of(HandoverOption.PICKUP_PARTNER.getValue()))
				.build();

		Assertions.assertTrue(processor.shouldRun(context));
	}

	@DisplayName("Процессор не должен запуститься для опции PICKUP")
	@Test
	public void shouldNotRun() {
		final BriefAndPickupContext context = BriefAndPickupContext.builder()
				.requestHandoverOptions(Set.of(HandoverOption.PICKUP.getValue()))
				.build();

		Assertions.assertFalse(processor.shouldRun(context));
	}

	@DisplayName("В случае отсутствия у товара, опции участвующие в задаче о рюкзаке должны быть исключены")
	@Test
	public void shouldDisableOptions() {
		final var product = new ProductDto();
		product.setProductId("1");
		final var extendedProduct = new ExtendedProduct();
		extendedProduct.setProduct(product);
		final BriefAndPickupContext context = BriefAndPickupContext.builder()
				.requestHandoverOptions(Set.of(HandoverOption.PICKUP_PARTNER.getValue()))
				.build();
		context.products(List.of(extendedProduct));

		StepVerifier.create(processor.executeProcessor(context))
				.assertNext(c -> {
					Assertions.assertFalse(c.hasOption(HandoverOption.ETA_DELIVERY.getValue()));
					Assertions.assertFalse(c.hasOption(HandoverOption.EXACTLY_TIME_DELIVERY.getValue()));
					Assertions.assertFalse(c.hasOption(HandoverOption.PICKUP_PARTNER.getValue()));
					Assertions.assertFalse(c.hasOption(HandoverOption.PICKUP_SEAMLESS.getValue()));
				})
				.verifyComplete();
	}
}
