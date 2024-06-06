package ru.mvideo.handoveroptionavailability.processor.request;

import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.collections4.ListUtils;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;
import ru.mvideo.handoveroptionavailability.exception.HandoverOptionAvailabilityValidationException;
import ru.mvideo.handoveroptionavailability.model.HandoverOption;
import ru.mvideo.handoveroptionavailability.model.PartnerBrand;
import ru.mvideo.handoveroptionavailability.model.PaymentMethod;
import ru.mvideo.handoveroptionavailability.model.PickupRequest;
import ru.mvideo.handoveroptionavailability.processor.model.BriefAndPickupContext;
import ru.mvideo.handoveroptionavailability.processor.model.Flags;
import ru.mvideo.handoveroptionavailability.utils.RequestValidationUtils;

@Component
public class PickupContextProducer implements ContextProducer<PickupRequest, BriefAndPickupContext> {

	private static final List<HandoverOption> DEFAULT_PICKUP_OPTIONS = List.of(
			HandoverOption.PICKUP,
			HandoverOption.PICKUP_PARTNER,
			HandoverOption.PICKUP_SEAMLESS
	);

	private static final List<PartnerBrand> DEFAULT_PARTNER_BRANDS = Arrays.asList(PartnerBrand.values());

	@Override
	public Mono<BriefAndPickupContext> produce(PickupRequest request) {
		return Mono.fromCallable(() -> prepareContext(request));
	}

	private BriefAndPickupContext prepareContext(PickupRequest request) {
		final var materials = request.getMaterials();
		RequestValidationUtils.requireUniqueMaterials(materials);
		validateDeliveryAndStockObjects(request);

		if (request.getHandoverOption() == null) {
			request.setHandoverOption(DEFAULT_PICKUP_OPTIONS);
		}

		if (request.getPickupPointBrands() == null) {
			request.setPickupPointBrands(DEFAULT_PARTNER_BRANDS);
		}

		final var handoverOptions = request.getHandoverOption();
		final var options = options(handoverOptions);
		final var handoverOptionContext = optionContexts(options);

		final var context = BriefAndPickupContext.builder()
				.requestHandoverOptions(options)
				.handoverOptionContext(handoverOptionContext)
				.regionId(request.getRegionId())
				.retailBrand(request.getRetailBrand())
				.materials(materials)
				.stockHandoverObjects(new HashSet<>(ListUtils.emptyIfNull(request.getHandoverObjects())))
				.stockObjects(request.getStockObjects())
				.pickupPointBrands(request.getPickupPointBrands())
				.build();

		if (Boolean.TRUE.equals(request.getIncludeStocks())) {
			context.addFlag(Flags.INCLUDE_STOCKS);
		}

		if (request.getHandoverObjects() != null && handoverOptions.contains(HandoverOption.PICKUP)) {
			context.addFlag(Flags.PICKUP_HANDOVER_OBJECTS);
			context.disableOption(HandoverOption.PICKUP_PARTNER.getValue(), "In request exist handover objects");
			context.disableOption(HandoverOption.PICKUP_SEAMLESS.getValue(), "In request exist handover objects");
			context.pickupObjectIds().addAll(request.getHandoverObjects());
		}

		if (CollectionUtils.isNotEmpty(request.getStockObjects())) {
			context.disableOption(HandoverOption.PICKUP_SEAMLESS.getValue(), "In request exist stock objects");
		}

		if (Boolean.TRUE.equals(request.getReturnPaymentConditions())) {
			context.addFlag(Flags.RETURN_PAYMENT_CONDITIONS);
		}

		if (request.getPaymentMethod() != null) {
			context.paymentMethod(request.getPaymentMethod().getValue());
		}

		return context;
	}

	private void validateDeliveryAndStockObjects(PickupRequest request) {
		var handoverObjects = request.getHandoverObjects();
		var stockObjects = request.getStockObjects();
		if (CollectionUtils.isEmpty(handoverObjects) && CollectionUtils.isEmpty(stockObjects)) {
			return;
		}
		if (CollectionUtils.isEmpty(handoverObjects)) {
			throw new HandoverOptionAvailabilityValidationException("'handoverObjects' обязателен, если передан атрибут 'stockObjects'", 1001);
		}
		if (PaymentMethod.CREDIT == request.getPaymentMethod() && CollectionUtils.isNotEmpty(stockObjects)) {
			throw new HandoverOptionAvailabilityValidationException(
					"'paymentMethod' == CREDIT не совместим с атрибутом 'stockObjects'", 1001);
		}
	}
}
