package ru.mvideo.handoveroptionavailability.processor.filter;

import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;
import ru.mvideo.handoveroptionavailability.model.HandoverOption;
import ru.mvideo.handoveroptionavailability.processor.BaseProcessor;
import ru.mvideo.handoveroptionavailability.processor.model.Context;
import ru.mvideo.handoveroptionavailability.processor.model.HandoverObject;

@Component
@RequiredArgsConstructor
public class ObjectsInDeliveryRadiusProcessor<T extends Context> extends BaseProcessor<T> {

	private static final Set<String> ETA_AND_EXACTLY = Set.of(
			HandoverOption.ETA_DELIVERY.getValue(),
			HandoverOption.EXACTLY_TIME_DELIVERY.getValue()
	);

	@Override
	protected Mono<T> executeProcessor(T context) {
		return Mono.fromCallable(() -> {
			final var options = context.options().stream()
					.filter(ETA_AND_EXACTLY::contains)
					.collect(Collectors.toSet());
			for (String option : options) {
				final var optionContext = context.handoverOptionContext().get(option);
				final var maxDeliveryRadius = Optional.ofNullable(optionContext.getMinPriceRule().getMaxRadius()).orElse(0.0);

				final var objectsInDeliveryRadius = optionContext.getHandoverObjects().stream()
						.filter(object -> object.getDistance() <= maxDeliveryRadius)
						.collect(Collectors.toList());
				optionContext.setHandoverObjects(objectsInDeliveryRadius);

				final var objectsIdsInDeliveryRadius = objectsInDeliveryRadius.stream()
						.map(HandoverObject::getObjectId)
						.collect(Collectors.toSet());

				final var optionsInRadius = optionContext.getAvailabilityOptions().stream()
						.filter(opt -> objectsIdsInDeliveryRadius.contains(opt.getHandoverObject()))
						.collect(Collectors.toList());
				optionContext.setAvailabilityOptions(optionsInRadius);

				if (optionsInRadius.isEmpty()) {
					context.disableOption(option, "No shops in the radius");
				}
			}

			return context;
		});
	}

	@Override
	public boolean shouldRun(T context) {
		return context.hasOption(HandoverOption.ETA_DELIVERY.getValue()) || context.hasOption(HandoverOption.EXACTLY_TIME_DELIVERY.getValue());
	}

}
