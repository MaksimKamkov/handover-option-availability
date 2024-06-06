package ru.mvideo.handoveroptionavailability.processor.context.brief;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;
import ru.mvideo.handoveroptionavailability.model.HandoverOption;
import ru.mvideo.handoveroptionavailability.processor.BaseProcessor;
import ru.mvideo.handoveroptionavailability.processor.model.BriefAndPickupContext;
import ru.mvideo.handoveroptionavailability.service.external.pickuppoints.PickupPointRestrictionService;

@Component
@RequiredArgsConstructor
public class PickupPointsBriefProcessor extends BaseProcessor<BriefAndPickupContext> {

	private final PickupPointRestrictionService pickupPointsService;

	@Override
	protected Mono<BriefAndPickupContext> executeProcessor(BriefAndPickupContext context) {
		return Mono.defer(() -> {
			final var optionContext = context.handoverOptionContext().get(HandoverOption.PICKUP_PARTNER.getValue());
			final var availabilityOptions = optionContext.getAvailabilityOptions();
			final var rimCode = context.regionId();
			final var brand = context.retailBrand().getValue();
			final var products = context.products();

			return pickupPointsService.briefPickupPoints(rimCode, brand, availabilityOptions, products)
					.doOnSuccess(pickupPoints -> {
						if (pickupPoints == null) {
							context.disableOption(HandoverOption.PICKUP_PARTNER.getValue(), "No matching pickup points found");
						}
						context.briefPickupPoints(pickupPoints);
					}).then(Mono.just(context));
		});
	}

	@Override
	public boolean shouldRun(BriefAndPickupContext context) {
		return context.hasOption(HandoverOption.PICKUP_PARTNER.getValue());
	}
}
