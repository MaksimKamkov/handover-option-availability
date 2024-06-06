package ru.mvideo.handoveroptionavailability.processor.filter.fit;

import java.util.Map;
import java.util.stream.Collectors;
import lombok.experimental.UtilityClass;
import ru.mvideo.handoveroptionavailability.model.external.HandoverOptionDetailAttribute;
import ru.mvideo.io.pickup.points.lib.model.response.CellLimit;
import ru.mvideo.lards.handover.option.model.ProviderAttribute;
import ru.mvideo.lards.handover.option.model.ProviderZoneAttributesHandoverOptions;

@UtilityClass
public class ContainerConverter {

	public ExtendedContainer toContainer(ProviderZoneAttributesHandoverOptions provider) {
		final Map<String, String> attributeMap = provider.getProviderAttributes().stream()
				.collect(Collectors.toMap(
						ProviderAttribute::getName,
						ProviderAttribute::getValue));

		final var length = Double.parseDouble(attributeMap.get(HandoverOptionDetailAttribute.LENGTH));
		final var width = Double.parseDouble(attributeMap.get(HandoverOptionDetailAttribute.WIDTH));
		final var height = Double.parseDouble(attributeMap.get(HandoverOptionDetailAttribute.HEIGHT));
		final var weight = Double.parseDouble(attributeMap.get(HandoverOptionDetailAttribute.WEIGHT));

		return new ExtendedContainer(String.valueOf(provider.getProviderName()), weight, length, width, height);
	}

	public ExtendedContainer toContainer(CellLimit cellLimit) {
		return new ExtendedContainer(
				cellLimit.getCellLimitId(),
				cellLimit.getMaxWeightGram(),
				cellLimit.getMaxWidthMillimetre(),
				cellLimit.getMaxDepthMillimetre(),
				cellLimit.getMaxHeightMillimetre()
		);
	}
}
