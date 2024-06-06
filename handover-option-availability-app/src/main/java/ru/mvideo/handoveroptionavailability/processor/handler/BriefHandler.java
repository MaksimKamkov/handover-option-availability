package ru.mvideo.handoveroptionavailability.processor.handler;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;
import ru.mvideo.handoveroptionavailability.model.BriefOptions;
import ru.mvideo.handoveroptionavailability.model.BriefRequest;
import ru.mvideo.handoveroptionavailability.processor.flow.BriefFlow;
import ru.mvideo.handoveroptionavailability.processor.model.BriefAndPickupContext;
import ru.mvideo.handoveroptionavailability.processor.request.BriefContextProducer;
import ru.mvideo.handoveroptionavailability.processor.response.BriefResponseProducer;

@RequiredArgsConstructor
@Component
public class BriefHandler extends AbstractRequestFluxHandler<BriefRequest, BriefOptions, BriefAndPickupContext> {

	private final BriefContextProducer contextProducer;
	private final BriefFlow flow;
	private final BriefResponseProducer responseProducer;

	@Override
	protected Mono<BriefAndPickupContext> context(BriefRequest request) {
		return contextProducer.produce(request)
				.publishOn(Schedulers.boundedElastic());
	}

	@Override
	protected Mono<BriefAndPickupContext> process(BriefAndPickupContext context) {
		return flow.process(context)
				.publishOn(Schedulers.boundedElastic());
	}

	@Override
	protected Flux<BriefOptions> response(BriefAndPickupContext context) {
		return responseProducer.produce(context)
				.publishOn(Schedulers.parallel());
	}
}
