package ru.mvideo.handoveroptionavailability.processor.handler;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import ru.mvideo.handoveroptionavailability.processor.model.Context;

public abstract class AbstractRequestFluxHandler<R, S, T extends Context> implements RequestFluxHandler<R, S> {

	protected abstract Mono<T> context(R request);

	protected abstract Mono<T> process(T context);

	protected abstract Flux<S> response(T context);

	@Override
	public final Flux<S> handle(R request) {
		return context(request)
				.flatMap(this::process)
				.flatMapMany(this::response);
	}
}
