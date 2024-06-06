package ru.mvideo.handoveroptionavailability.processor.context.stock;

import java.util.Collections;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;
import ru.mvideo.handoveroptionavailability.model.HandoverOption;
import ru.mvideo.handoveroptionavailability.processor.BaseProcessor;
import ru.mvideo.handoveroptionavailability.processor.model.StockObjectsContext;
import ru.mvideo.handoveroptionavailability.service.external.pickuppoints.PickupPointsService;

@Component
@RequiredArgsConstructor
public class LoadPickupPointsByIdProcessor extends BaseProcessor<StockObjectsContext> {

	private final PickupPointsService pickupPointsService;

	@Override
	protected Mono<StockObjectsContext> executeProcessor(StockObjectsContext context) {
		return Mono.defer(() -> pickupPointsService.getPickPoints(List.of(context.pickupPointId()))
				.map(pickupPoints -> {
					if (pickupPoints.isEmpty()) {
						context.disableOption(HandoverOption.PICKUP_PARTNER.getValue(), "Not found valid pickup points");
					} else {
						context.pickupPoints(pickupPoints);
					}

					return context;
				})
				.switchIfEmpty(Mono.fromCallable(() -> {
					context.availabilityOptions(Collections.emptyList());
					return context;
				}))).subscribeOn(Schedulers.boundedElastic());
	}

	@Override
	public boolean shouldRun(StockObjectsContext context) {
		return context.hasOption(HandoverOption.PICKUP_PARTNER.getValue());
	}

}
