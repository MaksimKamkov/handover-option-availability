package ru.mvideo.handoveroptionavailability.processor;

import reactor.core.publisher.Mono;
import ru.mvideo.handoveroptionavailability.processor.model.Context;

public interface Processor<T extends Context> {

	boolean shouldRun(T context);

	Mono<T> process(T context);
}
