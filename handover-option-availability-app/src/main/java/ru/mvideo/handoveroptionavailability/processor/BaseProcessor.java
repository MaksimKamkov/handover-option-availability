package ru.mvideo.handoveroptionavailability.processor;

import reactor.core.publisher.Mono;
import ru.mvideo.handoveroptionavailability.processor.model.Context;

public abstract class BaseProcessor<T extends Context> implements Processor<T> {

	protected abstract Mono<T> executeProcessor(T context);

	public final Mono<T> process(T context) {
		if (context.options().isEmpty()) {
			return Mono.empty();
		}
		if (shouldRun(context)) {
			return executeProcessor(context);
		} else {
			return Mono.just(context);
		}
	}

}
