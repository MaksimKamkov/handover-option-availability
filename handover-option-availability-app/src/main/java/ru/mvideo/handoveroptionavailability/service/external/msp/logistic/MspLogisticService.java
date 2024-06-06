package ru.mvideo.handoveroptionavailability.service.external.msp.logistic;

import java.util.List;
import java.util.Map;
import java.util.Set;
import reactor.core.publisher.Mono;
import ru.mvideo.handoveroptionavailability.model.Material;
import ru.mvideo.handoveroptionavailability.model.RetailBrand;
import ru.mvideo.handoveroptionavailability.model.StockObject;
import ru.mvideo.handoveroptionavailability.model.external.msp.logistic.HandoverType;
import ru.mvideo.handoveroptionavailability.model.external.msp.logistic.response.AvailabilityOption;

public interface MspLogisticService {

	Mono<List<AvailabilityOption>> fetchAvailabilityCalendar(RetailBrand brand, String regionId,
	                                                         Set<HandoverType> types, List<Material> materials,
	                                                         Set<String> objectsIds, Set<String> sapCodes);

	Mono<List<AvailabilityOption>> fetchAvailabilityCalendarForStockObjects(RetailBrand brand, String regionId,
	                                                         Set<HandoverType> types, Map<String, StockObject> stockObjects,
	                                                         Set<String> objectsIds, Set<String> sapCodes);
}
