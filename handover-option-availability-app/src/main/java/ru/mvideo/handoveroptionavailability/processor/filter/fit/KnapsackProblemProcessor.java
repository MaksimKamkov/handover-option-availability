package ru.mvideo.handoveroptionavailability.processor.filter.fit;

import java.util.Set;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;
import ru.mvideo.handoveroptionavailability.model.HandoverOption;
import ru.mvideo.handoveroptionavailability.processor.BaseProcessor;
import ru.mvideo.handoveroptionavailability.processor.model.Context;
import ru.mvideo.handoveroptionavailability.processor.utils.ProviderUtil;

@RequiredArgsConstructor
@Component
public class KnapsackProblemProcessor<T extends Context> extends BaseProcessor<T> {

	private static final Set<String> HANDOVER_OPTION_KNAPSACK = Set.of(
			HandoverOption.ETA_DELIVERY.getValue(),
			HandoverOption.EXACTLY_TIME_DELIVERY.getValue(),
			HandoverOption.PICKUP_SEAMLESS.getValue()
	);

	private final FitService fitService;

	@Override
	protected Mono<T> executeProcessor(T context) {
		return Mono.fromCallable(() -> {

			final var products = context.products().stream()
					.filter(product -> product.getProduct().getComplementMaterials() == null)
					.collect(Collectors.toList());

			final var knapsackOptions = context.options().stream()
					.filter(HANDOVER_OPTION_KNAPSACK::contains)
					.collect(Collectors.toSet());

			final var knapsackProviders = context.providers().stream()
					.filter(provider -> ProviderUtil.containsHandoverOption(provider, knapsackOptions))
					.collect(Collectors.toList());

			final var otherProviders = context.providers().stream()
					.filter(provider -> !ProviderUtil.containsHandoverOption(provider, knapsackOptions))
					.collect(Collectors.toList());

			final var providers = fitService.filterProviders(knapsackProviders, products);

			if (providers.isEmpty()) {
				knapsackOptions.forEach(s -> context.disableOption(s, "Products did not fit into any provider"));
				context.providers(otherProviders);
			} else {
				otherProviders.addAll(providers);
				context.providers(otherProviders);
				if (!ProviderUtil.containsHandoverOption(providers, HandoverOption.ETA_DELIVERY.getValue())) {
					context.disableOption(HandoverOption.ETA_DELIVERY.getValue(), "Providers with eta-delivery not found");
				}
				if (!ProviderUtil.containsHandoverOption(providers, HandoverOption.EXACTLY_TIME_DELIVERY.getValue())) {
					context.disableOption(HandoverOption.EXACTLY_TIME_DELIVERY.getValue(), "Providers with exactly-delivery not found");
				}
				if (!ProviderUtil.containsHandoverOption(providers, HandoverOption.PICKUP_SEAMLESS.getValue())) {
					context.disableOption(HandoverOption.PICKUP_SEAMLESS.getValue(), "Providers with seamless-delivery not found");
				}
			}
			return context;
		});
	}

	@Override
	public boolean shouldRun(T context) {
		return context.hasOption(HandoverOption.EXACTLY_TIME_DELIVERY.getValue())
				|| context.hasOption(HandoverOption.ETA_DELIVERY.getValue())
				|| context.hasOption(HandoverOption.PICKUP_SEAMLESS.getValue());
	}
}
