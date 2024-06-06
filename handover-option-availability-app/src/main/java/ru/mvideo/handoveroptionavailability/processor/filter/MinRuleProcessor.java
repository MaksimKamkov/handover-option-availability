package ru.mvideo.handoveroptionavailability.processor.filter;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import ru.mvideo.handoveroptionavailability.processor.BaseProcessor;
import ru.mvideo.handoveroptionavailability.processor.model.Context;
import ru.mvideo.lards.price.rules.model.MinPriceResponse;

@Component
@RequiredArgsConstructor
public class MinRuleProcessor<T extends Context> extends BaseProcessor<T> {

	@Override
	protected Mono<T> executeProcessor(T context) {
		return Flux.fromIterable(context.options())
				.zipWith(Flux.fromIterable(context.minPriceRules())
						.collectMap(MinPriceResponse::getHandoverOptionName)
						.cache()
						.repeat())
				.doOnNext(t2 -> {
					var minRule = t2.getT2().get(t2.getT1());
					if (minRule == null) {
						context.disableOption(t2.getT1(), "Price rule not found");
					} else {
						var optionContext = context.handoverOptionContext().get(t2.getT1());
						optionContext.setMinPriceRule(minRule);
					}
				})
				.then(Mono.just(context));
	}

	@Override
	public boolean shouldRun(T context) {
		return true;
	}
}
