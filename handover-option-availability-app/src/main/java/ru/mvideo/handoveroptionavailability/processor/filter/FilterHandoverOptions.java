package ru.mvideo.handoveroptionavailability.processor.filter;

import java.util.Collection;
import java.util.Set;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;
import ru.mvideo.handoveroptionavailability.processor.BaseProcessor;
import ru.mvideo.handoveroptionavailability.processor.model.Context;
import ru.mvideo.lards.handover.option.model.HandoverOption;
import ru.mvideo.lards.handover.option.model.ZoneProviderAttribute;

@RequiredArgsConstructor
@Component
public class FilterHandoverOptions<T extends Context> extends BaseProcessor<T> {

	@Override
	protected Mono<T> executeProcessor(T context) {
		return Mono.fromCallable(() -> {
			final var options = context.providers().stream()
					.flatMap(provider -> provider.getHandoverOptions().stream()
							.map(HandoverOption::getName))
					.collect(Collectors.toSet());

			//Если в ответе не вернулась запрашиваемая опция, то она отфильтровывается и в дальнейших расчетах не участвует.
			for (String handoverOption : context.options()) {
				if (!options.contains(handoverOption)) {
					context.disableOption(handoverOption, "Providers do not support delivery type");
				}
			}

			//Если не вернулась запрашиваемая зона, то она отфильтровывается и в дальнейших расчетах не участвует
			final var zones = context.providers().stream()
					.flatMap(provider -> provider.getZoneIds().stream())
					.collect(Collectors.toSet());
			context.zoneIds(zones);

			if (options.contains(ru.mvideo.handoveroptionavailability.model.HandoverOption.PICKUP_PARTNER.getValue())) {
				final var sapCodes = getSapCodes(context);
				context.sapCodes().addAll(sapCodes);
			}

			return context;
		}).subscribeOn(Schedulers.parallel());
	}

	@Override
	public boolean shouldRun(T context) {
		return true;
	}


	private Set<String> getSapCodes(T context) {
		return context.providers().stream()
				.map(provider -> provider.getZoneProviderAttributes().stream()
						.filter(attribute -> "virtualWarehouse".equals(attribute.getName()))
						.map(ZoneProviderAttribute::getValue)
						.toList()
				).flatMap(Collection::stream)
				.collect(Collectors.toSet());
	}
}