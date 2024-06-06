package ru.mvideo.handoveroptionavailability.service.external.pricerules;

import java.math.BigDecimal;
import java.util.List;
import java.util.Set;
import reactor.core.publisher.Flux;
import ru.mvideo.lards.price.rules.model.HandoverOption;
import ru.mvideo.lards.price.rules.model.MinPriceBatchResponse;
import ru.mvideo.lards.price.rules.model.MinPriceResponse;
import ru.mvideo.lards.price.rules.model.ZoneOptionPriceListDetailedResponseV2;

public interface PriceRulesService {

	Flux<ZoneOptionPriceListDetailedResponseV2> fetchPriceRules(Set<String> zones, Set<String> handoverOptions);

	Flux<MinPriceResponse> getMinPriceRules(Set<String> zones, List<HandoverOption> handoverOptions);

	Flux<MinPriceBatchResponse> getMinPriceRulesBatch(Set<String> zones, Set<String> handoverOptions, Set<BigDecimal> prices);
}
