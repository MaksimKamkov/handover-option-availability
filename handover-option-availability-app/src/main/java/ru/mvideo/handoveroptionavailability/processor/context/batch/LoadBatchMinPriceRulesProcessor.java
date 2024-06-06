package ru.mvideo.handoveroptionavailability.processor.context.batch;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;
import ru.mvideo.handoveroptionavailability.model.Material;
import ru.mvideo.handoveroptionavailability.processor.BaseProcessor;
import ru.mvideo.handoveroptionavailability.processor.model.BatchContext;
import ru.mvideo.handoveroptionavailability.service.external.pricerules.PriceRulesService;
import ru.mvideo.lards.price.rules.model.MinPriceBatchResponse;

@RequiredArgsConstructor
@Component
public class LoadBatchMinPriceRulesProcessor extends BaseProcessor<BatchContext> {

	private final PriceRulesService priceRulesService;

	@Override
	protected Mono<BatchContext> executeProcessor(BatchContext context) {
		return Mono.defer(() -> {
			final var prices = context.materials().stream()
					.map(Material::getPrice)
					.map(BigDecimal::valueOf)
					.collect(Collectors.toSet());

			return priceRulesService.getMinPriceRulesBatch(context.zoneIds(), new HashSet<>(context.options()), prices)
					.collectList()
					.publishOn(Schedulers.parallel())
					.map(response -> {
						final Map<BigDecimal, Map<String, BigDecimal>> priceOptions = response.stream().collect(
								Collectors.toMap(
										MinPriceBatchResponse::getPrice,
										MinPriceBatchResponse::getMinPriceHandoverOptions)
						);

						final Map<String, Map<String, BigDecimal>> materialOptionPrice = new HashMap<>();
						for (Material material : context.materials()) {
							materialOptionPrice.put(material.getMaterial(),
									priceOptions.get(BigDecimal.valueOf(material.getPrice())));
						}

						context.materialOptionPrice(materialOptionPrice);
						return context;
					});
		}).subscribeOn(Schedulers.boundedElastic());
	}

	@Override
	public boolean shouldRun(BatchContext context) {
		return true;
	}
}
