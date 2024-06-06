package ru.mvideo.handoveroptionavailability.processor.response;

import java.util.List;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;
import ru.mvideo.handoveroptionavailability.model.BriefOption;
import ru.mvideo.handoveroptionavailability.model.BriefOptions;
import ru.mvideo.handoveroptionavailability.model.HandoverOptionType;
import ru.mvideo.handoveroptionavailability.processor.model.BriefAndPickupContext;
import ru.mvideo.handoveroptionavailability.processor.response.option.brief.BriefDeliveryHandoverOptionService;
import ru.mvideo.handoveroptionavailability.processor.response.option.brief.BriefPickupHandoverOptionService;

@Component
@RequiredArgsConstructor
public class BriefResponseProducer implements ResponseProducer<Flux<BriefOptions>, BriefAndPickupContext> {
	private final List<BriefDeliveryHandoverOptionService> deliveryOptionServices;
	private final List<BriefPickupHandoverOptionService> pickupOptionServices;

	@Override
	public Flux<BriefOptions> produce(BriefAndPickupContext context) {
		return Mono.zip(
				getBriefDeliveryOptions(context),
				getBriefPickupOptions(context)
		).flatMapMany(objects -> {
			final var deliveryResponses = objects.getT1();
			final var pickupResponses = objects.getT2();

			return Flux.create(sink -> {
				if (!deliveryResponses.isEmpty()) {
					final var delivery = new BriefOptions();
					delivery.setType(HandoverOptionType.DELIVERY);
					delivery.setOptions(deliveryResponses);
					sink.next(delivery);
				}

				if (!pickupResponses.isEmpty()) {
					final var pickup = new BriefOptions();
					pickup.setType(HandoverOptionType.PICKUP);
					pickup.setOptions(pickupResponses);
					sink.next(pickup);
				}

				sink.complete();
			});
		});
	}

	private Mono<List<BriefOption>> getBriefDeliveryOptions(BriefAndPickupContext context) {
		return Flux.fromIterable(deliveryOptionServices)
				.parallel()
				.runOn(Schedulers.parallel())
				.map(response -> response.getOption(context))
				.filter(Optional::isPresent)
				.map(Optional::get)
				.sequential()
				.collectList();
	}

	private Mono<List<BriefOption>> getBriefPickupOptions(BriefAndPickupContext context) {
		return Flux.fromIterable(pickupOptionServices)
				.parallel()
				.runOn(Schedulers.parallel())
				.map(response -> response.getOption(context))
				.filter(Optional::isPresent)
				.map(Optional::get)
				.sequential()
				.collectList();
	}
}
