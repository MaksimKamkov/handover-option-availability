package ru.mvideo.handoveroptionavailability.processor.request;

import java.util.List;
import java.util.Set;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;
import ru.mvideo.handoveroptionavailability.exception.UnsupportedOptionException;
import ru.mvideo.handoveroptionavailability.model.HandoverOption;
import ru.mvideo.handoveroptionavailability.model.StockObjectsRequest;
import ru.mvideo.handoveroptionavailability.processor.model.StockObjectsContext;

@Component
public class StockObjectsContextProducer implements ContextProducer<StockObjectsRequest, StockObjectsContext> {

	private static final Set<HandoverOption> SUPPORTED_OPTIONS = Set.of(
			HandoverOption.INTERVAL_DELIVERY,
			HandoverOption.PICKUP,
			HandoverOption.PICKUP_PARTNER
	);

	@Override
	public Mono<StockObjectsContext> produce(StockObjectsRequest request) {
		return Mono.fromCallable(() -> prepareContext(request))
				.subscribeOn(Schedulers.parallel());
	}

	private StockObjectsContext prepareContext(StockObjectsRequest request) {
		final var handoverOption = request.getHandoverOption();
		if (!SUPPORTED_OPTIONS.contains(handoverOption)) {
			throw new UnsupportedOptionException(List.of(handoverOption));
		}

		if (HandoverOption.PICKUP_PARTNER.equals(handoverOption) && StringUtils.isBlank(request.getPickupPointId())) {
			throw new UnsupportedOptionException(List.of(HandoverOption.PICKUP_PARTNER));
		}

		final var materials = request.getMaterials();

		final var handoverOptions = List.of(handoverOption);
		final var options = options(handoverOptions);
		final var handoverOptionContext = optionContexts(options);

		final var context = StockObjectsContext.builder()
				.requestHandoverOptions(options)
				.handoverOptionContext(handoverOptionContext)
				.regionId(request.getRegionId())
				.retailBrand(request.getRetailBrand())
				.pickupPointId(request.getPickupPointId())
				.materials(materials)
				.build();

		context.pickupObjectIds().add(request.getHandoverObject());

		return context;
	}

}
