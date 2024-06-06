package ru.mvideo.handoveroptionavailability.processor.filter;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
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
import ru.mvideo.lards.geospatial.model.GeoPoint;
import ru.mvideo.lards.geospatial.model.GeoPolygon;
import ru.mvideo.lards.zone.model.AttributeDataType;
import ru.mvideo.lards.zone.model.ZoneAttributeResponse;
import ru.mvideo.lards.zone.model.ZoneBrand;
import ru.mvideo.lards.zone.model.ZoneDetailResponse;
import ru.mvideo.lards.zone.model.ZoneRole;
import ru.mvideo.product.model.ProductDto;

@ExtendWith(MockitoExtension.class)
class StocksAndShowcaseProcessorTest {

	@InjectMocks
	private StocksAndShowcaseProcessor<DeliveryContext> processor;

	@DisplayName("Товара достаточно на стоках")
	@Test
	void stocksAndNoShowcase() {

		DeliveryContext context = createContext(List.of(createOptionContextStocks()));
		final var result = processor.executeProcessor(context);

		StepVerifier.create(result)
				.consumeNextWith(ctx -> {
					assertEquals(4, context.handoverOptionContext().get(HandoverOption.ETA_DELIVERY.getValue()).getAvailabilityOptions().size());
					assertEquals(1, context.options().size());
				})
				.verifyComplete();
	}

	@DisplayName("Товара достаточно есть на стоки + витрина")
	@Test
	void stocksAndShowcase() {

		DeliveryContext context = createContext(List.of(createOptionContextStocksShowcase()));
		final var result = processor.executeProcessor(context);

		StepVerifier.create(result)
				.consumeNextWith(ctx -> {
					assertEquals(2, context.handoverOptionContext().get(HandoverOption.ETA_DELIVERY.getValue()).getAvailabilityOptions().size());
					assertEquals(1, context.options().size());
				})
				.verifyComplete();
	}

	@DisplayName("Товара нет на стоках и на витрине")
	@Test
	void noStocksAndNoShowcase() {

		DeliveryContext context = createContext(List.of(createOptionContextNoStocksNoShowcase()));
		final var result = processor.executeProcessor(context);

		StepVerifier.create(result)
				.consumeNextWith(ctx -> assertEquals(0, context.options().size()))
				.verifyComplete();
	}

	private ZoneDetailResponse getRegionDetails() {
		final var zoneAttribute = new ZoneAttributeResponse(
				"isShowcaseDeliveryAvailable",
				"",
				AttributeDataType.BOOLEAN,
				"",
				"true");
		final var zoneAttributes = List.of(zoneAttribute);
		return new ZoneDetailResponse(
				"S002",
				"Москва",
				ZoneBrand.MVIDEO,
				true,
				ZoneRole.REGULAR,
				new GeoPolygon(List.of(new GeoPoint(1.0, 1.0))),
				zoneAttributes);

	}

	private List<ExtendedProduct> getProducts() {
		final var productDto1 = new ProductDto();
		productDto1.setProductId("20037891");

		final var product1 = new ExtendedProduct();
		product1.setProduct(productDto1);
		product1.setQty(2);
		product1.setPrice(123.0);

		final var productDto2 = new ProductDto();
		productDto2.setProductId("50044074");

		final var product2 = new ExtendedProduct();
		product2.setProduct(productDto2);
		product2.setQty(3);
		product2.setPrice(456.0);

		return List.of(product1, product2);
	}

	private List<AvailabilityOption> getAvailabilityOptions() {
		AvailabilityOption option1 = new AvailabilityOption();
		option1.setHandoverObject("S11");
		option1.setStockObject("S11");
		option1.setMaterial("20037891");
		option1.setAvailableStock(5);
		option1.setShowCaseStock(0);
		option1.setAvailableDate(LocalDate.now());
		option1.setValidTo(LocalDateTime.now());

		AvailabilityOption option2 = new AvailabilityOption();
		option2.setHandoverObject("S22");
		option2.setStockObject("S22");
		option2.setMaterial("50044074");
		option2.setAvailableStock(3);
		option2.setShowCaseStock(0);
		option2.setAvailableDate(LocalDate.now());
		option2.setValidTo(LocalDateTime.now());

		AvailabilityOption option3 = new AvailabilityOption();
		option3.setHandoverObject("S11");
		option3.setStockObject("S11");
		option3.setMaterial("50044074");
		option3.setAvailableStock(5);
		option3.setShowCaseStock(0);
		option3.setAvailableDate(LocalDate.now());
		option3.setValidTo(LocalDateTime.now());

		AvailabilityOption option4 = new AvailabilityOption();
		option4.setHandoverObject("S22");
		option4.setStockObject("S22");
		option4.setMaterial("20037891");
		option4.setAvailableStock(3);
		option4.setShowCaseStock(0);
		option4.setAvailableDate(LocalDate.now());
		option4.setValidTo(LocalDateTime.now());

		AvailabilityOption option5 = new AvailabilityOption();
		option5.setHandoverObject("S22");
		option5.setStockObject("S22");
		option5.setMaterial("20037891");
		option5.setAvailableStock(3);
		option5.setShowCaseStock(0);
		option5.setAvailableDate(LocalDate.now().plusDays(1));
		option5.setValidTo(LocalDateTime.now().plusDays(1));

		return List.of(option1, option2, option3, option4, option5);
	}

