package ru.mvideo.handoveroptionavailability.service.external.deliverytimecalculation;

import reactor.core.publisher.Mono;
import ru.mvideo.deliverytimecalculation.model.DeliveryTimeResponse;
import ru.mvideo.lards.geospatial.model.GeoPoint;

public interface DeliveryTimeCalculationService {

	Mono<DeliveryTimeResponse> getDeliveryTime(GeoPoint sourceCoordinates, GeoPoint recipientCoordinates);
}
