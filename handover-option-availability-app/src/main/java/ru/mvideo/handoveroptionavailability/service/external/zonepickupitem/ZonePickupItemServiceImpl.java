package ru.mvideo.handoveroptionavailability.service.external.zonepickupitem;

import java.util.List;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;
import ru.mvideo.handoveroptionavailability.mapper.HandoverObjectMapper;
import ru.mvideo.handoveroptionavailability.processor.model.HandoverObject;
import ru.mvideo.lards.pickup.item.service.ZonePickupItemClientApi;

public class ZonePickupItemServiceImpl extends AbstractZonePickupItemService {

	public ZonePickupItemServiceImpl(ZonePickupItemClientApi client, HandoverObjectMapper handoverObjectMapper) {
		super(client, handoverObjectMapper);
	}

	@Override
	public Mono<List<HandoverObject>> getObjectsByZones(String regionId, String brand) {
		return findObjectByZone(regionId, brand)
				.publishOn(Schedulers.parallel())
				.collectList()
				.map(handoverObjectMapper::toHandoverObjects);
	}
}
