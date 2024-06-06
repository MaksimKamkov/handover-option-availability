package ru.mvideo.handoveroptionavailability.service.external.zone;

import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;
import ru.mvideo.handoveroptionavailability.exception.HandoverOptionAvailabilityException;
import ru.mvideo.handoveroptionavailability.exception.HandoverOptionAvailabilityValidationException;
import ru.mvideo.lards.geospatial.model.GeoPoint;
import ru.mvideo.lards.zone.client.service.ZoneClientService;
import ru.mvideo.lards.zone.model.FindInternalZoneIntersectionRequest;
import ru.mvideo.lards.zone.model.FindZoneByPointRequest;
import ru.mvideo.lards.zone.model.ZoneBrand;
import ru.mvideo.lards.zone.model.ZoneDetailResponse;
import ru.mvideo.lards.zone.model.ZoneResponse;

@Service
@RequiredArgsConstructor
public class ZoneInfoService {

	private final ZoneClientService client;

	public Mono<List<ZoneResponse>> getIncludedZones(String regionId, String brand) {
		var request = new FindInternalZoneIntersectionRequest(false, ZoneBrand.valueOf(brand));
		return client.findSubordinateZones(regionId, request).collectList();
	}

	public Mono<List<ZoneResponse>> getIncludedZonesByCoordinates(GeoPoint geoPoint, String brand) {
		var request = new FindZoneByPointRequest();
		request.setBrand(ZoneBrand.valueOf(brand));
		request.setPoint(geoPoint);

		return client.getZoneByGeoPoint(request)
				.collectList()
				.map(response -> {
					if (response == null || response.isEmpty()) {
						throw new HandoverOptionAvailabilityException("Отсутствуют данные в ответе сервиса Zone-info", 1104);
					}
					return response;
				});
	}

	public Mono<ZoneDetailResponse> getRegionZoneDetails(String regionId) {
		return client.getZoneDetailById(regionId)
				.onErrorResume(fallback -> {
					throw new HandoverOptionAvailabilityValidationException("Ошибка валидации regionId и retailBrand", 1002);
				});
	}
}
