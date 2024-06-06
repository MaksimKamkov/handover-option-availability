package ru.mvideo.handoveroptionavailability.processor.handler;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;
import ru.mvideo.handoveroptionavailability.model.PickupRequest;
import ru.mvideo.handoveroptionavailability.model.PickupResponseItem;
import ru.mvideo.handoveroptionavailability.processor.flow.PickupFlow;
import ru.mvideo.handoveroptionavailability.processor.model.BriefAndPickupContext;
import ru.mvideo.handoveroptionavailability.processor.request.PickupContextProducer;
import ru.mvideo.handoveroptionavailability.processor.response.PickupResponseProducer;

@RequiredArgsConstructor
@Component
public class PickupHandler extends AbstractRequestFluxHandler<PickupRequest, PickupResponseItem, BriefAndPickupContext> {

	private final PickupContextProducer contextProducer;
	private final PickupFlow flow;
	private final PickupResponseProducer responseProducer;

	@Override
	protected Mono<BriefAndPickupContext> context(PickupRequest request) {
		return contextProducer.produce(request)
				.publishOn(Schedulers.boundedElastic());
	}

	@Override
	protected Mono<BriefAndPickupContext> process(BriefAndPickupContext context) {
		return flow.process(context)
				.publishOn(Schedulers.boundedElastic());
	}

	@Override
	protected Flux<PickupResponseItem> response(BriefAndPickupContext context) {
		return responseProducer.produce(context)
				.publishOn(Schedulers.boundedElastic());
	}
}
