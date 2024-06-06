package ru.mvideo.handoveroptionavailability.service.external.geocoder;

import java.util.Set;
import lombok.RequiredArgsConstructor;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;
import ru.mvideo.handoveroptionavailability.exception.FailedToGetDeliveryCoordinatesException;
import ru.mvideo.lards.geocoder.api.client.YandexGeocoderPublicApi;
import ru.mvideo.lards.geocoder.api.model.response.GeocoderResponse;
import ru.mvideo.lards.geospatial.model.GeoPoint;

@Service
@RequiredArgsConstructor
public class GeoCoderService {

	private static final String CLIENT_FOR_GEOCODER = "DDS";
	private static final Set<String> VALID_KINDS = Set.of("house", "entrance");
	private static final Set<String> VALID_PRECISIONS = Set.of("exact", "number", "near", "range");

	private final YandexGeocoderPublicApi client;

	public Mono<GeoPoint> convertAddressToCoordinates(String address) {
		return client.getPoint(address, CLIENT_FOR_GEOCODER)
				.map(this::toGeoPoint);
	}

	private GeoPoint toGeoPoint(GeocoderResponse response) {
		if (CollectionUtils.isEmpty(response.getGeoObjects())) {
			throw new FailedToGetDeliveryCoordinatesException(response.getRequest());
		}

		final var object = response.getGeoObjects().get(0);
		if (VALID_KINDS.contains(object.getKind()) && VALID_PRECISIONS.contains(object.getPrecision())) {
			final var coordinate = object.getPoint().split(" ");
			return new GeoPoint(Double.parseDouble(coordinate[1]), Double.parseDouble(coordinate[0]));
		}

		throw new FailedToGetDeliveryCoordinatesException(response.getRequest());
	}

}
