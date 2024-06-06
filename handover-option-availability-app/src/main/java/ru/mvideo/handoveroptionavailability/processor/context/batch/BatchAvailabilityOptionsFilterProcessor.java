package ru.mvideo.handoveroptionavailability.processor.context.batch;

import static ru.mvideo.handoveroptionavailability.model.HandoverOption.ETA_DELIVERY;
import static ru.mvideo.handoveroptionavailability.model.HandoverOption.EXACTLY_TIME_DELIVERY;
import static ru.mvideo.handoveroptionavailability.model.HandoverOption.PICKUP_SEAMLESS;

import java.util.Collections;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;
import ru.mvideo.handoveroptionavailability.processor.BaseProcessor;
import ru.mvideo.handoveroptionavailability.processor.model.BatchContext;
import ru.mvideo.handoveroptionavailability.processor.model.HandoverObject;

@Component
@RequiredArgsConstructor
public class BatchAvailabilityOptionsFilterProcessor extends BaseProcessor<BatchContext> {

	private static final Set<String> ETA_AND_EXACTLY_TYPES = Set.of(
			ETA_DELIVERY.getValue(),
			EXACTLY_TIME_DELIVERY.getValue(),
			PICKUP_SEAMLESS.getValue()
	);

	@Override
	protected Mono<BatchContext> executeProcessor(BatchContext context) {
		return Mono.fromCallable(() -> {
			for (Map.Entry<String, Set<String>> entry : context.materialHandoverOption().entrySet()) {
				final var material = entry.getKey();
				final var materialOptions = context.availabilityOptions().stream()
						.filter(availabilityOption -> material.equals(availabilityOption.getMaterial()))
						.toList();
				if (materialOptions.isEmpty()) {
					entry.setValue(Collections.emptySet());
				} else {
					entry.getValue().removeIf(option -> {
						if (!ETA_AND_EXACTLY_TYPES.contains(option)) {
							return false;
						}
						Set<String> handoverObjectIds = context.handoverOptionContext().get(option).getHandoverObjects().stream()
								.map(HandoverObject::getObjectId)
								.collect(Collectors.toSet());

						return materialOptions.stream()
								.noneMatch(opt -> handoverObjectIds.contains(opt.getHandoverObject()));
					});
				}
			}
			return context;
		});
	}

	@Override
	public boolean shouldRun(BatchContext context) {
		return true;
	}
}
