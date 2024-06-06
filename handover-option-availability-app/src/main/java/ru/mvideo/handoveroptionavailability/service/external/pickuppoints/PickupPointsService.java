package ru.mvideo.handoveroptionavailability.service.external.pickuppoints;

import java.util.List;
import reactor.core.publisher.Mono;
import ru.mvideo.io.pickup.points.lib.model.response.CellLimit;
import ru.mvideo.io.pickup.points.lib.model.response.PickupPoint;

public interface PickupPointsService {

	Mono<List<PickupPoint>> getPickPoints(List<String> id);

	Mono<List<CellLimit>> warmUpCache();
}
