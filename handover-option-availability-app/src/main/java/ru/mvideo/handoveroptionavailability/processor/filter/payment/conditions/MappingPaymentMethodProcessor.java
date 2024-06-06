package ru.mvideo.handoveroptionavailability.processor.filter.payment.conditions;

import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import ru.mvideo.clop.cache.common.Cache;
import ru.mvideo.handoveroptionavailability.processor.BaseProcessor;
import ru.mvideo.handoveroptionavailability.processor.model.Context;
import ru.mvideo.handoveroptionavailability.processor.model.Flags;
import ru.mvideo.lards.handover.option.model.HandoverOptionPaymentMethodResponse;

@Component
@RequiredArgsConstructor
public class MappingPaymentMethodProcessor<T extends Context> extends BaseProcessor<T> {

	private final Cache<String, HandoverOptionPaymentMethodResponse> handoverOptionPaymentMethodsCache;

	@Override
	protected Mono<T> executeProcessor(T context) {
		return Flux.fromIterable(context.options())
				.flatMap(option -> handoverOptionPaymentMethodsCache.get(option)
						.doOnNext(response -> {
							if (response != null && response.getPaymentMethods() != null) {
								final var paymentMethods = response.getPaymentMethods().stream()
										.map(method -> method.getName().getValue())
										.collect(Collectors.toList());
								context.handoverOptionContext().get(option).setPaymentConditions(paymentMethods);
							}
						})
				).then(Mono.just(context));
	}

	@Override
	public boolean shouldRun(T context) {
		return context.flags().contains(Flags.RETURN_PAYMENT_CONDITIONS) && context.paymentMethod() == null;
	}
}
