package ru.mvideo.handoveroptionavailability.processor.handler;

import reactor.core.publisher.Mono;

public interface RequestMonoHandler<R, S> {
	Mono<S> handle(R request);
}
