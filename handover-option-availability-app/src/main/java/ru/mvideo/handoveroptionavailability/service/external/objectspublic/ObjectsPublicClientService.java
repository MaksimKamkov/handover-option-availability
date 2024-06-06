package ru.mvideo.handoveroptionavailability.service.external.objectspublic;

import java.util.List;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import ru.mvideo.lards.geospatial.model.GeoPoint;
import ru.mvideo.oi.pbl.model.ObjectResponseData;

public interface ObjectsPublicClientService {

	Mono<GeoPoint> getCoordinateHandoverObject(String objectId);

	Flux<ObjectResponseData> getHandoverObjectsDetails(List<String> objectIds);
}