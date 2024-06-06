package ru.mvideo.handoveroptionavailability.processor.response;

import java.util.List;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Flux;
import reactor.core.scheduler.Schedulers;
import ru.mvideo.handoveroptionavailability.model.DeliveryResponse;
import ru.mvideo.handoveroptionavailability.processor.model.DeliveryContext;
import ru.mvideo.handoveroptionavailability.processor.response.option.delivery.DetailedHandoverOptionService;

@Component
@RequiredArgsConstructor
public class DeliveryResponseProducer implements ResponseProducer<Flux<DeliveryResponse>, DeliveryContext> {
	private final List<DetailedHandoverOptionService> optionServices;

	@Override
	public Flux<DeliveryResponse> produce(DeliveryContext context) {
		return Flux.fromIterable(optionServices)
				.parallel()
				.runOn(Schedulers.parallel())
				.map(response -> response.getOption(context))
				.filter(Optional::isPresent)
				.map(Optional::get)
				.sequential();
	}
}
