package ru.mvideo.handoveroptionavailability.processor.context;

import java.util.List;
import java.util.Set;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;
import ru.mvideo.handoveroptionavailability.model.HandoverOption;
import ru.mvideo.handoveroptionavailability.processor.BaseProcessor;
import ru.mvideo.handoveroptionavailability.processor.model.Context;
import ru.mvideo.handoveroptionavailability.processor.model.Flags;
import ru.mvideo.handoveroptionavailability.processor.model.HandoverObject;
import ru.mvideo.handoveroptionavailability.service.external.objectspublic.ObjectsPublicClientService;
import ru.mvideo.handoveroptionavailability.utils.ObjectsPublicUtils;
import ru.mvideo.oi.pbl.model.ObjectResponseData;

@Component
@RequiredArgsConstructor
public class HandoverObjectAttributesProcessor<T extends Context> extends BaseProcessor<T> {

	private static final Set<String> PICKUP_TYPES = Set.of(
			HandoverOption.ETA_DELIVERY.getValue(),
			HandoverOption.EXACTLY_TIME_DELIVERY.getValue(),
			HandoverOption.PICKUP.getValue(),
			HandoverOption.PICKUP_SEAMLESS.getValue()
	);

	private final ObjectsPublicClientService objectsPublicClientService;

	@Override
	protected Mono<T> executeProcessor(T context) {
		var handoverObjectsFlux = Flux.fromIterable(context.handoverObjects());
		return handoverObjectsFlux
				.zipWith(handoverObjectsFlux
						.map(HandoverObject::getObjectId)
						.collectList()
						.flatMapMany(objectsPublicClientService::getHandoverObjectsDetails)
						.collectMap(ObjectResponseData::getObjectId)
						.cache()
						.repeat()
				)
				.publishOn(Schedulers.parallel())
				.map(t2 -> {
					var handoverObject = t2.getT1();
					ObjectsPublicUtils.enrichObjectsPublicInfo(t2.getT2().get(handoverObject.getObjectId()), handoverObject);
					return handoverObject;
				})
				.collectList()
				.doOnNext(context::handoverObjects)
				.thenMany(Flux.fromIterable(context.options()))
				.filter(PICKUP_TYPES::contains)
				.map(option -> context.handoverOptionContext().get(option))
				.doOnNext(optionContext -> {
					final var optionObjects = filterOptionObjects(optionContext.getHandoverOption(), context.handoverObjects());
					optionContext.setHandoverObjects(optionObjects);
				})
				.then(Mono.just(context))
				.subscribeOn(Schedulers.boundedElastic());
	}

	private List<HandoverObject> filterOptionObjects(String handoverOption, List<HandoverObject> objects) {
		if (HandoverOption.PICKUP.getValue().equals(handoverOption)) {
			return objects;
		} else {
			return objects.stream()
					.filter(object -> object.getIsAutoCourierAvailable() != null)
					.filter(HandoverObject::getIsAutoCourierAvailable)
					.toList();
		}
	}

	@Override
	public boolean shouldRun(T context) {
		return context.hasOption(HandoverOption.ETA_DELIVERY.getValue())
				|| context.hasOption(HandoverOption.EXACTLY_TIME_DELIVERY.getValue())
				|| context.hasOption(HandoverOption.PICKUP_SEAMLESS.getValue())
				|| (context.hasOption(HandoverOption.PICKUP.getValue()) && !context.flags().contains(Flags.PICKUP_HANDOVER_OBJECTS));
	}
}
