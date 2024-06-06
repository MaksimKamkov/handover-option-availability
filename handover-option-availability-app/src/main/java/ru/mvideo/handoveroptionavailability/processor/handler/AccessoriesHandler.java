package ru.mvideo.handoveroptionavailability.processor.handler;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;
import ru.mvideo.handoveroptionavailability.model.AccessoriesItem;
import ru.mvideo.handoveroptionavailability.model.AccessoriesRequest;
import ru.mvideo.handoveroptionavailability.processor.flow.AccessoriesFlow;
import ru.mvideo.handoveroptionavailability.processor.model.AccessoriesContext;
import ru.mvideo.handoveroptionavailability.processor.request.AccessoriesContextProducer;
import ru.mvideo.handoveroptionavailability.processor.response.AccessoriesResponseProducer;

@RequiredArgsConstructor
@Component
public class AccessoriesHandler extends AbstractRequestFluxHandler<AccessoriesRequest, AccessoriesItem, AccessoriesContext> {

	private final AccessoriesContextProducer contextProducer;
	private final AccessoriesFlow flow;
	private final AccessoriesResponseProducer responseProducer;

	@Override
	protected Mono<AccessoriesContext> context(AccessoriesRequest request) {
		return contextProducer.produce(request)
				.publishOn(Schedulers.boundedElastic());
	}

	@Override
	protected Mono<AccessoriesContext> process(AccessoriesContext context) {
		return flow.process(context)
				.publishOn(Schedulers.boundedElastic());
	}

	@Override
	protected Flux<AccessoriesItem> response(AccessoriesContext context) {
		return responseProducer.produce(context)
				.publishOn(Schedulers.boundedElastic());
	}
}
