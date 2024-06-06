package ru.mvideo.handoveroptionavailability.processor.response.option;

import java.util.Optional;
import ru.mvideo.handoveroptionavailability.processor.model.Context;

public interface HandoverOptionService<T extends Context, P> {

	Optional<P> getOption(T context);
}
