package ru.mvideo.handoveroptionavailability.processor.handler;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;
import ru.mvideo.handoveroptionavailability.model.DeliveryProvidersRequestV2;
import ru.mvideo.handoveroptionavailability.model.DeliveryProvidersResponse;
import ru.mvideo.handoveroptionavailability.processor.flow.DeliveryProvidersFlow;
import ru.mvideo.handoveroptionavailability.processor.model.DeliveryProvidersContext;
import ru.mvideo.handoveroptionavailability.processor.request.DeliveryProvidersProducer;
import ru.mvideo.handoveroptionavailability.processor.response.DeliveryProvidersResponseProducer;

@RequiredArgsConstructor
@Component
public class DeliveryProvidersHandler extends AbstractRequestMonoHandler<DeliveryProvidersRequestV2, DeliveryProvidersResponse, DeliveryProvidersContext> {

	private final DeliveryProvidersProducer contextProducer;
	private final DeliveryProvidersFlow flow;
	private final DeliveryProvidersResponseProducer responseProducer;

	@Override
	protected Mono<DeliveryProvidersContext> context(DeliveryProvidersRequestV2 request) {
		return contextProducer.produce(request)
				.publishOn(Schedulers.boundedElastic());
	}

	@Override
	protected Mono<DeliveryProvidersContext> process(DeliveryProvidersContext context) {
		return flow.process(context)
				.publishOn(Schedulers.boundedElastic());
	}

	@Override
	protected Mono<DeliveryProvidersResponse> response(DeliveryProvidersContext context) {
		return responseProducer.produce(context)
				.publishOn(Schedulers.boundedElastic());
	}
}
