package ru.mvideo.handoveroptionavailability.processor.filter;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;
import reactor.test.StepVerifier;
import ru.mvideo.handoveroptionavailability.model.HandoverOption;
import ru.mvideo.handoveroptionavailability.model.external.msp.logistic.response.AvailabilityOption;
import ru.mvideo.handoveroptionavailability.processor.filter.fit.ExtendedProduct;
import ru.mvideo.handoveroptionavailability.processor.model.DeliveryContext;
import ru.mvideo.handoveroptionavailability.processor.model.HandoverObject;
import ru.mvideo.handoveroptionavailability.processor.model.OptionContext;
import ru.mvideo.lards.handover.option.model.ProviderZoneAttributesHandoverOptions;
import ru.mvideo.lards.price.rules.model.MinPriceResponse;
import ru.mvideo.product.model.ProductDto;

@ExtendWith(MockitoExtension.class)
public class MinRuleProcessorTest {

	@InjectMocks
	private MinRuleProcessor<DeliveryContext> processor;

	@DisplayName("Есть цена, есть магазин в радиусе, опция доступна")
	@Test
	void executeProcessor() {

		var rule = createMinPriceRule();

		DeliveryContext context = createContext();
		final var result = processor.executeProcessor(context);

		StepVerifier.create(result)
				.consumeNextWith(ctx -> {
					assertEquals(rule, context.handoverOptionContext().get(HandoverOption.ETA_DELIVERY.getValue()).getMinPriceRule());
					assertEquals(1, context.options().size());
				})
				.verifyComplete();
	}

	@DisplayName("Нет ценого правила, опция не доступна")
	@Test
	void executeProcessor1() {

		DeliveryContext context = createContext();
		context.minPriceRules(List.of());

		final var result = processor.executeProcessor(context);

		StepVerifier.create(result)
				.consumeNextWith(ctx -> {
					assertNull(context.handoverOptionContext().get(HandoverOption.ETA_DELIVERY.getValue()).getMinPriceRule());
					assertEquals(0, context.options().size());
				})
				.verifyComplete();
	}


	private MinPriceResponse createMinPriceRule() {
		var rule = new MinPriceResponse();
		rule.setPrice(BigDecimal.valueOf(100.0));
		rule.setMinRadius(10.0);
		rule.setMaxRadius(14.0);
		rule.setZoneId("S002");
		rule.setHandoverOptionName("eta-delivery");
		return rule;
	}

	private List<ExtendedProduct> getProducts() {
		final var productDto1 = new ProductDto();
		productDto1.setProductId("123");

		final var product1 = new ExtendedProduct();
		product1.setProduct(productDto1);
		product1.setQty(3);
		product1.setPrice(10000.0);

		final var productDto2 = new ProductDto();
		productDto2.setProductId("456");

		final var product2 = new ExtendedProduct();
		product2.setProduct(productDto2);
		product2.setQty(1);
		product2.setPrice(30000.0);

		return List.of(product1, product2);
	}

	private DeliveryContext createContext() {

		final var requestHandoverOptions = Set.of(
				HandoverOption.ETA_DELIVERY.getValue()
		);

		var rule = createMinPriceRule();

		final var provider = new ProviderZoneAttributesHandoverOptions();
		provider.setZoneIds(List.of("S002"));
		ru.mvideo.lards.handover.option.model.HandoverOption handoverOption1 = new ru.mvideo.lards.handover.option.model.HandoverOption();
		handoverOption1.setName("eta-delivery");
		provider.setHandoverOptions(List.of(handoverOption1));


		List<ProviderZoneAttributesHandoverOptions> providers = List.of(provider);

		OptionContext optionContext = new OptionContext();

		AvailabilityOption option1 = new AvailabilityOption();
		option1.setHandoverObject("S11");
		AvailabilityOption option2 = new AvailabilityOption();
		option2.setHandoverObject("S22");
		List<AvailabilityOption> availabilityOptions = List.of(option1, option2);
		optionContext.setAvailabilityOptions(availabilityOptions);

		HandoverObject object1 = new HandoverObject();
		object1.setObjectId("S11");
		object1.setDistance(15.0);
		HandoverObject object2 = new HandoverObject();
		object2.setObjectId("S22");
		object2.setDistance(17.0);
		List<HandoverObject> handoverObjects = List.of(object1, object2);
		optionContext.setHandoverObjects(handoverObjects);

		Map<String, OptionContext> handoverOptionContext = new HashMap<>();
		handoverOptionContext.put(HandoverOption.ETA_DELIVERY.getValue(), optionContext);

		final var context = DeliveryContext.builder()
				.requestHandoverOptions(requestHandoverOptions)
				.handoverOptionContext(handoverOptionContext)
				.build();
		context.products(getProducts())
				.minPriceRules(List.of(rule))
				.providers(providers);
		return context;
	}
}