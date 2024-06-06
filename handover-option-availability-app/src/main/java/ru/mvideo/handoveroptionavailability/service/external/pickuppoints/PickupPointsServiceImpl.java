package ru.mvideo.handoveroptionavailability.service.external.pickuppoints;

import java.util.Collections;
import java.util.List;
import javax.annotation.PostConstruct;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;
import ru.mvideo.io.pickup.points.lib.model.response.CellLimit;
import ru.mvideo.io.pickup.points.lib.model.response.PickupPoint;
import ru.mvideo.io.pickup.points.lib.starter.client.service.CellLimitClient;
import ru.mvideo.io.pickup.points.lib.starter.client.service.PickupPointsClient;

@Slf4j
@Service
@AllArgsConstructor
public class PickupPointsServiceImpl implements PickupPointsService {

	private final PickupPointsClient pickupPointsClient;
	private final CellLimitClient cellLimitClient;

	private List<CellLimit> cellLimits;

	@PostConstruct
	public void init() {
		log.info("Cells cache initialization started");
		warmUpCache().block();
		log.info("Cells cache initialization ended");
	}

	@Scheduled(cron = "0 0 0 * * *")
	public void scheduledWarmUp() {
		log.info("Scheduled cells cache warm up started");
		warmUpCache().block();
		log.info("Scheduled cells cache warm up ended");
	}

	@Override
	public Mono<List<CellLimit>> warmUpCache() {
		return cellLimitClient.findAll()
				.doOnNext(response ->
						this.cellLimits = response
				)
				.onErrorResume(
						Throwable.class,
						fallback -> {
							log.error("Pickup-points service error: {}", fallback.getMessage());
							return Mono.just(Collections.emptyList());
						});
	}

	@Override
	public Mono<List<PickupPoint>> getPickPoints(List<String> pickupPointIds) {
		return pickupPointsClient.findByIds(pickupPointIds);
	}
}
