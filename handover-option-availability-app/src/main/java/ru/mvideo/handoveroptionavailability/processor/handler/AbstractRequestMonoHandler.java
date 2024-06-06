package ru.mvideo.handoveroptionavailability.processor.handler;

import reactor.core.publisher.Mono;
import ru.mvideo.handoveroptionavailability.processor.model.Context;

public abstract class AbstractRequestMonoHandler<R, S, T extends Context> implements RequestMonoHandler<R, S> {

	protected abstract Mono<T> context(R request);

	protected abstract Mono<T> process(T context);

	protected abstract Mono<S> response(T context);

	@Override
	public final Mono<S> handle(R request) {
		return context(request)
				.flatMap(this::process)
				.flatMap(this::response);
	}
}
