package ru.mvideo.handoveroptionavailability.processor.context.batch;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;
import ru.mvideo.handoveroptionavailability.model.HandoverOption;
import ru.mvideo.handoveroptionavailability.processor.BaseProcessor;
import ru.mvideo.handoveroptionavailability.processor.model.BatchContext;
import ru.mvideo.handoveroptionavailability.service.external.pickuppoints.PickupPointRestrictionService;

@Component
@RequiredArgsConstructor
public class PickupPointsBatchProcessor extends BaseProcessor<BatchContext> {

	private final PickupPointRestrictionService pickupPointsService;

	@Override
	protected Mono<BatchContext> executeProcessor(BatchContext context) {
		return Mono.defer(() -> {
			final var availabilityOptions = context.availabilityOptions().stream()
					.filter(opt -> context.sapCodes().contains(opt.getHandoverObject()))
					.toList();
			final var rimCode = context.regionId();
			final var brand = context.retailBrand().getValue();
			final var products = context.products();

			return pickupPointsService.batchPickupPoints(rimCode, brand, availabilityOptions, products)
					.publishOn(Schedulers.parallel())
					.doOnSuccess(pickupPoints -> {
						if (pickupPoints == null) {
							context.disableOption(HandoverOption.PICKUP_PARTNER.getValue(), "No matching pickup points found");
						}
						context.batchPickupPoints(pickupPoints);
					}).then(Mono.just(context));
		}).subscribeOn(Schedulers.boundedElastic());
	}

	@Override
	public boolean shouldRun(BatchContext context) {
		return context.hasOption(HandoverOption.PICKUP_PARTNER.getValue());
	}
}
