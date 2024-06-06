package ru.mvideo.handoveroptionavailability.processor.handler;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;
import ru.mvideo.handoveroptionavailability.model.DeliveryRequestV2;
import ru.mvideo.handoveroptionavailability.model.DeliveryResponse;
import ru.mvideo.handoveroptionavailability.processor.flow.DeliveryFlow;
import ru.mvideo.handoveroptionavailability.processor.model.DeliveryContext;
import ru.mvideo.handoveroptionavailability.processor.request.DeliveryContextProducer;
import ru.mvideo.handoveroptionavailability.processor.response.DeliveryResponseProducer;

@RequiredArgsConstructor
@Component
public class DeliveryHandler extends AbstractRequestFluxHandler<DeliveryRequestV2, DeliveryResponse, DeliveryContext> {

	private final DeliveryContextProducer contextProducer;
	private final DeliveryFlow flow;
	private final DeliveryResponseProducer responseProducer;

	@Override
	protected Mono<DeliveryContext> context(DeliveryRequestV2 request) {
		return contextProducer.produce(request)
				.publishOn(Schedulers.boundedElastic());
	}

	@Override
	protected Mono<DeliveryContext> process(DeliveryContext context) {
		return flow.process(context)
				.publishOn(Schedulers.boundedElastic());
	}

	@Override
	protected Flux<DeliveryResponse> response(DeliveryContext context) {
		return responseProducer.produce(context)
				.publishOn(Schedulers.boundedElastic());
	}
}
