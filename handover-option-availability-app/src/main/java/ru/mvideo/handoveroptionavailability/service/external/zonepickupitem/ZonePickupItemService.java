package ru.mvideo.handoveroptionavailability.service.external.zonepickupitem;

import java.util.List;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import ru.mvideo.handoveroptionavailability.processor.model.HandoverObject;
import ru.mvideo.lards.geospatial.model.GeoPoint;
import ru.mvideo.lards.zone.pickup.item.model.ZoneHandoverPickupObjectDistanceResponse;

public interface ZonePickupItemService {

	Mono<List<HandoverObject>> getObjectsByZones(String regionId, String brand);

	Flux<ZoneHandoverPickupObjectDistanceResponse> getObjectsByRadius(String regionId, String brand, GeoPoint point, double maxRadius);
}
