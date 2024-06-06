package ru.mvideo.handoveroptionavailability.processor.response;

import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;
import ru.mvideo.handoveroptionavailability.model.DeliveryProvidersResponse;
import ru.mvideo.handoveroptionavailability.model.DeliveryProvidersResponseItem;
import ru.mvideo.handoveroptionavailability.processor.model.DeliveryProvidersContext;
import ru.mvideo.lards.handover.option.model.ProviderZoneAttributesHandoverOptions;

@Component
@RequiredArgsConstructor
public class DeliveryProvidersResponseProducer implements ResponseProducer<Mono<DeliveryProvidersResponse>, DeliveryProvidersContext> {

	@Override
	public Mono<DeliveryProvidersResponse> produce(DeliveryProvidersContext context) {

		final List<DeliveryProvidersResponseItem> providerPriorities = context.providers().stream()
				.map(this::toDeliveryProvidersResponseItem)
				.filter(Objects::nonNull)
				.collect(Collectors.toList());

		final var deliveryProvidersResponse = new DeliveryProvidersResponse();
		deliveryProvidersResponse.setOptionProviders(providerPriorities);

		return Mono.just(deliveryProvidersResponse);
	}

	private DeliveryProvidersResponseItem toDeliveryProvidersResponseItem(ProviderZoneAttributesHandoverOptions provider) {

		//в рамках одного провайдера может быть несколько значений приоритетов на разные зоны
		final var zoneProviderPriorityAttribute = provider.getZoneProviderAttributes().stream()
				.filter(attribute -> "supplierPriority".equals(attribute.getName()))
				.findAny();// выбираем приоритет на случайную зону

		if (zoneProviderPriorityAttribute.isEmpty()) {
			return null;
		}

		final var deliveryProvidersResponseItem = new DeliveryProvidersResponseItem();
		deliveryProvidersResponseItem.setProvider(provider.getProviderName());
		deliveryProvidersResponseItem.setPriority(Integer.parseInt(zoneProviderPriorityAttribute.get().getValue()));
		return deliveryProvidersResponseItem;
	}
}
