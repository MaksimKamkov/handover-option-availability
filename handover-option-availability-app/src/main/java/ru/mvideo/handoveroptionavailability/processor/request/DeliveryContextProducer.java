package ru.mvideo.handoveroptionavailability.processor.request;

import java.util.List;
import java.util.stream.Collectors;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;
import ru.mvideo.handoveroptionavailability.exception.HandoverOptionAvailabilityValidationException;
import ru.mvideo.handoveroptionavailability.model.DeliveryRequestV2;
import ru.mvideo.handoveroptionavailability.model.HandoverOption;
import ru.mvideo.handoveroptionavailability.model.Material;
import ru.mvideo.handoveroptionavailability.model.PaymentMethod;
import ru.mvideo.handoveroptionavailability.model.StockMaterial;
import ru.mvideo.handoveroptionavailability.processor.model.DeliveryContext;
import ru.mvideo.handoveroptionavailability.processor.model.Flags;
import ru.mvideo.handoveroptionavailability.utils.RequestValidationUtils;

@Component
public class DeliveryContextProducer implements ContextProducer<DeliveryRequestV2, DeliveryContext> {

	private static final List<HandoverOption> DEFAULT_DELIVERY_OPTIONS = List.of(
			HandoverOption.ETA_DELIVERY,
			HandoverOption.EXACTLY_TIME_DELIVERY,
			HandoverOption.ELECTRONIC_DELIVERY,
			HandoverOption.DPD_DELIVERY,
			HandoverOption.INTERVAL_DELIVERY
	);

	private static final List<HandoverOption> REQUIRED_ADDRESS_OPTIONS = List.of(
			HandoverOption.ETA_DELIVERY,
			HandoverOption.EXACTLY_TIME_DELIVERY
	);

	@Override
	public Mono<DeliveryContext> produce(DeliveryRequestV2 request) {
		return Mono.fromCallable(() -> prepareContext(request));
	}

	private DeliveryContext prepareContext(DeliveryRequestV2 request) {
		final var materials = request.getMaterials();
		RequestValidationUtils.requireUniqueMaterials(materials);

		if (request.getHandoverOption() == null) {
			request.setHandoverOption(DEFAULT_DELIVERY_OPTIONS);
		}

		var handoverOptions = request.getHandoverOption();

		if (isAddressRequired(handoverOptions) && request.getAddress() == null) {
			handoverOptions = handoverOptions.stream()
					.filter(o -> !REQUIRED_ADDRESS_OPTIONS.contains(o))
					.collect(Collectors.toList());
			if (handoverOptions.isEmpty()) {
				throw new HandoverOptionAvailabilityValidationException("Отсутствуют адрес и координаты", 1001);
			}
		}
		validateDeliveryAndStockObjects(request);

		final var destination = request.getAddress() != null ? request.getAddress().getRepresentation() : null;
		final var coordinate = request.getAddress() != null ? request.getAddress().getCoordinate() : null;

		final var options = options(handoverOptions);
		final var handoverOptionContext = optionContexts(options);

		final var context = DeliveryContext.builder()
				.requestHandoverOptions(options)
				.handoverOptionContext(handoverOptionContext)
				.regionId(request.getRegionId())
				.retailBrand(request.getRetailBrand())
				.materials(materials)
				.destination(destination)
				.coordinatePoint(coordinate)
				.stockHandoverObjects(request.getHandoverObjects())
				.stockObjects(request.getStockObjects())
				.build();

		if (request.getIncludeStocks()) {
			context.addFlag(Flags.INCLUDE_STOCKS);
		}

		if (Boolean.TRUE.equals(request.getReturnPaymentConditions())) {
			context.addFlag(Flags.RETURN_PAYMENT_CONDITIONS);
		}

		if (request.getPaymentMethod() != null) {
			context.paymentMethod(request.getPaymentMethod().getValue());
		}

		return context;
	}

	private void validateDeliveryAndStockObjects(DeliveryRequestV2 request) {
		var handoverObjects = request.getHandoverObjects();
		var stockObjects = request.getStockObjects();
		if (CollectionUtils.isEmpty(handoverObjects) && CollectionUtils.isEmpty(stockObjects)) {
			return;
		}
		if (CollectionUtils.isEmpty(handoverObjects)) {
			throw new HandoverOptionAvailabilityValidationException("'handoverObjects' обязателен, если передан атрибут 'stockObjects'", 1001);
		} else if (CollectionUtils.isEmpty(stockObjects)) {
			throw new HandoverOptionAvailabilityValidationException("'stockObjects' обязателен, если передан атрибут 'handoverObjects'", 1001);
		}
		if (PaymentMethod.CREDIT == request.getPaymentMethod()) {
			throw new HandoverOptionAvailabilityValidationException(
					"'paymentMethod' == CREDIT не совместим с атрибутами 'handoverObjects', 'stockObjects'", 1001);
		}
		if (CollectionUtils.containsAny(request.getHandoverOption(), HandoverOption.ETA_DELIVERY, HandoverOption.EXACTLY_TIME_DELIVERY)) {
			if (handoverObjects.size() != 1 || stockObjects.size() != 1 || !handoverObjects.contains(stockObjects.get(0).getStock())) {
				throw new HandoverOptionAvailabilityValidationException("Для опций 'eta-delivery' и 'exactly-time-delivery' "
						+ "в запросе должен быть передан один и тот же объект в единичном количестве "
						+ "в атрибутах 'handoverObjects/' и 'stockObjects/'", 1001);
			}
		}
		var rootMaterialIds = request.getMaterials().stream()
				.map(Material::getMaterial)
				.collect(Collectors.toSet());
		var stockMaterialsIds = stockObjects.stream()
				.flatMap(stockObject -> stockObject.getMaterials().stream())
				.map(StockMaterial::getMaterial)
				.collect(Collectors.toSet());
		if (!rootMaterialIds.containsAll(stockMaterialsIds)) {
			throw new HandoverOptionAvailabilityValidationException(
					"Поля 'materials/' и 'stockObjects/materials/' должны содержать одинаковые наименования материалов", 1001);
		}
	}

	private boolean isAddressRequired(List<HandoverOption> handoverOptions) {
		return handoverOptions.contains(HandoverOption.ETA_DELIVERY) || handoverOptions.contains(HandoverOption.EXACTLY_TIME_DELIVERY);
	}

}
