package ru.mvideo.handoveroptionavailability.utils;

import java.util.ArrayList;
import java.util.Map;
import lombok.Value;
import lombok.experimental.UtilityClass;
import ru.mvideo.handoveroptionavailability.model.external.SapAttribute;
import ru.mvideo.handoveroptionavailability.processor.filter.fit.ExtendedProduct;
import ru.mvideo.lards.restriction.model.ComplementableMaterial;
import ru.mvideo.lards.restriction.model.DimensionMeasureUnit;
import ru.mvideo.lards.restriction.model.Material;
import ru.mvideo.lards.restriction.model.SingleMaterial;
import ru.mvideo.lards.restriction.model.WeightMeasureUnit;
import ru.mvideo.product.model.ProductDto;

@UtilityClass
public class ProductConverter {

	public Material toMaterial(ExtendedProduct extendedProduct) {
		var context = new ProductContext(extendedProduct);
		return Material.builder()
				.qty(context.getQuantity())
				.material(context.getMaterial())
				.length(context.getLength())
				.height(context.getHeight())
				.width(context.getWidth())
				.dimensionsMeasure(context.getDimensionsMeasureUnit())
				.weight(context.getWeight())
				.weightMeasure(context.getWeightMeasureUnit())
				.build();
	}

	public ComplementableMaterial toComplementableMaterial(ExtendedProduct extendedProduct) {
		var context = new ProductContext(extendedProduct);
		return ComplementableMaterial.builder()
				.material(context.getMaterial())
				.length(context.getLength())
				.height(context.getHeight())
				.width(context.getWidth())
				.dimensionsMeasure(context.getDimensionsMeasureUnit())
				.weight(context.getWeight())
				.weightMeasure(context.getWeightMeasureUnit())
				.items(new ArrayList<>())
				.build();
	}

	public SingleMaterial toSingleMaterial(ExtendedProduct extendedProduct) {
		var context = new ProductContext(extendedProduct);
		return SingleMaterial.builder()
				.material(context.getMaterial())
				.length(context.getLength())
				.height(context.getHeight())
				.width(context.getWidth())
				.dimensionsMeasure(context.getDimensionsMeasureUnit())
				.weight(context.getWeight())
				.weightMeasure(context.getWeightMeasureUnit())
				.build();
	}


	@Value
	private static class ProductContext {

		String material;
		Integer quantity;
		Double length;
		Double height;
		Double width;
		DimensionMeasureUnit dimensionsMeasureUnit;
		Double weight;
		WeightMeasureUnit weightMeasureUnit;

		ProductContext(ExtendedProduct extendedProduct) {
			ProductDto product = extendedProduct.getProduct();

			final var sapAttributes = product.getSapAttributes();

			final Map<String, String> sapConstants = sapAttributes.getConstants();

			final var dimensionsMeasureString = sapConstants.get(SapAttribute.DIMENSION_MEASURE);
			final var dimensionsMeasure =
					EnumUtils.getByValue(
									ru.mvideo.handoveroptionavailability.model.external.DimensionMeasureUnit.class,
									dimensionsMeasureString
							)
							.orElse(ru.mvideo.handoveroptionavailability.model.external.DimensionMeasureUnit.CM);

			final var weightMeasureString = sapConstants.get(SapAttribute.WEIGHT_MEASURE);
			final var weightMeasure =
					EnumUtils.getByValue(
									ru.mvideo.handoveroptionavailability.model.external.WeightMeasureUnit.class,
									weightMeasureString
							)
							.orElse(ru.mvideo.handoveroptionavailability.model.external.WeightMeasureUnit.G);

			this.quantity = extendedProduct.getQty();
			this.material = product.getProductId();

			this.dimensionsMeasureUnit = DimensionMeasureUnit.fromValue(dimensionsMeasure.name());
			this.length = Double.parseDouble(sapConstants.get(SapAttribute.LENGTH));
			this.width = Double.parseDouble(sapConstants.get(SapAttribute.WIDTH));
			this.height = Double.parseDouble(sapConstants.get(SapAttribute.HEIGHT));

			this.weightMeasureUnit = WeightMeasureUnit.fromValue(weightMeasure.name());
			this.weight = Double.parseDouble(sapConstants.get(SapAttribute.WEIGHT));
		}
	}
}
