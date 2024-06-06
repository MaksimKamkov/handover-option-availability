package ru.mvideo.handoveroptionavailability.processor.handler;

import reactor.core.publisher.Flux;

public interface RequestFluxHandler<R, S> {
	Flux<S> handle(R request);
}
