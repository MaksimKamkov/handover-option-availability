package ru.mvideo.handoveroptionavailability.service.external.objectspublic;

import static ru.mvideo.handoveroptionavailability.service.external.objectspublic.ObjectPublicConstants.H_O_ATTRIBUTES;
import static ru.mvideo.handoveroptionavailability.service.external.objectspublic.ObjectPublicConstants.H_O_COORDINATE_ATTRIBUTES;
import static ru.mvideo.handoveroptionavailability.service.external.objectspublic.ObjectPublicConstants.LATITUDE;
import static ru.mvideo.handoveroptionavailability.service.external.objectspublic.ObjectPublicConstants.LONGITUDE;

import java.util.Collections;
import java.util.List;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;
import ru.mvideo.lards.geospatial.model.GeoPoint;
import ru.mvideo.oi.pbl.api.client.ObjectsPublicApiV2;
import ru.mvideo.oi.pbl.model.ObjectAttributeData;
import ru.mvideo.oi.pbl.model.ObjectResponseData;
import ru.mvideo.oi.pbl.model.ObjectValuesPublicRequest;
import ru.mvideo.oi.pbl.model.ObjectValuesPublicResponse;

public record ObjectsPublicClientServiceImpl(ObjectsPublicApiV2 client) implements ObjectsPublicClientService {

	@Override
	public Mono<GeoPoint> getCoordinateHandoverObject(String objectId) {
		return findObjectValues(Collections.singletonList(objectId), H_O_COORDINATE_ATTRIBUTES)
				.next()
				.flatMapIterable(ObjectResponseData::getAttributes)
				.collectMap(ObjectAttributeData::getAttribute, attribute -> Double.parseDouble(attribute.getValue()))
				.map(coordinates -> new GeoPoint(coordinates.get(LATITUDE), coordinates.get(LONGITUDE)));
	}

	@Override
	public Flux<ObjectResponseData> getHandoverObjectsDetails(List<String> objectIds) {
		return findObjectValues(objectIds, H_O_ATTRIBUTES);
	}


	private Flux<ObjectResponseData> findObjectValues(List<String> objectIds, List<String> attributes) {
		return client.findObjectValues(new ObjectValuesPublicRequest(objectIds, attributes, Collections.emptyList()))
				.subscribeOn(Schedulers.parallel())
				.flatMapIterable(ObjectValuesPublicResponse::getObjects);
	}
}