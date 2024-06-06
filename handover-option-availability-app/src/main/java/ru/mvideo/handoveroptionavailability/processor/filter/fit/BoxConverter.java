package ru.mvideo.handoveroptionavailability.processor.filter.fit;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import lombok.experimental.UtilityClass;
import ru.mvideo.handoveroptionavailability.model.external.DimensionMeasureUnit;
import ru.mvideo.handoveroptionavailability.model.external.SapAttribute;
import ru.mvideo.handoveroptionavailability.utils.EnumUtils;
import ru.mvideo.lards.packing.algorithm.model.Box;
import ru.mvideo.product.model.ProductDto;

@UtilityClass
public class BoxConverter {

	public List<Box> toBoxes(ExtendedProduct context, DimensionMeasureUnit dimensionMeasureUnit) {
		return IntStream
				.range(0, context.getQty())
				.mapToObj(i -> toBox(context.getProduct(), dimensionMeasureUnit))
				.collect(Collectors.toList());
	}

	public Box toBox(ProductDto product, DimensionMeasureUnit dimensionMeasureUnit) {
		final var sapAttributes = product.getSapAttributes();

		final Map<String, String> sapConstants = sapAttributes.getConstants();

		final var measureString = sapConstants.get(SapAttribute.DIMENSION_MEASURE);
		final var measure = EnumUtils.getByValue(DimensionMeasureUnit.class, measureString).orElse(DimensionMeasureUnit.CM);

		final var length = Double.parseDouble(sapConstants.get(SapAttribute.LENGTH));
		final var width = Double.parseDouble(sapConstants.get(SapAttribute.WIDTH));
		final var height = Double.parseDouble(sapConstants.get(SapAttribute.HEIGHT));

		if (dimensionMeasureUnit.equals(DimensionMeasureUnit.CM)) {
			return new Box(measure.toCentimeters(length), measure.toCentimeters(width), measure.toCentimeters(height));
		} else {
			return new Box(measure.toMillimeters(length), measure.toMillimeters(width), measure.toMillimeters(height)
			);
		}
	}
}
