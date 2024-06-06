package ru.mvideo.handoveroptionavailability.processor.context;

import java.math.BigDecimal;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;
import ru.mvideo.handoveroptionavailability.model.HandoverOption;
import ru.mvideo.handoveroptionavailability.model.external.msp.logistic.response.AvailabilityOption;
import ru.mvideo.handoveroptionavailability.processor.BaseProcessor;
import ru.mvideo.handoveroptionavailability.processor.filter.fit.ExtendedProduct;
import ru.mvideo.handoveroptionavailability.processor.model.Context;
import ru.mvideo.handoveroptionavailability.processor.model.HandoverObject;
import ru.mvideo.handoveroptionavailability.processor.model.OptionContext;
import ru.mvideo.handoveroptionavailability.service.external.pricerules.PriceRulesService;

@RequiredArgsConstructor
@Component
public class LoadMinPriceRulesProcessor<T extends Context> extends BaseProcessor<T> {

	private static final Set<String> ETA_AND_EXACTLY = Set.of(
			HandoverOption.ETA_DELIVERY.getValue(),
			HandoverOption.EXACTLY_TIME_DELIVERY.getValue()
	);

	private static final Set<String> HANDOVER_OPTION_WITH_SPLIT_BASKET = Set.of(
			HandoverOption.INTERVAL_DELIVERY.getValue(),
			HandoverOption.DPD_DELIVERY.getValue(),
			HandoverOption.ELECTRONIC_DELIVERY.getValue(),
			HandoverOption.PICKUP.getValue()
	);

	private final PriceRulesService priceRulesService;

	@Override
	protected Mono<T> executeProcessor(T context) {
		//collect min distance for ETA_AND_EXACTLY options
		return Flux.fromIterable(context.options())
				.filter(ETA_AND_EXACTLY::contains)
				.map(opt -> context.handoverOptionContext().get(opt))
				.map(this::calculateNearestPickupObject)
				.flatMap(Mono::justOrEmpty)
				.map(HandoverObject::getDistance)
				.sort()
				.next()
				.map(Optional::of)
				.defaultIfEmpty(Optional.empty())
				//collect total price per handover option name
				.flatMap(distance -> Flux.fromIterable(context.options())
						.flatMap(option -> Mono.just(option)
								.filter(HANDOVER_OPTION_WITH_SPLIT_BASKET::contains)
								.map(opt -> context.handoverOptionContext().get(opt))
								.map(OptionContext::getProducts)
								.switchIfEmpty(Mono.just(context.products()))
								.map(products -> new ru.mvideo.lards.price.rules.model.HandoverOption(
										option, BigDecimal.valueOf(getTotalOrderPrice(products)),
										distance.orElse(null))))
						.collectList())
				.flatMapMany(request -> priceRulesService.getMinPriceRules(context.zoneIds(), request))
				.collectList()
				.doOnNext(context::minPriceRules)
				.then(Mono.just(context))
				.subscribeOn(Schedulers.parallel());
	}

	@Override
	public boolean shouldRun(T context) {
		return true;
	}

	private Double getTotalOrderPrice(List<ExtendedProduct> products) {
		return products.stream()
				.filter(product -> product.getPrice() != null)
				.map(material -> material.getPrice() * material.getQty())
				.reduce(0D, Double::sum);
	}

	private Optional<HandoverObject> calculateNearestPickupObject(OptionContext context) {
		final var handoverObjectsWithStocks = context.getAvailabilityOptions().stream()
				.map(AvailabilityOption::getHandoverObject)
				.collect(Collectors.toSet());
		return context.getHandoverObjects().stream()
				.filter(handoverObject -> handoverObjectsWithStocks.contains(handoverObject.getObjectId()))
				.filter(handoverObject -> handoverObject.getDistance() != null)
				.min(Comparator.comparingDouble(HandoverObject::getDistance));
	}
}