	private List<HandoverObject> getHandoverObjects() {
		HandoverObject object1 = new HandoverObject();
		object1.setObjectId("S11");
		object1.setDeliveryStartTime(LocalTime.of(10, 0));
		object1.setDeliveryEndTime(LocalTime.of(22, 0));
		HandoverObject object2 = new HandoverObject();
		object2.setObjectId("S22");
		object2.setDeliveryStartTime(LocalTime.of(10, 0));
		object2.setDeliveryEndTime(LocalTime.of(22, 0));
		return List.of(object1, object2);
	}

	private OptionContext createOptionContextStocks() {
		OptionContext optionContext = new OptionContext();
		optionContext.setHandoverOption(HandoverOption.ETA_DELIVERY.getValue());
		optionContext.setHandoverObjects(getHandoverObjects());
		optionContext.setAvailabilityOptions(getAvailabilityOptions());
		return optionContext;
	}

	private List<AvailabilityOption> getAvailabilityOptionsStockShowcase() {
		AvailabilityOption option1 = new AvailabilityOption();
		option1.setHandoverObject("S11");
		option1.setStockObject("S11");
		option1.setMaterial("20037891");
		option1.setAvailableStock(2);
		option1.setShowCaseStock(2);
		option1.setAvailableDate(LocalDate.now());
		option1.setValidTo(LocalDateTime.now());

		AvailabilityOption option2 = new AvailabilityOption();
		option2.setHandoverObject("S22");
		option2.setStockObject("S22");
		option2.setMaterial("50044074");
		option2.setAvailableStock(3);
		option2.setShowCaseStock(0);
		option2.setAvailableDate(LocalDate.now());
		option2.setValidTo(LocalDateTime.now());

		AvailabilityOption option3 = new AvailabilityOption();
		option3.setHandoverObject("S11");
		option3.setStockObject("S11");
		option3.setMaterial("50044074");
		option3.setAvailableStock(2);
		option3.setShowCaseStock(2);
		option3.setAvailableDate(LocalDate.now());
		option3.setValidTo(LocalDateTime.now());

		AvailabilityOption option4 = new AvailabilityOption();
		option4.setHandoverObject("S22");
		option4.setStockObject("S22");
		option4.setMaterial("20037891");
		option4.setAvailableStock(3);
		option4.setShowCaseStock(0);
		option4.setAvailableDate(LocalDate.now());
		option4.setValidTo(LocalDateTime.now());

		return List.of(option1, option2, option3, option4);
	}

	private OptionContext createOptionContextStocksShowcase() {
		OptionContext optionContext = new OptionContext();
		optionContext.setHandoverOption(HandoverOption.ETA_DELIVERY.getValue());
		optionContext.setHandoverObjects(getHandoverObjects());
		optionContext.setAvailabilityOptions(getAvailabilityOptionsStockShowcase());
		return optionContext;
	}

	private OptionContext createOptionContextNoStocksNoShowcase() {
		OptionContext optionContext = new OptionContext();
		optionContext.setHandoverOption(HandoverOption.ETA_DELIVERY.getValue());
		optionContext.setHandoverObjects(getHandoverObjects());
		optionContext.setAvailabilityOptions(getAvailabilityOptionsNoStockNoShowcase());
		return optionContext;
	}

	private List<AvailabilityOption> getAvailabilityOptionsNoStockNoShowcase() {
		AvailabilityOption option1 = new AvailabilityOption();
		option1.setHandoverObject("S11");
		option1.setStockObject("S11");
		option1.setMaterial("20037891");
		option1.setAvailableStock(0);
		option1.setShowCaseStock(1);
		option1.setAvailableDate(LocalDate.now());
		option1.setValidTo(LocalDateTime.now());

		AvailabilityOption option2 = new AvailabilityOption();
		option2.setHandoverObject("S22");
		option2.setStockObject("S22");
		option2.setMaterial("50044074");
		option2.setAvailableStock(1);
		option2.setShowCaseStock(0);
		option2.setAvailableDate(LocalDate.now());
		option2.setValidTo(LocalDateTime.now());

		AvailabilityOption option3 = new AvailabilityOption();
		option3.setHandoverObject("S11");
		option3.setStockObject("S11");
		option3.setMaterial("50044074");
		option3.setAvailableStock(1);
		option3.setShowCaseStock(2);
		option3.setAvailableDate(LocalDate.now());
		option3.setValidTo(LocalDateTime.now());

		AvailabilityOption option4 = new AvailabilityOption();
		option4.setHandoverObject("S22");
		option4.setStockObject("S22");
		option4.setMaterial("20037891");
		option4.setAvailableStock(2);
		option4.setShowCaseStock(0);
		option4.setAvailableDate(LocalDate.now());
		option4.setValidTo(LocalDateTime.now());

		return List.of(option1, option2, option3, option4);
	}


	private DeliveryContext createContext(List<OptionContext> optionContexts) {
		Map<String, OptionContext> handoverOptionContext = new HashMap<>();
		for (OptionContext optionContext : optionContexts) {
			handoverOptionContext.put(optionContext.getHandoverOption(), optionContext);
		}

		final var context = DeliveryContext.builder()
				.requestHandoverOptions(Set.of(HandoverOption.ETA_DELIVERY.getValue()))
				.handoverOptionContext(handoverOptionContext)
				.build();
		context.products(getProducts())
				.regionDetails(getRegionDetails());
		return context;
	}
}