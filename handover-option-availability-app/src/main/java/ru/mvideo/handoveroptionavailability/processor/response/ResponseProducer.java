package ru.mvideo.handoveroptionavailability.processor.response;

import ru.mvideo.handoveroptionavailability.processor.model.Context;

public interface ResponseProducer<R, T extends Context> {

	R produce(T context);
}
