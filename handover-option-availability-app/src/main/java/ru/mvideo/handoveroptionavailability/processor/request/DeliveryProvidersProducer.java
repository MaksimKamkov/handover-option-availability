package ru.mvideo.handoveroptionavailability.processor.request;

import java.util.List;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebInputException;
import reactor.core.publisher.Mono;
import ru.mvideo.handoveroptionavailability.model.DeliveryProvidersRequestV2;
import ru.mvideo.handoveroptionavailability.model.HandoverOption;
import ru.mvideo.handoveroptionavailability.model.LocationDescriptionV2;
import ru.mvideo.handoveroptionavailability.processor.model.DeliveryProvidersContext;
import ru.mvideo.handoveroptionavailability.utils.RequestValidationUtils;

@Component
public class DeliveryProvidersProducer implements ContextProducer<DeliveryProvidersRequestV2, DeliveryProvidersContext> {

	private static final List<HandoverOption> OPTIONS = List.of(
			HandoverOption.ETA_DELIVERY,
			HandoverOption.EXACTLY_TIME_DELIVERY,
			HandoverOption.PICKUP_SEAMLESS
	);

	@Override
	public Mono<DeliveryProvidersContext> produce(DeliveryProvidersRequestV2 request) {
		return Mono.fromCallable(() -> {

			final var materials = request.getMaterials();
			RequestValidationUtils.requireUniqueMaterials(materials);

			validateLocationDescription(request.getSource());
			validateLocationDescription(request.getRecipient());

			final var options = options(OPTIONS);
			final var handoverOptionContext = optionContexts(options);

			return DeliveryProvidersContext.builder()
					.requestHandoverOptions(options)
					.handoverOptionContext(handoverOptionContext)
					.retailBrand(request.getRetailBrand())
					.handoverOptionMaterial(request.getHandoverOptionMaterial())
					.materials(materials)
					.recipient(request.getRecipient())
					.source(request.getSource())
					.build();
		});
	}

	private void validateLocationDescription(LocationDescriptionV2 locationDescription) {
		if (locationDescription.getObjectId() == null) {
			if (locationDescription.getAddress() == null) {
				throw new ServerWebInputException("Отсутствуют адрес, координаты и идентификатор магазина");
			} else if (locationDescription.getAddress().getCoordinate() == null
					&& locationDescription.getAddress().getRepresentation() == null) {
				throw new ServerWebInputException("Отсутствуют адрес, координаты и идентификатор магазина");
			}
		}
	}
}
