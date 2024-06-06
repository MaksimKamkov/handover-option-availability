package ru.mvideo.handoveroptionavailability.processor.flow;

import reactor.core.publisher.Mono;
import ru.mvideo.handoveroptionavailability.processor.model.Context;

public interface Flow<T extends Context> {

	Mono<T> process(T context);
}
