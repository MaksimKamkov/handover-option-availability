package ru.mvideo.handoveroptionavailability.processor.context.deliveryproviders;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;
import ru.mvideo.handoveroptionavailability.model.HandoverOption;
import ru.mvideo.handoveroptionavailability.processor.BaseProcessor;
import ru.mvideo.handoveroptionavailability.processor.model.DeliveryProvidersContext;
import ru.mvideo.handoveroptionavailability.service.external.pricerules.PriceRulesService;
import ru.mvideo.lards.handover.option.model.ProviderZoneAttributesHandoverOptions;
import ru.mvideo.lards.price.rules.model.ZoneOptionPriceListDetailedResponseV2;

@Slf4j
@Component
@RequiredArgsConstructor
public class PriceRuleProcessor extends BaseProcessor<DeliveryProvidersContext> {

	private final PriceRulesService priceRulesService;

	private static final List<String> OPTIONS = List.of(
			HandoverOption.ETA_DELIVERY.getValue(),
			HandoverOption.EXACTLY_TIME_DELIVERY.getValue(),
			HandoverOption.PICKUP_SEAMLESS.getValue()
	);

	@Override
	protected Mono<DeliveryProvidersContext> executeProcessor(DeliveryProvidersContext context) {

		return Mono.defer(() -> priceRulesService.fetchPriceRules(context.zoneIds(), new HashSet<>(context.options()))
				.collectList()
				.flatMap(priceRules -> {

					if (priceRules.isEmpty()) {
						context.disableOptions(OPTIONS, "Empty response from price rules");
					}

					List<ProviderZoneAttributesHandoverOptions> providers = new ArrayList<>();
					for (ProviderZoneAttributesHandoverOptions provider : context.providers()) {
						for (ZoneOptionPriceListDetailedResponseV2 priceRule : priceRules) {
							if (provider.getZoneIds().contains(priceRule.getZoneId())) {
								providers.add(provider);
							}
						}
					}
					context.providers(providers);
					return Mono.just(context);
				}));
	}

	@Override
	public boolean shouldRun(DeliveryProvidersContext context) {
		return true;
	}
}
