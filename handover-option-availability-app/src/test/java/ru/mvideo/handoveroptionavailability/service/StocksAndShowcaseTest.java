package ru.mvideo.handoveroptionavailability.service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Stream;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import ru.mvideo.handoveroptionavailability.model.external.msp.logistic.response.AvailabilityOption;
import ru.mvideo.handoveroptionavailability.processor.filter.fit.ExtendedProduct;
import ru.mvideo.handoveroptionavailability.processor.model.HandoverObject;
import ru.mvideo.product.model.ProductDto;

class StocksAndShowcaseTest {

	@ParameterizedTest(name = "{0}")
	@MethodSource("filteredAvailabilityOptions")
	void seamlessAvailabilityOptions_FilterByConditions_ReturnEmptyList(String description, AvailabilityOption option) {
		//GIVEN
		List<AvailabilityOption> availabilityOptions = List.of(option);

		var productDto = new ProductDto();
		productDto.setProductId("100");

		var extendedOrderContext = new ExtendedProduct();
		extendedOrderContext.setProduct(productDto);
		extendedOrderContext.setQty(2);
		List<ExtendedProduct> extendedOrderContexts = List.of(extendedOrderContext);

		var handoverObject = new HandoverObject();
		handoverObject.setObjectId("A1");
		List<HandoverObject> handoverObjects = List.of(handoverObject);

		// WHEN
		List<AvailabilityOption> result = StocksAndShowcase.seamlessAvailabilityOptions(
				availabilityOptions, extendedOrderContexts, handoverObjects);

		//THEN
		Assertions.assertTrue(result.isEmpty());
	}

	@DisplayName("Валидный AvailabilityOption")
	@Test
	void seamlessAvailabilityOptions_validAvailabilityOption_returnSameOption() {
		//GIVEN
		AvailabilityOption option = AvailabilityOption.builder()
				.handoverObject("A1")
				.stockObject("A1")
				.material("100")
				.availableStock(3)
				.showCaseStock(0)
				.validTo(LocalDateTime.now())
				.availableDate(LocalDate.now())
				.build();

		List<AvailabilityOption> availabilityOptions = List.of(option);

		var productDto = new ProductDto();
		productDto.setProductId("100");

		var extendedOrderContext = new ExtendedProduct();
		extendedOrderContext.setProduct(productDto);
		extendedOrderContext.setQty(2);
		List<ExtendedProduct> extendedOrderContexts = List.of(extendedOrderContext);

		var handoverObject = new HandoverObject();
		handoverObject.setObjectId("A1");
		List<HandoverObject> handoverObjects = List.of(handoverObject);

		// WHEN
		List<AvailabilityOption> result = StocksAndShowcase.seamlessAvailabilityOptions(
				availabilityOptions, extendedOrderContexts, handoverObjects);

		//THEN
		Assertions.assertEquals(1, result.size());
		Assertions.assertEquals(option, result.get(0));
	}

	private static Stream<Arguments> filteredAvailabilityOptions() {
		return Stream.of(
				Arguments.of(
						"Разные handoverObject и stockObject",
						AvailabilityOption.builder()
								.handoverObject("A1")
								.stockObject("B1")
								.material("100")
								.availableStock(3)
								.showCaseStock(0)
								.validTo(LocalDateTime.now())
								.availableDate(LocalDate.now())
								.build()),
				Arguments.of(
						"Отсутствует в списке запрашиваемых объектов",
						AvailabilityOption.builder()
								.handoverObject("A2")
								.stockObject("A2")
								.material("100")
								.availableStock(3)
								.showCaseStock(0)
								.validTo(LocalDateTime.now())
								.availableDate(LocalDate.now())
								.build()
				),
				Arguments.of(
						"В наличии меньше товаров чем требуется",
						AvailabilityOption.builder()
								.handoverObject("A1")
								.stockObject("A1")
								.material("100")
								.availableStock(1)
								.showCaseStock(0)
								.validTo(LocalDateTime.now())
								.availableDate(LocalDate.now())
								.build()
				),
				Arguments.of(
						"validTo больше текущей даты",
						AvailabilityOption.builder()
								.handoverObject("A1")
								.stockObject("A1")
								.material("100")
								.availableStock(3)
								.showCaseStock(0)
								.validTo(LocalDateTime.now().plusDays(2))
								.availableDate(LocalDate.now())
								.build()
				),
				Arguments.of(
						"availableDate больше текущей даты",
						AvailabilityOption.builder()
								.handoverObject("A1")
								.stockObject("A1")
								.material("100")
								.availableStock(3)
								.showCaseStock(0)
								.validTo(LocalDateTime.now())
								.availableDate(LocalDate.now().plusDays(2))
								.build()
				)
		);
	}
}
