package ru.mvideo.handoveroptionavailability.service.external.zonepickupitem;

import java.util.Collections;
import java.util.List;
import lombok.RequiredArgsConstructor;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import ru.mvideo.handoveroptionavailability.mapper.HandoverObjectMapper;
import ru.mvideo.lards.geospatial.model.GeoPoint;
import ru.mvideo.lards.model.RetailBrand;
import ru.mvideo.lards.pickup.item.service.ZonePickupItemClientApi;
import ru.mvideo.lards.zone.pickup.item.model.ObjectType;
import ru.mvideo.lards.zone.pickup.item.model.PickupHandoverFilter;
import ru.mvideo.lards.zone.pickup.item.model.ZoneHandoverPickupObjectDistanceListRequest;
import ru.mvideo.lards.zone.pickup.item.model.ZoneHandoverPickupObjectDistanceRequest;
import ru.mvideo.lards.zone.pickup.item.model.ZoneHandoverPickupObjectDistanceResponse;
import ru.mvideo.lards.zone.pickup.item.model.ZoneHandoverPickupObjectResponse;

@RequiredArgsConstructor
public abstract class AbstractZonePickupItemService implements ZonePickupItemService {

	protected static final List<ObjectType> OBJECT_TYPES = List.of(ObjectType.SHOP);
	protected final ZonePickupItemClientApi client;
	protected final HandoverObjectMapper handoverObjectMapper;

	@Override
	public Flux<ZoneHandoverPickupObjectDistanceResponse> getObjectsByRadius(String regionId, String brand,
	                                                                         GeoPoint point, double maxRadius) {
		final var pickupHandoverFilter = new PickupHandoverFilter(RetailBrand.valueOf(brand), OBJECT_TYPES);

		final var request = Collections.singletonList(new ZoneHandoverPickupObjectDistanceRequest(regionId, point, maxRadius));

		final var zoneHandoverPickupObjectDistanceListRequest = new ZoneHandoverPickupObjectDistanceListRequest(request, pickupHandoverFilter);

		return client.getObjectsByZoneWithDistance(zoneHandoverPickupObjectDistanceListRequest);
	}

	protected Flux<ZoneHandoverPickupObjectResponse> findObjectByZone(String regionId, String brand) {
		return Mono.just(new PickupHandoverFilter(RetailBrand.valueOf(brand), OBJECT_TYPES))
				.flatMapMany(filter -> client.getObjectsByZones(Collections.singletonList(regionId), filter));
	}
}
