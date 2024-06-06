package ru.mvideo.handoveroptionavailability.processor.filter.fit;

import java.util.List;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import ru.mvideo.handoveroptionavailability.model.external.DimensionMeasureUnit;
import ru.mvideo.handoveroptionavailability.model.external.WeightMeasureUnit;
import ru.mvideo.handoveroptionavailability.utils.WeightCalculator;
import ru.mvideo.lards.handover.option.model.ProviderZoneAttributesHandoverOptions;
import ru.mvideo.lards.packing.algorithm.model.Box;
import ru.mvideo.lards.packing.algorithm.service.ContainerPackingService;

@RequiredArgsConstructor
@Component
public class FitServiceImpl implements FitService {

	private final ContainerPackingService packingService;

	@Override
	public boolean anyMatchProvider(List<ProviderZoneAttributesHandoverOptions> providers,
	                                List<ExtendedProduct> products) {

		final var boxes =
				products.stream()
						.flatMap(product -> BoxConverter.toBoxes(product, DimensionMeasureUnit.CM).stream())
						.collect(Collectors.toList());

		final double weight =
				products.stream()
						.map(product -> WeightCalculator.toWeight(product.getProduct(), product.getQty(), WeightMeasureUnit.KG))
						.mapToDouble(Double::doubleValue)
						.sum();

		return providers.stream().anyMatch(provider -> {
			final var container = ContainerConverter.toContainer(provider);
			return weight <= container.getWeight() && packingService.isFit(container, boxes);
		});
	}

	@Override
	public List<ProviderZoneAttributesHandoverOptions> filterProviders(List<ProviderZoneAttributesHandoverOptions> providers,
	                                                                   List<ExtendedProduct> products) {
		final List<Box> orderBoxes =
				products.stream()
						.flatMap(product -> BoxConverter.toBoxes(product, DimensionMeasureUnit.CM).stream())
						.collect(Collectors.toList());

		final double orderWeight =
				products.stream()
						.map(product -> WeightCalculator.toWeight(product.getProduct(), product.getQty(), WeightMeasureUnit.KG))
						.mapToDouble(Double::doubleValue)
						.sum();

		return providers.stream()
				.filter(provider -> {
					final var container = ContainerConverter.toContainer(provider);
					return orderWeight <= container.getWeight() && packingService.isFit(container, orderBoxes);
				})
				.collect(Collectors.toList());
	}
}
