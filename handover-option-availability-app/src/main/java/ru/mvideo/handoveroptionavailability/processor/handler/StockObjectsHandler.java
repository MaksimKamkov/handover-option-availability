package ru.mvideo.handoveroptionavailability.processor.handler;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import ru.mvideo.handoveroptionavailability.model.StockObjectsRequest;
import ru.mvideo.handoveroptionavailability.model.StockObjectsResponseItem;
import ru.mvideo.handoveroptionavailability.processor.flow.StockObjectsFlow;
import ru.mvideo.handoveroptionavailability.processor.model.StockObjectsContext;
import ru.mvideo.handoveroptionavailability.processor.request.StockObjectsContextProducer;
import ru.mvideo.handoveroptionavailability.processor.response.StockObjectsResponseProducer;

@RequiredArgsConstructor
@Component
public class StockObjectsHandler extends AbstractRequestFluxHandler<StockObjectsRequest, StockObjectsResponseItem, StockObjectsContext> {

	private final StockObjectsContextProducer contextProducer;
	private final StockObjectsFlow flow;
	private final StockObjectsResponseProducer responseProducer;

	@Override
	protected Mono<StockObjectsContext> context(StockObjectsRequest request) {
		return contextProducer.produce(request);
	}

	@Override
	protected Mono<StockObjectsContext> process(StockObjectsContext context) {
		return flow.process(context);
	}

	@Override
	protected Flux<StockObjectsResponseItem> response(StockObjectsContext context) {
		return responseProducer.produce(context);
	}
}
