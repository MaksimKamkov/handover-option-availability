package ru.mvideo.handoveroptionavailability.processor.context;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;
import ru.mvideo.handoveroptionavailability.model.PartnerBrand;
import ru.mvideo.handoveroptionavailability.processor.BaseProcessor;
import ru.mvideo.handoveroptionavailability.processor.model.BriefAndPickupContext;
import ru.mvideo.handoveroptionavailability.processor.model.Context;
import ru.mvideo.handoveroptionavailability.service.external.providers.ProvidersService;
import ru.mvideo.lards.zone.model.ZoneResponse;

@RequiredArgsConstructor
@Component
public class LoadProvidersProcessor<T extends Context> extends BaseProcessor<T> {

	private final ProvidersService providersService;

	@Override
	protected Mono<T> executeProcessor(T context) {
		return Mono.defer(() -> {
			// передача идентификаторов зон и + regionId из запроса
			final var zones = context.zones().stream()
					.map(ZoneResponse::getId)
					.collect(Collectors.toSet());
			zones.add(context.regionId());

			//опции получения товара которые остались к моменту вызова
			final var handoverOptions = new HashSet<>(context.options());
			final var pickupPointBrands = collectPickupPointsBrands(context);
			return providersService.findProviders(zones, handoverOptions, pickupPointBrands)
					.publishOn(Schedulers.parallel())
					.map(providers -> {
						context.providers(providers);
						return context;
					});
		}).subscribeOn(Schedulers.boundedElastic());
	}

	private Set<String> collectPickupPointsBrands(T context) {
		if (!(context instanceof BriefAndPickupContext)) {
			return Collections.emptySet();
		}

		final var partnerBrands = ((BriefAndPickupContext) context).pickupPointBrands();
		if (CollectionUtils.isEmpty(partnerBrands)) {
			return Collections.emptySet();
		}

		return partnerBrands.stream()
				.map(PartnerBrand::name)
				.collect(Collectors.toSet());
	}

	@Override
	public boolean shouldRun(T context) {
		return true;
	}
}
