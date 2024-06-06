package ru.mvideo.handoveroptionavailability.utils;

import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Test;
import ru.mvideo.handoveroptionavailability.model.external.SapAttribute;
import ru.mvideo.handoveroptionavailability.processor.filter.fit.ExtendedProduct;
import ru.mvideo.lards.restriction.model.ComplementableMaterial;
import ru.mvideo.lards.restriction.model.DimensionMeasureUnit;
import ru.mvideo.lards.restriction.model.Material;
import ru.mvideo.lards.restriction.model.SingleMaterial;
import ru.mvideo.lards.restriction.model.WeightMeasureUnit;
import ru.mvideo.product.model.ProductComplementMaterialDto;
import ru.mvideo.product.model.ProductDto;
import ru.mvideo.product.model.SapAttributesDto;

class ProductConverterTest {

	@Test
	void toMaterial() {
		//GIVEN
		var sapAttributes = new SapAttributesDto()
				.constants(
						Map.of(
								SapAttribute.DIMENSION_MEASURE, "СМ",
								SapAttribute.LENGTH, "1",
								SapAttribute.WIDTH, "2",
								SapAttribute.HEIGHT, "3",
								SapAttribute.WEIGHT_MEASURE, "Г",
								SapAttribute.WEIGHT, "1"
						));
		var productDto = new ProductDto();
		productDto.productId("1")
				.sapAttributes(sapAttributes);

		var extendedProduct = new ExtendedProduct();
		extendedProduct.setProduct(productDto);
		extendedProduct.setQty(5);
		//WHEN
		Material actual = ProductConverter.toMaterial(extendedProduct);
		//THEN
		assertEquals(productDto.getProductId(), actual.getMaterial());
		assertEquals(5, actual.getQty());
		assertEquals(DimensionMeasureUnit.CM, actual.getDimensionsMeasure());
		assertEquals(1d, actual.getLength());
		assertEquals(2d, actual.getWidth());
		assertEquals(3d, actual.getHeight());
		assertEquals(WeightMeasureUnit.G, actual.getWeightMeasure());
		assertEquals(1d, actual.getWeight());
	}

	@Test
	void toComplementableMaterial_valid_returnComplementableMaterial() {
		//GIVEN
		var sapAttributes = new SapAttributesDto()
				.constants(
						Map.of(
								SapAttribute.DIMENSION_MEASURE, "СМ",
								SapAttribute.LENGTH, "1",
								SapAttribute.WIDTH, "2",
								SapAttribute.HEIGHT, "3",
								SapAttribute.WEIGHT_MEASURE, "Г",
								SapAttribute.WEIGHT, "1"
						));
		var productDto = new ProductDto();
		var complementMaterials = List.of(
				new ProductComplementMaterialDto().materialNumber("2"),
				new ProductComplementMaterialDto().materialNumber("3")
		);
		productDto.productId("1")
				.sapAttributes(sapAttributes)
				.complementMaterials(complementMaterials);

		var extendedProduct = new ExtendedProduct();
		extendedProduct.setProduct(productDto);
		//WHEN
		ComplementableMaterial actual = ProductConverter.toComplementableMaterial(extendedProduct);
		//THEN
		assertEquals(productDto.getProductId(), actual.getMaterial());
		assertEquals(DimensionMeasureUnit.CM, actual.getDimensionsMeasure());
		assertEquals(1d, actual.getLength());
		assertEquals(2d, actual.getWidth());
		assertEquals(3d, actual.getHeight());
		assertEquals(WeightMeasureUnit.G, actual.getWeightMeasure());
		assertEquals(1d, actual.getWeight());
	}

	@Test
	void toSingleMaterial_valid_returnSingleMaterial() {
		//GIVEN
		var sapAttributes = new SapAttributesDto()
				.constants(
						Map.of(
								SapAttribute.DIMENSION_MEASURE, "СМ",
								SapAttribute.LENGTH, "1",
								SapAttribute.WIDTH, "2",
								SapAttribute.HEIGHT, "3",
								SapAttribute.WEIGHT_MEASURE, "Г",
								SapAttribute.WEIGHT, "1"
						));
		var productDto = new ProductDto();
		productDto.productId("1")
				.sapAttributes(sapAttributes);

		var extendedProduct = new ExtendedProduct();
		extendedProduct.setProduct(productDto);
		//WHEN
		SingleMaterial actual = ProductConverter.toSingleMaterial(extendedProduct);
		//THEN
		assertEquals(productDto.getProductId(), actual.getMaterial());
		assertEquals(DimensionMeasureUnit.CM, actual.getDimensionsMeasure());
		assertEquals(1d, actual.getLength());
		assertEquals(2d, actual.getWidth());
		assertEquals(3d, actual.getHeight());
		assertEquals(WeightMeasureUnit.G, actual.getWeightMeasure());
		assertEquals(1d, actual.getWeight());
	}
}