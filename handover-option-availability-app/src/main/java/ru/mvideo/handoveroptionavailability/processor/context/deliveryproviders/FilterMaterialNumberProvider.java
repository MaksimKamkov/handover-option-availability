package ru.mvideo.handoveroptionavailability.processor.context.deliveryproviders;

import java.util.Collection;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;
import ru.mvideo.handoveroptionavailability.processor.BaseProcessor;
import ru.mvideo.handoveroptionavailability.processor.model.DeliveryProvidersContext;
import ru.mvideo.lards.handover.option.model.HandoverOption;
import ru.mvideo.lards.handover.option.model.ProviderZoneAttributesHandoverOptions;

@Slf4j
@Component
@RequiredArgsConstructor
public class FilterMaterialNumberProvider extends BaseProcessor<DeliveryProvidersContext> {

	@Override
	protected Mono<DeliveryProvidersContext> executeProcessor(DeliveryProvidersContext context) {
		return Mono.fromCallable(() -> {

			final var handoverOptionMaterial = context.getHandoverOptionMaterial();

			final var providers = context.providers().stream()
					.filter(provider -> provider.getHandoverOptions().stream()
							.anyMatch(option -> context.getHandoverOptionMaterial().equals(option.getMaterialNumber())))
					.collect(Collectors.toList());
			context.providers(providers);

			final var options = providers.stream()
					.map(ProviderZoneAttributesHandoverOptions::getHandoverOptions)
					.flatMap(Collection::stream)
					.collect(Collectors.toSet());

			for (HandoverOption option : options) {
				if (!handoverOptionMaterial.equals(option.getMaterialNumber())) {
					context.disableOption(option.getName(), "Другое значение от handoverOptionMaterial из запроса");
				}
			}

			return context;
		});
	}

	@Override
	public boolean shouldRun(DeliveryProvidersContext context) {
		return true;
	}
}
