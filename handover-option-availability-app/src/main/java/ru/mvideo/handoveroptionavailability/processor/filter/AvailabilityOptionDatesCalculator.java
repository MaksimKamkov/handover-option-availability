package ru.mvideo.handoveroptionavailability.processor.filter;

import java.util.Set;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;
import ru.mvideo.handoveroptionavailability.model.HandoverOption;
import ru.mvideo.handoveroptionavailability.processor.BaseProcessor;
import ru.mvideo.handoveroptionavailability.processor.model.Context;
import ru.mvideo.handoveroptionavailability.processor.utils.DateCalculator;

@Component
@RequiredArgsConstructor
public class AvailabilityOptionDatesCalculator<T extends Context> extends BaseProcessor<T> {

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
				if (HANDOVER_OPTION_WITH_SPLIT_BASKET.contains(option)) {
					final var optionContext = context.handoverOptionContext().get(option);
					final var availabilityOptions =
							DateCalculator.enrichAvailabilityOptionDates(optionContext.getAvailabilityOptions());
					optionContext.setAvailabilityOptions(availabilityOptions);
				}
			}
			return context;
		});
	}

	@Override
	public boolean shouldRun(T context) {
		return context.hasOption(HandoverOption.INTERVAL_DELIVERY.getValue())
				|| context.hasOption(HandoverOption.DPD_DELIVERY.getValue())
				|| context.hasOption(HandoverOption.ELECTRONIC_DELIVERY.getValue())
				|| context.hasOption(HandoverOption.PICKUP.getValue());
	}
}

