package ru.mvideo.handoveroptionavailability.mapper;

import java.util.List;
import org.mapstruct.Mapper;
import ru.mvideo.handoveroptionavailability.processor.model.HandoverObject;
import ru.mvideo.lards.zone.pickup.item.model.ZoneHandoverPickupObjectResponse;

@Mapper
public interface HandoverObjectMapper {

	default List<HandoverObject> toHandoverObjects(List<ZoneHandoverPickupObjectResponse> zoneHandoverPickupObjects) {
		return zoneHandoverPickupObjects.stream()
				.flatMap(response -> response.getHandoverPickupObjects().stream())
				.map(pickupObject -> HandoverObject.builder()
						.objectId(pickupObject.getObjectId())
						.coordinate(pickupObject.getCoordinate())
						.build()
				)
				.distinct()
				.toList();
	}
}
