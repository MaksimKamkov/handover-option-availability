package ru.mvideo.handoveroptionavailability.processor.filter;

import java.time.LocalTime;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import ru.mvideo.handoveroptionavailability.model.HandoverOption;
import ru.mvideo.handoveroptionavailability.model.RetailBrand;
import ru.mvideo.handoveroptionavailability.processor.BaseProcessor;
import ru.mvideo.handoveroptionavailability.processor.model.Context;
import ru.mvideo.handoveroptionavailability.processor.model.HandoverObject;

@Component
@RequiredArgsConstructor
public class HandoverObjectTimeZoneFilterProcessor<T extends Context> extends BaseProcessor<T> {
	@Override
	protected Mono<T> executeProcessor(T context) {
		return Mono.fromSupplier(context::handoverOptionContext)
				.map(contextMap -> contextMap.get(HandoverOption.ETA_DELIVERY.getValue()))
				.flatMap(optionContext -> Flux.fromIterable(optionContext.getHandoverObjects())
						.filter(this::isDeliveryTimeExists)
						.filter(object -> {
							var requestTime = LocalTime.now(object.getTimeZone());
							return requestTime.isAfter(object.getDeliveryStartTime())
									&& requestTime.isBefore(object.getDeliveryEndTime());
						})
						.collectList()
						.doOnNext(optionContext::setHandoverObjects)
				)
				.thenReturn(context);
	}

	@Override
	public boolean shouldRun(T context) {
		return RetailBrand.MVIDEO.equals(context.retailBrand()) && context.hasOption(HandoverOption.ETA_DELIVERY.getValue());
	}

	private boolean isDeliveryTimeExists(HandoverObject object) {
		return object.getTimeZone() != null && object.getDeliveryStartTime() != null && object.getDeliveryEndTime() != null;
	}
}
