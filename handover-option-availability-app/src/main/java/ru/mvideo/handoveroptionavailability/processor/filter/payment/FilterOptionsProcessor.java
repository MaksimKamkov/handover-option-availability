package ru.mvideo.handoveroptionavailability.processor.filter.payment;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;
import ru.mvideo.handoveroptionavailability.processor.BaseProcessor;
import ru.mvideo.handoveroptionavailability.processor.model.Context;
import ru.mvideo.handoveroptionavailability.processor.model.Flags;

@Component
@RequiredArgsConstructor
public class FilterOptionsProcessor<T extends Context> extends BaseProcessor<T> {

	@Override
	protected Mono<T> executeProcessor(T context) {
		return Mono.fromCallable(() -> {
			for (String option : context.options()) {
				final var optionContext = context.handoverOptionContext().get(option);
				if (optionContext.getPaymentConditions() == null) {
					context.disableOption(option, "No payment conditions");
				}
			}
			return context;
		});
	}

	@Override
	public boolean shouldRun(T context) {
		return context.flags().contains(Flags.RETURN_PAYMENT_CONDITIONS) || context.paymentMethod() != null;
	}
}
