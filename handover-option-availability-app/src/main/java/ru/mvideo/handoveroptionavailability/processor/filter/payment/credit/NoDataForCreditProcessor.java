package ru.mvideo.handoveroptionavailability.processor.filter.payment.credit;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;
import ru.mvideo.handoveroptionavailability.processor.BaseProcessor;
import ru.mvideo.handoveroptionavailability.processor.model.Context;

@Component
@RequiredArgsConstructor
public class NoDataForCreditProcessor<T extends Context> extends BaseProcessor<T> {

	@Override
	protected Mono<T> executeProcessor(T context) {
		return Mono.empty();
	}

	@Override
	public boolean shouldRun(T context) {
		return "CREDIT".equals(context.paymentMethod()) && context.minStock() == null && context.creditApprovalLeadTime() == null;
	}
}
