package ru.mvideo.handoveroptionavailability.service.external.zonepickupitem;

import java.util.List;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;
import ru.mvideo.handoveroptionavailability.cache.LocalCache;
import ru.mvideo.handoveroptionavailability.config.CacheProperties;
import ru.mvideo.handoveroptionavailability.mapper.HandoverObjectMapper;
import ru.mvideo.handoveroptionavailability.processor.model.HandoverObject;
import ru.mvideo.lards.pickup.item.service.ZonePickupItemClientApi;

public class ZonePickupItemCacheableServiceImpl extends AbstractZonePickupItemService implements InitializingBean {

	private final CacheProperties cacheProperties;
	private LocalCache<ZoneObjectKey, List<HandoverObject>> cache;

	public ZonePickupItemCacheableServiceImpl(ZonePickupItemClientApi client, HandoverObjectMapper handoverObjectMapper,
	                                          CacheProperties cacheProperties) {
		super(client, handoverObjectMapper);
		this.cacheProperties = cacheProperties;
	}

	@Override
	public Mono<List<HandoverObject>> getObjectsByZones(String regionId, String brand) {
		return cache.get(new ZoneObjectKey(regionId, brand));
	}

	@Override
	public void afterPropertiesSet() throws Exception {
		this.cache = new LocalCache<>(
				cacheProperties.getHandoverOptionZoneObjectsTtl(),
				request -> findObjectByZone(request.regionId, request.brand)
						.collectList()
						.map(handoverObjectMapper::toHandoverObjects)
		);
	}


	private record ZoneObjectKey(String regionId, String brand) {
	}
}
