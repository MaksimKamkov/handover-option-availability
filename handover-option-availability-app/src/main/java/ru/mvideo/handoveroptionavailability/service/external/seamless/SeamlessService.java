package ru.mvideo.handoveroptionavailability.service.external.seamless;

import java.util.Collections;
import java.util.List;
import java.util.Set;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;
import ru.mvideo.availability_chains.model.RelatedObjectsDetails;
import ru.mvideo.availability_chains.model.RelatedObjectsDetailsRequest;
import ru.mvideo.oi.clop.api.client.route.RelatedObjectsClient;

@Slf4j
@Service
@RequiredArgsConstructor
public class SeamlessService {

	private final RelatedObjectsClient client;

	public Mono<List<RelatedObjectsDetails>> getRelatedObjectsListDetails(Set<String> objectIds, double maxDistance) {
		var request = new RelatedObjectsDetailsRequest();
		request.setObjectIds(objectIds);
		request.setMaxDistance(maxDistance);
		request.type(Set.of("TAXI"));

		return client.getRelatedObjectsListDetails(request)
				.collectList()
				.onErrorResume(
						Throwable.class,
						fallback -> {
							log.error("Availability-chains service error: {}", fallback.getMessage());
							return Mono.just(Collections.emptyList());
						});
	}
}
