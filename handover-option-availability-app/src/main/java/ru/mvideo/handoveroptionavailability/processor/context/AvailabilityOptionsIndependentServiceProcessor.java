package ru.mvideo.handoveroptionavailability.processor.context;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;
import ru.mvideo.handoveroptionavailability.model.external.msp.logistic.HandoverType;
import ru.mvideo.handoveroptionavailability.model.external.msp.logistic.response.AvailabilityOption;
import ru.mvideo.handoveroptionavailability.processor.BaseProcessor;
import ru.mvideo.handoveroptionavailability.processor.filter.fit.ExtendedProduct;
import ru.mvideo.handoveroptionavailability.processor.model.Context;
import ru.mvideo.handoveroptionavailability.processor.model.Flags;

@Component
@RequiredArgsConstructor
public class AvailabilityOptionsIndependentServiceProcessor<T extends Context> extends BaseProcessor<T> {

	@Override
	protected Mono<T> executeProcessor(T context) {
		return Mono.fromCallable(() -> {
			List<AvailabilityOption> availabilityOptions = new ArrayList<>();
			for (ExtendedProduct product : context.products()) {
				final var availabilityOption =
						createAvailabilityOption(context.regionId(), product.getProduct().getProductId());
				availabilityOptions.add(availabilityOption);
			}
			context.availabilityOptions(availabilityOptions);
			return context;
		}).subscribeOn(Schedulers.boundedElastic());
	}

	@Override
	public boolean shouldRun(T context) {
		return context.flags().contains(Flags.INDEPENDENT_SERVICE);
	}

	private AvailabilityOption createAvailabilityOption(String regionId, String material) {
		final var option = new AvailabilityOption();
		option.setMaterial(material);
		option.setHandoverObject(regionId);
		option.setStockObject(regionId);
		option.setHandoverType(HandoverType.ELECTRONIC);
		option.setAvailableStock(50);
		option.setShowCaseStock(0);
		option.setStockObjectPriority(0);
		option.setAvailableDate(LocalDate.now());
		option.setValidTo(LocalDateTime.now());
		option.setPrepaidOnly(true);
		return option;
	}
}
