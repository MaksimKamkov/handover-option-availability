package ru.mvideo.handoveroptionavailability.processor.filter.payment.conditions;

import java.util.ArrayList;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import ru.mvideo.clop.cache.common.Cache;
import ru.mvideo.handoveroptionavailability.processor.BaseProcessor;
import ru.mvideo.handoveroptionavailability.processor.model.Context;
import ru.mvideo.lards.handover.option.model.HandoverOptionPaymentMethodResponse;

@Component
@RequiredArgsConstructor
public class MappingWithFilterPaymentMethodProcessor<T extends Context> extends BaseProcessor<T> {

	private final Cache<String, HandoverOptionPaymentMethodResponse> handoverOptionPaymentMethodsCache;

	@Override
	protected Mono<T> executeProcessor(T context) {
		return Flux.fromIterable(context.options())
				.flatMap(option -> handoverOptionPaymentMethodsCache.get(option)
						.doOnNext(response -> {
							if (response != null && response.getPaymentMethods() != null) {
								final var paymentMethod = context.paymentMethod();
								if (response.getPaymentMethods().stream()
										.anyMatch(method -> method.getName().getValue().equals(paymentMethod))) {
									List<String> methods = new ArrayList<>();
									methods.add(paymentMethod);
									context.handoverOptionContext().get(option).setPaymentConditions(methods);
								}
							}
						})
				).then(Mono.just(context));
	}

	@Override
	public boolean shouldRun(T context) {
		return context.paymentMethod() != null;
	}
}
