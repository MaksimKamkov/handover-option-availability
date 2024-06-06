package ru.mvideo.handoveroptionavailability.processor.response;

import java.util.List;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Flux;
import reactor.core.scheduler.Schedulers;
import ru.mvideo.handoveroptionavailability.model.PickupResponseItem;
import ru.mvideo.handoveroptionavailability.processor.model.BriefAndPickupContext;
import ru.mvideo.handoveroptionavailability.processor.response.option.pickup.PickupHandoverOptionService;

@Component
@RequiredArgsConstructor
public class PickupResponseProducer implements ResponseProducer<Flux<PickupResponseItem>, BriefAndPickupContext> {
	private final List<PickupHandoverOptionService> optionServices;

	@Override
	public Flux<PickupResponseItem> produce(BriefAndPickupContext context) {
		return Flux.fromIterable(optionServices)
				.parallel()
				.runOn(Schedulers.parallel())
				.map(response -> response.getOption(context))
				.filter(Optional::isPresent)
				.map(Optional::get)
				.sequential();
	}
}
