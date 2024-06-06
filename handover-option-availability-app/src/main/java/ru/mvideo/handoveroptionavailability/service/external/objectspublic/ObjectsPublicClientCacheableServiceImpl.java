package ru.mvideo.handoveroptionavailability.service.external.objectspublic;

import static ru.mvideo.handoveroptionavailability.service.external.objectspublic.ObjectPublicConstants.LATITUDE;
import static ru.mvideo.handoveroptionavailability.service.external.objectspublic.ObjectPublicConstants.LONGITUDE;

import java.util.List;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import ru.mvideo.handoveroptionavailability.exception.FailedToGetDeliveryCoordinatesException;
import ru.mvideo.handoveroptionavailability.service.external.objectspublic.kafka.KafkaObjectLocalCache;
import ru.mvideo.lards.geospatial.model.GeoPoint;
import ru.mvideo.oi.pbl.model.ObjectAttributeData;
import ru.mvideo.oi.pbl.model.ObjectResponseData;

public class ObjectsPublicClientCacheableServiceImpl implements ObjectsPublicClientService {

	private final KafkaObjectLocalCache cache;

	public ObjectsPublicClientCacheableServiceImpl(KafkaObjectLocalCache kafkaObjectLocalCache) {
		this.cache = kafkaObjectLocalCache;
	}

	@Override
	public Mono<GeoPoint> getCoordinateHandoverObject(String objectId) {
		return Mono.fromCallable(() -> cache.get(objectId))
				.flatMapIterable(ObjectResponseData::getAttributes)
				.collectMap(ObjectAttributeData::getAttribute, ObjectAttributeData::getValue)
				.map(coordinates -> {
					if (coordinates.isEmpty() || !coordinates.containsKey(LATITUDE) || !coordinates.containsKey(LONGITUDE)) {
						throw new FailedToGetDeliveryCoordinatesException(objectId);
					}
					return new GeoPoint(Double.parseDouble(coordinates.get(LATITUDE)), Double.parseDouble(coordinates.get(LONGITUDE)));
				});
	}

	@Override
	public Flux<ObjectResponseData> getHandoverObjectsDetails(List<String> objectIds) {
		return Flux.fromIterable(cache.getAll(objectIds).values());
	}
}
