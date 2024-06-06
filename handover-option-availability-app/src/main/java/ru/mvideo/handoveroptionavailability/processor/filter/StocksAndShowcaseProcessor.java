package ru.mvideo.handoveroptionavailability.processor.filter;

import static ru.mvideo.handoveroptionavailability.model.HandoverOption.ETA_DELIVERY;
import static ru.mvideo.handoveroptionavailability.model.HandoverOption.EXACTLY_TIME_DELIVERY;
import static ru.mvideo.handoveroptionavailability.model.HandoverOption.PICKUP_SEAMLESS;

import java.time.LocalTime;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;
import ru.mvideo.handoveroptionavailability.model.HandoverOption;
import ru.mvideo.handoveroptionavailability.model.external.msp.logistic.response.AvailabilityOption;
import ru.mvideo.handoveroptionavailability.processor.BaseProcessor;
import ru.mvideo.handoveroptionavailability.processor.filter.fit.ExtendedProduct;
import ru.mvideo.handoveroptionavailability.processor.model.Context;
import ru.mvideo.handoveroptionavailability.processor.model.HandoverObject;
import ru.mvideo.handoveroptionavailability.processor.model.OptionContext;
import ru.mvideo.handoveroptionavailability.service.StocksAndShowcase;
import ru.mvideo.lards.zone.model.ZoneAttributeResponse;

@Component
@RequiredArgsConstructor
public class StocksAndShowcaseProcessor<T extends Context> extends BaseProcessor<T> {

	private static final Set<String> ETA_AND_EXACTLY_TYPES = Set.of(
			ETA_DELIVERY.getValue(),
			EXACTLY_TIME_DELIVERY.getValue(),
			PICKUP_SEAMLESS.getValue()
	);
	private static final LocalTime START_OF_DAY = LocalTime.of(0, 0);
	private static final LocalTime DELIVERY_END_TIME = LocalTime.of(22, 0);
	private static final LocalTime END_OF_DAY = LocalTime.of(23, 59);

	@Override
	protected Mono<T> executeProcessor(T context) {
		return Mono.fromCallable(() -> {
			final var options = context.options().stream()
					.filter(ETA_AND_EXACTLY_TYPES::contains)
					.collect(Collectors.toSet());

			final var showcase = getShowcase(context);

			final var products = context.products().stream()
					.filter(product -> product.getPrice() != null)
					.toList();

			for (String option : options) {
				final var optionContext = context.handoverOptionContext().get(option);

				final var handoverObjectIds = optionContext.getHandoverObjects().stream()
						.map(HandoverObject::getObjectId)
						.toList();

				final var availabilityOptions = optionContext.getAvailabilityOptions().stream()
						.filter(opt -> handoverObjectIds.contains(opt.getHandoverObject()))
						.toList();

				boolean showcaseDeliveryAvailable = !showcaseDeliveryAvailable(optionContext) && showcase;

				final var handoverOption = HandoverOption.fromValue(option);
				final var result = getAvailabilityOptions(optionContext, handoverOption, availabilityOptions, products,
						context.handoverObjects(), showcaseDeliveryAvailable);

				if (result.isEmpty()) {
					context.disableOption(option, "Stores do not contain items to deliver the entire order");
					continue;
				}
				optionContext.setAvailabilityOptions(result);
			}
			return context;
		});
	}

	private List<AvailabilityOption> getAvailabilityOptions(OptionContext context, HandoverOption option, List<AvailabilityOption> options,
	                                                        List<ExtendedProduct> products, List<HandoverObject> handoverObjects,
	                                                        boolean showcaseDeliveryAvailable) {
		return switch (option) {
			case ETA_DELIVERY -> etaDeliveryOptions(context, options, products, showcaseDeliveryAvailable);
			case EXACTLY_TIME_DELIVERY -> exactlyTimeOptions(options, products, showcaseDeliveryAvailable);
			case PICKUP_SEAMLESS -> pickupSeamlessOptions(context, options, products, handoverObjects);
			default -> Collections.emptyList();
		};
	}

	private List<AvailabilityOption> etaDeliveryOptions(OptionContext context, List<AvailabilityOption> options,
	                                                    List<ExtendedProduct> products,
	                                                    boolean showcaseDeliveryAvailable) {

		final var availabilityOptions = StocksAndShowcase.etaAvailabilityOptions(options, products, showcaseDeliveryAvailable);
		if (CollectionUtils.isNotEmpty(availabilityOptions)) {
			return availabilityOptions;
		} else {
			final var allDayObjectIds = context.getHandoverObjects().stream()
					.filter(object -> START_OF_DAY.equals(object.getDeliveryStartTime()) && END_OF_DAY.equals(object.getDeliveryEndTime()))
					.filter(object -> {
						final var requestTime = LocalTime.now(object.getTimeZone());
						return requestTime.isAfter(DELIVERY_END_TIME) && requestTime.isBefore(END_OF_DAY);
					})
					.map(HandoverObject::getObjectId)
					.toList();

			final var allDayAvailabilityOptions = context.getAvailabilityOptions().stream()
					.filter(opt -> allDayObjectIds.contains(opt.getHandoverObject()))
					.toList();

			return StocksAndShowcase.etaPreorderAvailabilityOptions(allDayAvailabilityOptions, products, showcaseDeliveryAvailable);
		}
	}

	private List<AvailabilityOption> exactlyTimeOptions(List<AvailabilityOption> options,
	                                                    List<ExtendedProduct> products,
	                                                    boolean showcaseDeliveryAvailable) {
		return StocksAndShowcase.exactlyAvailabilityOptions(options, products, showcaseDeliveryAvailable);
	}

	private List<AvailabilityOption> pickupSeamlessOptions(OptionContext context, List<AvailabilityOption> options,
	                                                       List<ExtendedProduct> products, List<HandoverObject> handoverObjects) {
		if (showcaseDeliveryAvailable(context)) {
			return Collections.emptyList();
		} else {
			return StocksAndShowcase.seamlessAvailabilityOptions(options, products, handoverObjects);
		}
	}

	private boolean showcaseDeliveryAvailable(OptionContext context) {
		final var zoneItemsHandoverObjectIds = context.getHandoverObjects().stream()
				.map(HandoverObject::getObjectId)
				.collect(Collectors.toSet());

		//availabilityOptions не содержит значений "stockObject", кроме полученных в ZonePickupItemService.getObjectsByRadius,
		// для любой даты доступности с любым количеством товара на складе и на витрине
		var extraStockObject = false;
		for (AvailabilityOption option : context.getAvailabilityOptions()) {
			if (!zoneItemsHandoverObjectIds.contains(option.getStockObject())) {
				extraStockObject = true;
				break;
			}
		}
		return extraStockObject;
	}

	private boolean getShowcase(T context) {
		final var zoneDetailResponse = context.regionDetails();
		if (zoneDetailResponse.getAttributes() == null) {
			return false;
		}

		final var showcaseAttribute =
				zoneDetailResponse.getAttributes().stream()
						.filter(attribute -> "isShowcaseDeliveryAvailable".equals(attribute.getName()))
						.map(ZoneAttributeResponse::getValue)
						.findFirst()
						.orElse("false");
		return Boolean.parseBoolean(showcaseAttribute);
	}

	@Override
	public boolean shouldRun(T context) {
		return context.hasOption(ETA_DELIVERY.getValue())
				|| context.hasOption(EXACTLY_TIME_DELIVERY.getValue())
				|| context.hasOption(PICKUP_SEAMLESS.getValue());
	}
}
