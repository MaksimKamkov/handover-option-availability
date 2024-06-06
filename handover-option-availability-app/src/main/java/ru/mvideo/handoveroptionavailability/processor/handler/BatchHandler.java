package ru.mvideo.handoveroptionavailability.processor.handler;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;
import ru.mvideo.handoveroptionavailability.model.BatchRequest;
import ru.mvideo.handoveroptionavailability.model.MaterialHandoverOptions;
import ru.mvideo.handoveroptionavailability.processor.flow.BatchFlow;
import ru.mvideo.handoveroptionavailability.processor.model.BatchContext;
import ru.mvideo.handoveroptionavailability.processor.request.BatchContextProducer;
import ru.mvideo.handoveroptionavailability.processor.response.BatchResponseProducer;

@RequiredArgsConstructor
@Component
public class BatchHandler extends AbstractRequestFluxHandler<BatchRequest, MaterialHandoverOptions, BatchContext> {

	private final BatchContextProducer contextProducer;
	private final BatchFlow flow;
	private final BatchResponseProducer responseProducer;

	@Override
	protected Mono<BatchContext> context(BatchRequest request) {
		return contextProducer.produce(request)
				.publishOn(Schedulers.boundedElastic());
	}

	@Override
	protected Mono<BatchContext> process(BatchContext context) {
		return flow.process(context)
				.publishOn(Schedulers.boundedElastic());
	}

	@Override
	protected Flux<MaterialHandoverOptions> response(BatchContext context) {
		return responseProducer.produce(context)
				.publishOn(Schedulers.boundedElastic());
	}
}
