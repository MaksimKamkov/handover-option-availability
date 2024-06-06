package ru.mvideo.handoveroptionavailability.processor.context;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;
import ru.mvideo.handoveroptionavailability.exception.HandoverOptionAvailabilityValidationException;
import ru.mvideo.handoveroptionavailability.processor.BaseProcessor;
import ru.mvideo.handoveroptionavailability.processor.model.Context;
import ru.mvideo.handoveroptionavailability.service.external.zone.ZoneInfoService;

@RequiredArgsConstructor
@Component
public class LoadRegionalZoneAttributesProcessor<T extends Context> extends BaseProcessor<T> {

	private final ZoneInfoService zoneInfo;

	@Override
	protected Mono<T> executeProcessor(T context) {
		final var regionId = context.regionId();
		return zoneInfo.getRegionZoneDetails(regionId)
				.map(zoneDetails -> {
					final var brand = context.retailBrand().getValue();
					if (!regionId.equals(zoneDetails.getId()) || !brand.equals(zoneDetails.getBrand().toString())) {
						throw new HandoverOptionAvailabilityValidationException("Ошибка валидации regionId и retailBrand", 1002);
					}
					context.regionDetails(zoneDetails);
					return context;
				});
	}

	@Override
	public boolean shouldRun(T context) {
		return true;
	}
}
