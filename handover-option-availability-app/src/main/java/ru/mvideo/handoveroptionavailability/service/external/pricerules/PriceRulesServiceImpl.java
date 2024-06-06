package ru.mvideo.handoveroptionavailability.service.external.pricerules;

import java.math.BigDecimal;
import java.util.List;
import java.util.Set;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;
import ru.mvideo.handoveroptionavailability.exception.HandoverOptionAvailabilityException;
import ru.mvideo.lards.price.rules.api.v2.client.PriceRulesPublicApi;
import ru.mvideo.lards.price.rules.api.v2.client.ZoneOptionPriceListPublicApi;
import ru.mvideo.lards.price.rules.model.HandoverOption;
import ru.mvideo.lards.price.rules.model.MinPriceBatchRequest;
import ru.mvideo.lards.price.rules.model.MinPriceBatchResponse;
import ru.mvideo.lards.price.rules.model.MinPriceRequest;
import ru.mvideo.lards.price.rules.model.MinPriceResponse;
import ru.mvideo.lards.price.rules.model.ZoneOptionPriceListDetailedResponseV2;
import ru.mvideo.lards.price.rules.model.ZoneOptionPriceListDetailsRequestV2;

@Service
@RequiredArgsConstructor
public class PriceRulesServiceImpl implements PriceRulesService {

	private final ZoneOptionPriceListPublicApi zoneOptionPriceListPublicApi;
	private final PriceRulesPublicApi priceRulesPublicApi;

	@Override
	public Flux<ZoneOptionPriceListDetailedResponseV2> fetchPriceRules(Set<String> zones, Set<String> handoverOptions) {
		return Flux.defer(() -> {
			var request = new ZoneOptionPriceListDetailsRequestV2(zones, handoverOptions);

			return zoneOptionPriceListPublicApi.findByIdList(request);

		}).subscribeOn(Schedulers.boundedElastic());
	}

	@Override
	public Flux<MinPriceResponse> getMinPriceRules(Set<String> zones, List<HandoverOption> handoverOptions) {
		return Mono.fromSupplier(() -> new MinPriceRequest(zones, handoverOptions))
				.flatMapMany(priceRulesPublicApi::findMinPrice)
				.subscribeOn(Schedulers.boundedElastic());
	}

	@Override
	public Flux<MinPriceBatchResponse> getMinPriceRulesBatch(Set<String> zones, Set<String> handoverOptions, Set<BigDecimal> prices) {
		return Mono.fromSupplier(() -> new MinPriceBatchRequest(zones, handoverOptions, prices))
				.flatMapMany(priceRulesPublicApi::findMinPriceBatch)
				.switchIfEmpty(Mono.error(
						new HandoverOptionAvailabilityException("Отсутствуют данные в ответе сервиса handover-price-rules", 1104)))
				.subscribeOn(Schedulers.boundedElastic());
	}
}
