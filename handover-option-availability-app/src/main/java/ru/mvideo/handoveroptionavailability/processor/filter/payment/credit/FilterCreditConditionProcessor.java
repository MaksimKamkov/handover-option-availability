package ru.mvideo.handoveroptionavailability.processor.filter.payment.credit;

import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;
import ru.mvideo.handoveroptionavailability.processor.BaseProcessor;
import ru.mvideo.handoveroptionavailability.processor.model.Context;
import ru.mvideo.handoveroptionavailability.processor.model.Flags;

@Component
@RequiredArgsConstructor
public class FilterCreditConditionProcessor<T extends Context> extends BaseProcessor<T> {

	@Override
	protected Mono<T> executeProcessor(T context) {
		return Mono.fromCallable(() -> {
			for (String option : context.options()) {
				final var optionContext = context.handoverOptionContext().get(option);
				final var notCreditConditions = optionContext.getPaymentConditions().stream()
						.filter(condition -> !"CREDIT".equals(condition))
						.collect(Collectors.toList());
				optionContext.setPaymentConditions(notCreditConditions);
			}
			return context;
		});
	}

	@Override
	public boolean shouldRun(T context) {
		return context.flags().contains(Flags.RETURN_PAYMENT_CONDITIONS)
				&& context.paymentMethod() == null
				&& context.minStock() == null
				&& context.creditApprovalLeadTime() == null;
	}
}
