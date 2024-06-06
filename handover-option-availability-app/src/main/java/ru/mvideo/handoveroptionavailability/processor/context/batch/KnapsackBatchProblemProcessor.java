package ru.mvideo.handoveroptionavailability.processor.context.batch;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;
import ru.mvideo.handoveroptionavailability.model.HandoverOption;
import ru.mvideo.handoveroptionavailability.processor.BaseProcessor;
import ru.mvideo.handoveroptionavailability.processor.filter.fit.ExtendedProduct;
import ru.mvideo.handoveroptionavailability.processor.filter.fit.FitService;
import ru.mvideo.handoveroptionavailability.processor.model.BatchContext;
import ru.mvideo.lards.handover.option.model.ProviderZoneAttributesHandoverOptions;
import ru.mvideo.product.model.ProductComplementMaterialDto;

@RequiredArgsConstructor
@Component
public class KnapsackBatchProblemProcessor extends BaseProcessor<BatchContext> {

	private static final Set<String> HANDOVER_OPTION_KNAPSACK = Set.of(
			HandoverOption.ETA_DELIVERY.getValue(),
			HandoverOption.EXACTLY_TIME_DELIVERY.getValue(),
			HandoverOption.PICKUP_SEAMLESS.getValue()
	);

	private final FitService fitService;

	@Override
	protected Mono<BatchContext> executeProcessor(BatchContext context) {
		return Mono.fromCallable(() -> {

			final var knapsackOptions = context.options().stream()
					.filter(HANDOVER_OPTION_KNAPSACK::contains)
					.collect(Collectors.toSet());
			final var products = context.products();
			final var providers = context.providers();

			final var optionProviders = knapsackOptionProviders(providers, knapsackOptions);
			final var materialNumberProducts = materialNumberProducts(products);

			for (Map.Entry<String, Set<String>> material : context.materialHandoverOption().entrySet()) {
				final var options = material.getValue().stream()
						.filter(option -> {
							if (HANDOVER_OPTION_KNAPSACK.contains(option)) {
								return fitService.anyMatchProvider(optionProviders.get(option),
										materialNumberProducts.get(material.getKey()));
							}
							return true;
						}).collect(Collectors.toSet());
				material.setValue(options);
			}
			return context;
		});
	}

	@Override
	public boolean shouldRun(BatchContext context) {
		return context.hasOption(HandoverOption.EXACTLY_TIME_DELIVERY.getValue())
				|| context.hasOption(HandoverOption.ETA_DELIVERY.getValue())
				|| context.hasOption(HandoverOption.PICKUP_SEAMLESS.getValue());
	}

	private Map<String, List<ExtendedProduct>> materialNumberProducts(List<ExtendedProduct> products) {

		final Map<String, List<ExtendedProduct>> materialProduct = products.stream()
				.collect(Collectors.toMap(product -> product.getProduct().getProductId(), List::of));

		final Map<String, List<ExtendedProduct>> result = new HashMap<>();
		for (ExtendedProduct product : products) {
			if (product.getProduct().getComplementMaterials() == null) {
				result.put(product.getProduct().getProductId(), List.of(product));
			} else {
				final var complements = product.getProduct().getComplementMaterials().stream()
						.map(ProductComplementMaterialDto::getMaterialNumber)
						.collect(Collectors.toList());

				final List<ExtendedProduct> complementProducts = new ArrayList<>();
				for (String productId : complements) {
					complementProducts.addAll(materialProduct.get(productId));
				}
				result.put(product.getProduct().getProductId(), complementProducts);
			}
		}
		return result;
	}

	private Map<String, List<ProviderZoneAttributesHandoverOptions>> knapsackOptionProviders(List<ProviderZoneAttributesHandoverOptions> providers,
	                                                                                         Set<String> knapsackOptions) {

		final Map<String, List<ProviderZoneAttributesHandoverOptions>> optionProviders = new HashMap<>();

		for (ProviderZoneAttributesHandoverOptions provider : providers) {
			final List<ProviderZoneAttributesHandoverOptions> providerList = new ArrayList<>();
			providerList.add(provider);
			for (var opt : provider.getHandoverOptions()) {
				if (knapsackOptions.contains(opt.getName())) {
					optionProviders.merge(
							opt.getName(),
							providerList,
							(oldV, newV) -> {
								oldV.addAll(newV);
								return oldV;
							});
				}
			}
		}

		return optionProviders;
	}
}
