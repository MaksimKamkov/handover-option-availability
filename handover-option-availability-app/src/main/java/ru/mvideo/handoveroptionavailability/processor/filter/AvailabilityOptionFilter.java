package ru.mvideo.handoveroptionavailability.processor.filter;

import java.util.Collection;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;
import ru.mvideo.handoveroptionavailability.model.HandoverOption;
import ru.mvideo.handoveroptionavailability.model.external.msp.logistic.HandoverType;
import ru.mvideo.handoveroptionavailability.processor.BaseProcessor;
import ru.mvideo.handoveroptionavailability.processor.model.Context;
import ru.mvideo.lards.handover.option.model.ProviderZoneAttributesHandoverOptions;

@Component
@RequiredArgsConstructor
public class AvailabilityOptionFilter<T extends Context> extends BaseProcessor<T> {

	private static final Map<String, HandoverType> HANDOVER_TYPE_MAP = Map.of(
			HandoverOption.ETA_DELIVERY.getValue(), HandoverType.PICKUP,
			HandoverOption.EXACTLY_TIME_DELIVERY.getValue(), HandoverType.PICKUP,
			HandoverOption.DPD_DELIVERY.getValue(), HandoverType.POSTAL,
			HandoverOption.INTERVAL_DELIVERY.getValue(), HandoverType.COURIER,
			HandoverOption.ELECTRONIC_DELIVERY.getValue(), HandoverType.ELECTRONIC,
			HandoverOption.PICKUP.getValue(), HandoverType.PICKUP,
			HandoverOption.PICKUP_PARTNER.getValue(), HandoverType.PICKUP,
			HandoverOption.PICKUP_SEAMLESS.getValue(), HandoverType.PICKUP
	);

	private static final Set<String> HANDOVER_OPTION_WITH_SPLIT_BASKET = Set.of(
			HandoverOption.INTERVAL_DELIVERY.getValue(),
			HandoverOption.DPD_DELIVERY.getValue(),
			HandoverOption.ELECTRONIC_DELIVERY.getValue(),
			HandoverOption.PICKUP.getValue()
	);

	@Override
	protected Mono<T> executeProcessor(T context) {
		return Mono.fromCallable(() -> {
			for (String option : context.options()) {
				final var optionContext = context.handoverOptionContext().get(option);
				final var handoverType = HANDOVER_TYPE_MAP.get(option);

				optionContext.setHandoverOptionMaterial(getHandoverObjectMaterial(context, option));

				var availabilityOptions = context.availabilityOptions().stream()
						.filter(availabilityOption -> handoverType.equals(availabilityOption.getHandoverType()))
						.collect(Collectors.toList());

				if (HANDOVER_OPTION_WITH_SPLIT_BASKET.contains(option)) {
					final var productIds = optionContext.getProducts().stream()
							.map(product -> product.getProduct().getProductId())
							.collect(Collectors.toList());
					availabilityOptions = availabilityOptions.stream()
							.filter(availabilityOption -> productIds.contains(availabilityOption.getMaterial()))
							.collect(Collectors.toList());
				}

				if (availabilityOptions.isEmpty()) {
					context.disableOption(option, "Not found materials for delivery option");
				} else {
					optionContext.setAvailabilityOptions(availabilityOptions);
				}
			}
			return context;
		});
	}

	private String getHandoverObjectMaterial(T context, String optionName) {
		final var providers = context.providers();
		return providers.stream()
				.map(ProviderZoneAttributesHandoverOptions::getHandoverOptions)
				.flatMap(Collection::stream)
				.filter(handoverOption -> optionName.equals(handoverOption.getName()))
				.map(ru.mvideo.lards.handover.option.model.HandoverOption::getMaterialNumber)
				.filter(Objects::nonNull)
				.findFirst().orElse(null);
	}

	@Override
	public boolean shouldRun(T context) {
		return true;
	}
}
