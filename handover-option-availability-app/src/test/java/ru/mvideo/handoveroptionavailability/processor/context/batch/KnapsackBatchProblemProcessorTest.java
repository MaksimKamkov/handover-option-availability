package ru.mvideo.handoveroptionavailability.processor.context.batch;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentMatchers;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import reactor.test.StepVerifier;
import ru.mvideo.handoveroptionavailability.model.HandoverOption;
import ru.mvideo.handoveroptionavailability.processor.filter.fit.ExtendedProduct;
import ru.mvideo.handoveroptionavailability.processor.filter.fit.FitService;
import ru.mvideo.handoveroptionavailability.processor.model.BatchContext;
import ru.mvideo.lards.handover.option.model.ProviderAttribute;
import ru.mvideo.lards.handover.option.model.ProviderZoneAttributesHandoverOptions;
import ru.mvideo.product.model.ProductDto;

@ExtendWith(MockitoExtension.class)
class KnapsackBatchProblemProcessorTest {

	@InjectMocks
	private KnapsackBatchProblemProcessor processor;

	@Mock
	private FitService fitService;

	@BeforeEach
	public void beforeEach() {
		Mockito.clearInvocations(fitService);
	}

	@DisplayName("Все товары с рюкзаком и проходят алгоритм")
	@Test
	public void test1() {

		final var context = BatchContext.builder()
				.requestHandoverOptions(Set.of(HandoverOption.ETA_DELIVERY.getValue(), HandoverOption.INTERVAL_DELIVERY.getValue()))
				.build();
		context.providers(getProviders());
		context.products(List.of(getProduct1(), getProduct2()));

		final Map<String, Set<String>> materialHandoverOptions = new HashMap<>();
		materialHandoverOptions.put("123", Set.of("eta-delivery", "interval-delivery"));
		materialHandoverOptions.put("456", Set.of("eta-delivery", "interval-delivery"));

		context.materialHandoverOption(materialHandoverOptions);

		Mockito.when(fitService.anyMatchProvider(ArgumentMatchers.anyList(), ArgumentMatchers.anyList())).thenReturn(true);

		final var result = processor.executeProcessor(context);

		StepVerifier.create(result)
				.consumeNextWith(ctx -> {
					assertEquals(2, ctx.materialHandoverOption().get("123").size());
					assertEquals(2, ctx.materialHandoverOption().get("456").size());
				})
				.verifyComplete();

		Mockito.verify(fitService, Mockito.times(2)).anyMatchProvider(ArgumentMatchers.anyList(), ArgumentMatchers.anyList());
	}

	@DisplayName("Для одного товара выполнится рюкзак")
	@Test
	public void test2() {

		final var context = BatchContext.builder()
				.requestHandoverOptions(Set.of(HandoverOption.ETA_DELIVERY.getValue(), HandoverOption.INTERVAL_DELIVERY.getValue()))
				.build();
		context.providers(getProviders());
		context.products(List.of(getProduct1(), getProduct2()));

		final Map<String, Set<String>> materialHandoverOptions = new HashMap<>();
		materialHandoverOptions.put("123", Set.of("eta-delivery", "interval-delivery"));
		materialHandoverOptions.put("456", Set.of("interval-delivery"));

		context.materialHandoverOption(materialHandoverOptions);

		Mockito.when(fitService.anyMatchProvider(ArgumentMatchers.anyList(), ArgumentMatchers.anyList())).thenReturn(true);

		final var result = processor.executeProcessor(context);

		StepVerifier.create(result)
				.consumeNextWith(ctx -> {
					assertEquals(2, ctx.materialHandoverOption().get("123").size());
					assertEquals(1, ctx.materialHandoverOption().get("456").size());
				})
				.verifyComplete();

		Mockito.verify(fitService, Mockito.times(1)).anyMatchProvider(ArgumentMatchers.anyList(), ArgumentMatchers.anyList());
	}

	@DisplayName("Для одного товара отфильтруется опция")
	@Test
	public void test3() {

		final var context = BatchContext.builder()
				.requestHandoverOptions(Set.of(HandoverOption.ETA_DELIVERY.getValue(), HandoverOption.INTERVAL_DELIVERY.getValue()))
				.build();
		context.providers(getProviders());
		context.products(List.of(getProduct1(), getProduct2()));

		final Map<String, Set<String>> materialHandoverOptions = new HashMap<>();
		materialHandoverOptions.put("123", Set.of("eta-delivery", "interval-delivery"));
		materialHandoverOptions.put("456", Set.of("interval-delivery"));

		context.materialHandoverOption(materialHandoverOptions);

		Mockito.when(fitService.anyMatchProvider(ArgumentMatchers.anyList(), ArgumentMatchers.anyList())).thenReturn(false);

		final var result = processor.executeProcessor(context);

		StepVerifier.create(result)
				.consumeNextWith(ctx -> {
					assertEquals(1, ctx.materialHandoverOption().get("123").size());
					assertEquals(1, ctx.materialHandoverOption().get("456").size());
				})
				.verifyComplete();

		Mockito.verify(fitService, Mockito.times(1)).anyMatchProvider(ArgumentMatchers.anyList(), ArgumentMatchers.anyList());
	}


	private List<ProviderZoneAttributesHandoverOptions> getProviders() {
		final var provider = new ProviderZoneAttributesHandoverOptions();
		provider.setZoneIds(List.of("S002"));
		final var handoverOption = new ru.mvideo.lards.handover.option.model.HandoverOption();
		handoverOption.setName("eta-delivery");
		provider.setHandoverOptions(List.of(handoverOption));


		final var length = new ProviderAttribute();
		length.setName("max_length");
		length.setValue("");

		final var width = new ProviderAttribute();
		width.setName("max_width");
		width.setValue("");

		final var height = new ProviderAttribute();
		height.setName("max_height");
		height.setValue("");

		final var weight = new ProviderAttribute();
		weight.setName("max_weight");
		weight.setValue("");

		List<ProviderAttribute> providerAttributes = new ArrayList<>();
		providerAttributes.add(length);
		providerAttributes.add(width);
		providerAttributes.add(height);
		providerAttributes.add(weight);

		provider.setProviderAttributes(providerAttributes);

		return List.of(provider);
	}

	private ExtendedProduct getProduct1() {
		final var productDto1 = new ProductDto();
		productDto1.setProductId("123");

		final var product1 = new ExtendedProduct();
		product1.setProduct(productDto1);
		product1.setQty(1);
		product1.setPrice(10000.0);

		return product1;
	}

	private ExtendedProduct getProduct2() {

		final var productDto2 = new ProductDto();
		productDto2.setProductId("456");

		final var product2 = new ExtendedProduct();
		product2.setProduct(productDto2);
		product2.setQty(1);
		product2.setPrice(30000.0);

		return product2;
	}
}