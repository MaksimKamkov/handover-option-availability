package ru.mvideo.handoveroptionavailability.processor.context.brief;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;
import ru.mvideo.handoveroptionavailability.model.HandoverOption;
import ru.mvideo.handoveroptionavailability.processor.BaseProcessor;
import ru.mvideo.handoveroptionavailability.processor.model.BriefAndPickupContext;
import ru.mvideo.handoveroptionavailability.processor.model.Flags;
import ru.mvideo.handoveroptionavailability.processor.model.HandoverObject;

@RequiredArgsConstructor
@Component
public class HandoverObjectsForPickupProcessor extends BaseProcessor<BriefAndPickupContext> {

	@Override
	protected Mono<BriefAndPickupContext> executeProcessor(BriefAndPickupContext context) {
		return Mono.fromCallable(() -> {
			final var handoverObjects = toHandoverObjects(context.pickupObjectIds());
			context.handoverObjects(handoverObjects);
			final var optionContext = context.handoverOptionContext().get(HandoverOption.PICKUP.getValue());
			optionContext.setHandoverObjects(handoverObjects);
			return context;
		});
	}

	private List<HandoverObject> toHandoverObjects(Set<String> handoverObjectsInRequest) {
		final var result = new ArrayList<HandoverObject>();
		for (String handoverObject : handoverObjectsInRequest) {
			final var object = new HandoverObject();
			object.setObjectId(handoverObject);
			result.add(object);
		}
		return result;
	}

	@Override
	public boolean shouldRun(BriefAndPickupContext context) {
		return context.hasOption(HandoverOption.PICKUP.getValue()) && context.flags().contains(Flags.PICKUP_HANDOVER_OBJECTS);
	}
}
