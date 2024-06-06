package ru.mvideo.handoveroptionavailability.utils;

import java.util.Map;
import lombok.experimental.UtilityClass;
import ru.mvideo.handoveroptionavailability.model.external.SapAttribute;
import ru.mvideo.handoveroptionavailability.model.external.WeightMeasureUnit;
import ru.mvideo.product.model.ProductDto;
import ru.mvideo.product.model.SapAttributesDto;

@UtilityClass
public class WeightCalculator {

	public double toWeight(SapAttributesDto attributes, Integer qty, WeightMeasureUnit weightMeasureUnit) {
		if (attributes == null || attributes.getConstants() == null) {
			throw new IllegalArgumentException(String.format("Missed attributes: %s, %s", SapAttribute.WEIGHT_MEASURE, SapAttribute.WEIGHT));
		}
		final Map<String, String> sapConstants = attributes.getConstants();

		final String measureString = sapConstants.get(SapAttribute.WEIGHT_MEASURE);
		final WeightMeasureUnit measure = EnumUtils.getByValue(WeightMeasureUnit.class, measureString)
				.orElse(WeightMeasureUnit.KG);

		final double weight = Double.parseDouble(sapConstants.get(SapAttribute.WEIGHT));
		if (WeightMeasureUnit.G.equals(weightMeasureUnit)) {
			return measure.toGrams(weight) * qty;
		} else {
			return measure.toKilograms(weight) * qty;
		}
	}

	public double toWeight(ProductDto product, Integer qty, WeightMeasureUnit weightMeasureUnit) {
		final var sapAttributes = product.getSapAttributes();
		final var sapConstants = sapAttributes.getConstants();

		final var measureString = sapConstants.get(SapAttribute.WEIGHT_MEASURE);
		final WeightMeasureUnit measure = EnumUtils.getByValue(WeightMeasureUnit.class, measureString).orElse(WeightMeasureUnit.KG);

		final var weight = Double.parseDouble(sapConstants.get(SapAttribute.WEIGHT));
		if (WeightMeasureUnit.G.equals(weightMeasureUnit)) {
			return measure.toGrams(weight) * qty;
		} else {
			return measure.toKilograms(weight) * qty;
		}
	}
}
