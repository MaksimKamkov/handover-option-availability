package ru.mvideo.handoveroptionavailability.processor.context.deliveryproviders;

import java.util.List;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;
import ru.mvideo.handoveroptionavailability.model.HandoverOption;
import ru.mvideo.handoveroptionavailability.processor.BaseProcessor;
import ru.mvideo.handoveroptionavailability.processor.model.DeliveryProvidersContext;
import ru.mvideo.lards.geospatial.utils.DistanceMeasure;
import ru.mvideo.lards.geospatial.utils.HaversineDistanceUtil;

@Slf4j
@Component
@RequiredArgsConstructor
public class ProvidersInDistanceProcessor extends BaseProcessor<DeliveryProvidersContext> {

	private static final List<String> OPTIONS = List.of(
			HandoverOption.ETA_DELIVERY.getValue(),
			HandoverOption.EXACTLY_TIME_DELIVERY.getValue(),
			HandoverOption.PICKUP_SEAMLESS.getValue()
	);

	@Override
	protected Mono<DeliveryProvidersContext> executeProcessor(DeliveryProvidersContext context) {
		return Mono.fromCallable(() -> {

			final var radius = HaversineDistanceUtil.Companion.calculateHaversineDistance(
					context.getCoordinatesRecipient(),
					context.getCoordinatesSource(),
					DistanceMeasure.KM
			);

			final var providersInRadius = context.providers().stream()
					.filter(response -> response.getProviderAttributes().stream()
							.anyMatch(attribute -> "source_radius".equals(attribute.getName())
									&& Double.parseDouble(attribute.getValue()) >= radius)
					)
					.collect(Collectors.toList());

			if (providersInRadius.isEmpty()) {
				context.disableOptions(OPTIONS, "No providers with the required delivery distance");
			} else {
				context.providers(providersInRadius);
			}
			return context;
		});
	}

	@Override
	public boolean shouldRun(DeliveryProvidersContext context) {
		return true;
	}
}
