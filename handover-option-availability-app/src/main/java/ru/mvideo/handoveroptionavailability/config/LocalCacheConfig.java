package ru.mvideo.handoveroptionavailability.config;

import java.util.List;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import ru.mvideo.clop.cache.common.LoadableCache;
import ru.mvideo.clop.cache.common.MapCache;
import ru.mvideo.clop.cache.common.loader.ScheduledCacheLoader;
import ru.mvideo.lards.handover.option.api.v1.HandoverOptionPublicApi;
import ru.mvideo.lards.handover.option.model.HandoverOptionPaymentMethodResponse;
import ru.mvideo.lards.zone.client.service.ZoneClientService;
import ru.mvideo.lards.zone.model.ZoneAttributePaymentResponse;

@Configuration
public class LocalCacheConfig {

	@Bean
	public MapCache<String, HandoverOptionPaymentMethodResponse> handoverOptionPaymentMethodsCache(HandoverOptionPublicApi handoverOptionPublicApi) {
		return new MapCache<>(keys -> handoverOptionPublicApi.getAllPaymentMethods()
				.collectMap(paymentMethod -> paymentMethod.getHandoverOption().getValue()));
	}

	@Bean
	public MapCache<String, ZoneAttributePaymentResponse> zoneCreditApprovalLeadTime(ZoneClientService zoneClientService) {
		return new MapCache<>(keys -> zoneClientService.getAllPaymentAttributes()
				.collectMap(ZoneAttributePaymentResponse::getId));
	}

	@Bean
	public ScheduledCacheLoader handoverOptionPaymentMethodsCacheLoader(
			LoadableCache<String, HandoverOptionPaymentMethodResponse> handoverOptionPaymentMethodsCache,
			LoadableCache<String, ZoneAttributePaymentResponse> zoneCreditApprovalLeadTime) {

		return new ScheduledCacheLoader(List.of(handoverOptionPaymentMethodsCache, zoneCreditApprovalLeadTime));
	}
}
