package ru.mvideo.handoveroptionavailability.processor.context;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;
import ru.mvideo.handoveroptionavailability.processor.BaseProcessor;
import ru.mvideo.handoveroptionavailability.processor.model.Context;
import ru.mvideo.handoveroptionavailability.service.external.zone.ZoneInfoService;

@RequiredArgsConstructor
@Component
public class LoadZoneProcessor<T extends Context> extends BaseProcessor<T> {

	private final ZoneInfoService zoneInfo;

	@Override
	protected Mono<T> executeProcessor(T context) {
		final var regionId = context.regionId();
		final var brand = context.retailBrand();

		return zoneInfo.getIncludedZones(regionId, brand.getValue())
				.publishOn(Schedulers.parallel())
				.map(zones -> {
					context.zones(zones);
					return context;
				});
	}

	@Override
	public boolean shouldRun(T context) {
		return true;
	}
}
