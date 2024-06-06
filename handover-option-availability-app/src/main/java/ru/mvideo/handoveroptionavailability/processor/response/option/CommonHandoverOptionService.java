package ru.mvideo.handoveroptionavailability.processor.response.option;

import java.util.Optional;
import ru.mvideo.handoveroptionavailability.processor.model.Context;

public abstract class CommonHandoverOptionService<T extends Context, P> implements HandoverOptionService<T, P> {

	protected abstract boolean support(T context);

	protected abstract P prepareResponse(T context);

	@Override
	public final Optional<P> getOption(T context) {
		if (support(context)) {
			return Optional.ofNullable(prepareResponse(context));
		} else {
			return Optional.empty();
		}
	}
}
