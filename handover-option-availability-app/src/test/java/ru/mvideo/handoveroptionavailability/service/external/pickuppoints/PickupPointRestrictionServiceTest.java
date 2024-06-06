package ru.mvideo.handoveroptionavailability.service.external.pickuppoints;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import org.mockito.ArgumentCaptor;

import static org.mockito.BDDMockito.given;

import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import reactor.core.publisher.Flux;
import reactor.test.StepVerifier;
import ru.mvideo.handoveroptionavailability.model.external.SapAttribute;
import ru.mvideo.handoveroptionavailability.model.external.msp.logistic.response.AvailabilityOption;
import ru.mvideo.handoveroptionavailability.processor.filter.fit.ExtendedProduct;
import ru.mvideo.lards.restriction.api.client.KnapsackPublicApi;
import ru.mvideo.lards.restriction.model.KnapsackBatchBriefRequest;
import ru.mvideo.product.model.ProductComplementMaterialDto;
import ru.mvideo.product.model.ProductDto;
import ru.mvideo.product.model.SapAttributesDto;

@ExtendWith(MockitoExtension.class)
class PickupPointRestrictionServiceTest {

	@Mock
	private KnapsackPublicApi client;

	@InjectMocks
	private PickupPointRestrictionService service;

	@DisplayName("Проверка передачи комплектных материалов в запросе")
	@Test
	void batchPickupPoints_complementMaterials_collapsedMaterialsInRequest() {
		var rimCode = "S002";
		var brand = "MVIDEO";
		List<AvailabilityOption> availabilityOptions = List.of(AvailabilityOption.builder()
				.handoverObject("A1")
				.stockObject("A1")
				.material("100")
				.availableStock(3)
				.showCaseStock(0)
				.validTo(LocalDateTime.now())
				.availableDate(LocalDate.now())
				.build());

		var sapAttributes1 = new SapAttributesDto()
				.constants(Map.of(
						SapAttribute.DIMENSION_MEASURE, "СМ",
						SapAttribute.LENGTH, "1",
						SapAttribute.WIDTH, "2",
						SapAttribute.HEIGHT, "3",
						SapAttribute.WEIGHT_MEASURE, "Г",
						SapAttribute.WEIGHT, "1"
				));
		var productDto1 = new ProductDto();
		var complementMaterials = List.of(
				new ProductComplementMaterialDto().materialNumber("2"),
				new ProductComplementMaterialDto().materialNumber("3")
		);
		productDto1.productId("1")
				.sapAttributes(sapAttributes1)
				.complementMaterials(complementMaterials);

		var extendedProduct1 = new ExtendedProduct();
		extendedProduct1.setProduct(productDto1);
		extendedProduct1.setQty(3);
		extendedProduct1.setPrice(100.0);

		var sapAttributes2 = new SapAttributesDto()
				.constants(Map.of(
						SapAttribute.DIMENSION_MEASURE, "СМ",
						SapAttribute.LENGTH, "4",
						SapAttribute.WIDTH, "5",
						SapAttribute.HEIGHT, "6",
						SapAttribute.WEIGHT_MEASURE, "Г",
						SapAttribute.WEIGHT, "2"
				));

		var productDto2 = new ProductDto();
		productDto2.productId("2")
				.sapAttributes(sapAttributes2);

		var extendedProduct2 = new ExtendedProduct();
		extendedProduct2.setProduct(productDto2);
		extendedProduct2.setQty(3);
		extendedProduct2.setPrice(100.0);

		var sapAttributes3 = new SapAttributesDto()
				.constants(Map.of(
						SapAttribute.DIMENSION_MEASURE, "СМ",
						SapAttribute.LENGTH, "7",
						SapAttribute.WIDTH, "8",
						SapAttribute.HEIGHT, "9",
						SapAttribute.WEIGHT_MEASURE, "Г",
						SapAttribute.WEIGHT, "3"
				));

		var productDto3 = new ProductDto();
		productDto3.productId("3")
				.sapAttributes(sapAttributes3);

		var extendedProduct3 = new ExtendedProduct();
		extendedProduct3.setProduct(productDto3);
		extendedProduct3.setQty(3);
		extendedProduct3.setPrice(100.0);

		var sapAttributes4 = new SapAttributesDto()
				.constants(Map.of(
						SapAttribute.DIMENSION_MEASURE, "СМ",
						SapAttribute.LENGTH, "10",
						SapAttribute.WIDTH, "11",
						SapAttribute.HEIGHT, "12",
						SapAttribute.WEIGHT_MEASURE, "Г",
						SapAttribute.WEIGHT, "4"
				));
		var productDto4 = new ProductDto();
		productDto4.productId("4")
				.sapAttributes(sapAttributes4);

		var extendedProduct4 = new ExtendedProduct();
		extendedProduct4.setProduct(productDto4);
		extendedProduct4.setQty(2);
		extendedProduct4.setPrice(400.0);

		var products = List.of(extendedProduct1, extendedProduct2, extendedProduct3, extendedProduct4);

		ArgumentCaptor<KnapsackBatchBriefRequest> requestCaptor = ArgumentCaptor.forClass(KnapsackBatchBriefRequest.class);
		given(client.getBatchBrief(requestCaptor.capture()))
				.willReturn(Flux.empty());

		StepVerifier.create(
						service.batchPickupPoints(
								rimCode,
								brand,
								availabilityOptions,
								products))
				.consumeNextWith(result -> assertTrue(result.isEmpty()))
				.verifyComplete();

		KnapsackBatchBriefRequest captorValue = requestCaptor.getValue();
		assertEquals(rimCode, captorValue.getRimCode());
		assertEquals(brand, captorValue.getBrand().getValue());
		assertNull(captorValue.getRadius());
		assertNull(captorValue.getQtyPickupPoints());
		assertNull(captorValue.getAddress());
		assertEquals(2, captorValue.getPositions().size());
		var firstMaterial = captorValue.getPositions().get(0);
		assertEquals(productDto1.getProductId(), firstMaterial.getMaterial());
		assertEquals(2, firstMaterial.getItems().size());
		var secondMaterial = captorValue.getPositions().get(1);
		assertEquals(productDto4.getProductId(), secondMaterial.getMaterial());
		assertEquals(0, secondMaterial.getItems().size());
	}
}