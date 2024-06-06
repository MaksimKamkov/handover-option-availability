package ru.mvideo.handoveroptionavailability.processor.request;

import java.util.HashMap;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import reactor.core.publisher.Mono;
import ru.mvideo.handoveroptionavailability.model.HandoverOption;
import ru.mvideo.handoveroptionavailability.processor.model.Context;
import ru.mvideo.handoveroptionavailability.processor.model.OptionContext;

public interface ContextProducer<R, T extends Context> {

	Mono<T> produce(R request);

	default Set<String> options(List<HandoverOption> options) {
		return options.stream()
				.map(HandoverOption::getValue)
				.collect(Collectors.toSet());
	}

	default HashMap<String, OptionContext> optionContexts(Set<String> options) {
		final var handoverOptionContext = new HashMap<String, OptionContext>();
		for (String option : options) {
			final var optionContext = new OptionContext();
			optionContext.setHandoverOption(option);

			handoverOptionContext.put(option, optionContext);
		}
		return handoverOptionContext;
	}
}
