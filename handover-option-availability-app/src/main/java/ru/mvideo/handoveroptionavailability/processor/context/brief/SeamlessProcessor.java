package ru.mvideo.handoveroptionavailability.processor.context.brief;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;
import ru.mvideo.availability_chains.model.RelatedObjectsDetails;
import ru.mvideo.handoveroptionavailability.model.HandoverOption;
import ru.mvideo.handoveroptionavailability.model.external.msp.logistic.response.AvailabilityOption;
import ru.mvideo.handoveroptionavailability.processor.BaseProcessor;
import ru.mvideo.handoveroptionavailability.processor.model.BriefAndPickupContext;
import ru.mvideo.handoveroptionavailability.processor.utils.ProviderUtil;
import ru.mvideo.handoveroptionavailability.service.external.seamless.SeamlessService;
import ru.mvideo.lards.price.rules.model.MinPriceResponse;

@Component
@RequiredArgsConstructor
public class SeamlessProcessor extends BaseProcessor<BriefAndPickupContext> {

	private final SeamlessService seamlessService;

	@Override
	protected Mono<BriefAndPickupContext> executeProcessor(BriefAndPickupContext context) {

		final var seamlessProviders = context.providers().stream()
				.filter(provider -> provider.getHandoverOptions().stream()
						.anyMatch(option -> option.getName().equals(HandoverOption.PICKUP_SEAMLESS.getValue())))
				.collect(Collectors.toList());

		final var maxDistance = ProviderUtil.findMaxRadius(seamlessProviders).orElse(0.0);

		final var optionContext = context.handoverOptionContext().get(HandoverOption.PICKUP_SEAMLESS.getValue());

		final var handoverObjects = optionContext.getAvailabilityOptions().stream()
				.map(AvailabilityOption::getHandoverObject)
				.collect(Collectors.toSet());

		return seamlessService.getRelatedObjectsListDetails(handoverObjects, maxDistance)
				.map(relatedObjects -> filterBySeamlessPriceLists(context, relatedObjects))
				.doOnNext(relatedObjects -> {
					if (relatedObjects.isEmpty()) {
						context.disableOption(HandoverOption.PICKUP_SEAMLESS.getValue(), "No matching price rules found for seamless");
					}
				})
				.map(context::seamlessRelatedObjectsDetails);
	}

	private List<RelatedObjectsDetails> filterBySeamlessPriceLists(BriefAndPickupContext context, List<RelatedObjectsDetails> relatedObjects) {
		if (context.minPriceRules() == null) {
			return Collections.emptyList();
		}

		List<MinPriceResponse> seamlessPriceLists = context.minPriceRules().stream()
				.filter(minPriceResponse ->
						HandoverOption.PICKUP_SEAMLESS.getValue()
								.equals(minPriceResponse.getHandoverOptionName()))
				.collect(Collectors.toList());

		return relatedObjects.stream()
				.filter(relatedObject -> hasAcceptablePriceList(seamlessPriceLists, relatedObject.getDistance()))
				.collect(Collectors.toList());
	}

	@Override
	public boolean shouldRun(BriefAndPickupContext context) {
		return context.hasOption(HandoverOption.PICKUP_SEAMLESS.getValue());
	}

	private boolean hasAcceptablePriceList(List<MinPriceResponse> minPriceRules, Double distance) {
		return minPriceRules.stream()
				.anyMatch(minPriceResponse -> distance >= minPriceResponse.getMinRadius()
						&& distance <= minPriceResponse.getMaxRadius());
	}
}
