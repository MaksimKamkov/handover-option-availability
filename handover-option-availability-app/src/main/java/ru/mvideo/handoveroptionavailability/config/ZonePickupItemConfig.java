package ru.mvideo.handoveroptionavailability.config;

import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import ru.mvideo.handoveroptionavailability.config.annotation.condition.ConditionalOnZoneObjectsCacheEnabled;
import ru.mvideo.handoveroptionavailability.mapper.HandoverObjectMapper;
import ru.mvideo.handoveroptionavailability.service.external.zonepickupitem.ZonePickupItemCacheableServiceImpl;
import ru.mvideo.handoveroptionavailability.service.external.zonepickupitem.ZonePickupItemService;
import ru.mvideo.handoveroptionavailability.service.external.zonepickupitem.ZonePickupItemServiceImpl;
import ru.mvideo.lards.pickup.item.service.ZonePickupItemClientApi;

@Configuration
public class ZonePickupItemConfig {

	@Bean
	@ConditionalOnZoneObjectsCacheEnabled
	public ZonePickupItemService zonePickupCacheableService(ZonePickupItemClientApi client, HandoverObjectMapper handoverObjectMapper,
	                                                        CacheProperties cacheProperties) {
		return new ZonePickupItemCacheableServiceImpl(client, handoverObjectMapper, cacheProperties);
	}

	@Bean
	@ConditionalOnMissingBean(ZonePickupItemService.class)
	public ZonePickupItemService defaultZonePickupService(ZonePickupItemClientApi client, HandoverObjectMapper handoverObjectMapper) {
		return new ZonePickupItemServiceImpl(client, handoverObjectMapper);
	}
}
