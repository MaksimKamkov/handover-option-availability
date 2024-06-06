package ru.mvideo.handoveroptionavailability.processor.context;

import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;
import ru.mvideo.handoveroptionavailability.model.external.msp.logistic.HandoverType;
import ru.mvideo.handoveroptionavailability.model.external.msp.logistic.response.AvailabilityOption;
import ru.mvideo.handoveroptionavailability.processor.BaseProcessor;
import ru.mvideo.handoveroptionavailability.processor.model.BriefAndPickupContext;
import ru.mvideo.handoveroptionavailability.processor.model.Context;
import ru.mvideo.handoveroptionavailability.processor.model.DeliveryContext;
import ru.mvideo.handoveroptionavailability.processor.model.Flags;
import ru.mvideo.handoveroptionavailability.service.external.msp.logistic.MspLogisticService;

@Component
@RequiredArgsConstructor
public class AvailabilityOptionsProcessor<T extends Context> extends BaseProcessor<T> {

	private final MspLogisticService mspLogisticService;

	@Override
	protected Mono<T> executeProcessor(T context) {
		return Mono.defer(() -> fetchAvailabilityCalendar(context)
				.map(options -> {
					context.availabilityOptions(options);
					return context;
				})
				.switchIfEmpty(Mono.fromSupplier(() -> {
					context.availabilityOptions(Collections.emptyList());
					return context;
				})))
				.subscribeOn(Schedulers.boundedElastic());
	}

	@Override
	public boolean shouldRun(T context) {
		return !context.flags().contains(Flags.INDEPENDENT_SERVICE);
	}

	private Mono<List<AvailabilityOption>> fetchAvailabilityCalendar(T context) {
		var handoverTypes = context.options().stream()
				.map(this::defineHandoverType)
				.filter(Objects::nonNull)
				.collect(Collectors.toSet());

		if (context.stockObjects().isEmpty()) {
			HandoverObjectsData handoverObjectsData = getHandoverObjectsData(context);
			return mspLogisticService.fetchAvailabilityCalendar(
					context.retailBrand(),
					context.regionId(),
					handoverTypes,
					context.materials(),
					handoverObjectsData.handoverObjects(),
					handoverObjectsData.sapCodes()
			);
		}
		return mspLogisticService.fetchAvailabilityCalendarForStockObjects(
				context.retailBrand(),
				context.regionId(),
				handoverTypes,
				context.stockObjects(),
				context.stockHandoverObjects(),
				context.sapCodes());
	}

	private HandoverType defineHandoverType(String handoverOption) {
		return switch (handoverOption) {
			case "interval-delivery" -> HandoverType.COURIER;
			case "dpd-delivery" -> HandoverType.POSTAL;
			case "electronic-delivery" -> HandoverType.ELECTRONIC;
			case "eta-delivery", "exactly-time-delivery", "pickup", "pickup-partner", "pickup-seamless" -> HandoverType.PICKUP;
			default -> null;
		};
	}

	private HandoverObjectsData getHandoverObjectsData(T context) {
		if (context instanceof BriefAndPickupContext) {
			return context.stockHandoverObjects().isEmpty()
					? new HandoverObjectsData(context.pickupObjectIds(), context.sapCodes())
					: new HandoverObjectsData(context.stockHandoverObjects(), Collections.emptySet());
		} else if (context instanceof DeliveryContext) {
			return new HandoverObjectsData(context.pickupObjectIds(), Collections.emptySet());
		}
		return new HandoverObjectsData(context.pickupObjectIds(), context.sapCodes());
	}

	private record HandoverObjectsData(Set<String> handoverObjects, Set<String> sapCodes) {
	}
}
