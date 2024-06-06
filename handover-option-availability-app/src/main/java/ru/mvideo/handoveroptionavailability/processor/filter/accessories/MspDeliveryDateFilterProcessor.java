package ru.mvideo.handoveroptionavailability.processor.filter.accessories;

import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;
import ru.mvideo.handoveroptionavailability.processor.BaseProcessor;
import ru.mvideo.handoveroptionavailability.processor.model.AccessoriesContext;

@Component
@RequiredArgsConstructor
public class MspDeliveryDateFilterProcessor extends BaseProcessor<AccessoriesContext> {

	@Override
	protected Mono<AccessoriesContext> executeProcessor(AccessoriesContext context) {
		return Mono.fromCallable(() -> {
			final var deliveryDate = context.deliveryDate();
			for (String option : context.options()) {
				final var optionContext = context.handoverOptionContext().get(option);
				final var availabilityOptions = optionContext.getAvailabilityOptions().stream()
						.filter(opt -> deliveryDate.equals(opt.getAvailableDate()))
						.collect(Collectors.toList());
				optionContext.setAvailabilityOptions(availabilityOptions);
			}
			return context;
		});
	}

	@Override
	public boolean shouldRun(AccessoriesContext context) {
		return true;
	}
}
