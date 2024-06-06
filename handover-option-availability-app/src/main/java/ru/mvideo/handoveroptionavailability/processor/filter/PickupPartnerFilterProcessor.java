package ru.mvideo.handoveroptionavailability.processor.filter;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;
import ru.mvideo.handoveroptionavailability.model.HandoverOption;
import ru.mvideo.handoveroptionavailability.model.external.msp.logistic.response.AvailabilityOption;
import ru.mvideo.handoveroptionavailability.processor.BaseProcessor;
import ru.mvideo.handoveroptionavailability.processor.filter.fit.ExtendedProduct;
import ru.mvideo.handoveroptionavailability.processor.model.BriefAndPickupContext;
import ru.mvideo.handoveroptionavailability.processor.model.HandoverObject;
import ru.mvideo.handoveroptionavailability.service.StocksAndShowcase;

@Component
@RequiredArgsConstructor
public class PickupPartnerFilterProcessor extends BaseProcessor<BriefAndPickupContext> {

	private static final Set<String> PICKUP_TYPES = Set.of(
			HandoverOption.PICKUP.getValue(),
			HandoverOption.PICKUP_PARTNER.getValue()
	);

	@Override
	protected Mono<BriefAndPickupContext> executeProcessor(BriefAndPickupContext context) {
		return Mono.fromCallable(() -> {
			final var options = context.options().stream()
					.filter(PICKUP_TYPES::contains)
					.collect(Collectors.toSet());

			for (String option : options) {
				final var optionContext = context.handoverOptionContext().get(option);

				List<AvailabilityOption> result = new ArrayList<>();

				if (option.equals(HandoverOption.PICKUP.getValue())) {
					final var handoverObjectIds = optionContext.getHandoverObjects().stream()
							.map(HandoverObject::getObjectId)
							.toList();

					result = optionContext.getAvailabilityOptions().stream()
							.filter(opt -> handoverObjectIds.contains(opt.getHandoverObject()))
							.toList();
				}
				if (option.equals(HandoverOption.PICKUP_PARTNER.getValue())) {
					final var uniqueMaterials = context.uniqueMaterials();
					final var products = context.products().stream()
							.filter(product -> uniqueMaterials.contains(product.getProduct().getProductId()))
							.collect(Collectors.toMap(product -> product.getProduct().getProductId(), ExtendedProduct::getQty));

					result = StocksAndShowcase
							.pickupPartnerAvailabilityOptions(optionContext.getAvailabilityOptions(), products).stream()
							.filter(opt -> context.sapCodes().contains(opt.getHandoverObject()))
							.toList();
				}
				if (result.isEmpty()) {
					context.disableOption(option, "Stores do not contain items to deliver the entire order");
					continue;
				}
				optionContext.setAvailabilityOptions(result);
			}
			return context;
		});
	}

	@Override
	public boolean shouldRun(BriefAndPickupContext context) {
		return context.hasOption(HandoverOption.PICKUP_PARTNER.getValue())
				|| context.hasOption(HandoverOption.PICKUP.getValue());
	}
}
