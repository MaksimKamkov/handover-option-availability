package ru.mvideo.handoveroptionavailability.processor.request;

import java.util.List;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;
import ru.mvideo.handoveroptionavailability.model.BriefRequest;
import ru.mvideo.handoveroptionavailability.model.HandoverOption;
import ru.mvideo.handoveroptionavailability.processor.model.BriefAndPickupContext;
import ru.mvideo.handoveroptionavailability.processor.model.Flags;
import ru.mvideo.handoveroptionavailability.utils.RequestValidationUtils;

@Component
public class BriefContextProducer implements ContextProducer<BriefRequest, BriefAndPickupContext> {

	private static final List<HandoverOption> DEFAULT_OPTIONS = List.of(
			HandoverOption.ETA_DELIVERY,
			HandoverOption.EXACTLY_TIME_DELIVERY,
			HandoverOption.ELECTRONIC_DELIVERY,
			HandoverOption.DPD_DELIVERY,
			HandoverOption.INTERVAL_DELIVERY,
			HandoverOption.PICKUP,
			HandoverOption.PICKUP_PARTNER,
			HandoverOption.PICKUP_SEAMLESS
	);

	@Override
	public Mono<BriefAndPickupContext> produce(BriefRequest request) {
		return Mono.fromCallable(() -> prepareContext(request));
	}

	private BriefAndPickupContext prepareContext(BriefRequest request) {
		final var materials = request.getMaterials();
		RequestValidationUtils.requireUniqueMaterials(materials);

		if (request.getHandoverOption() == null) {
			request.setHandoverOption(DEFAULT_OPTIONS);
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
				.build();

		if (Boolean.TRUE.equals(request.getCachebust())) {
			context.addFlag(Flags.CACHE_BUST);
		}

		if (Boolean.TRUE.equals(request.getIncludePickupObject())) {
			context.addFlag(Flags.INCLUDE_PICKUP_OBJECT);
		}

		if (Boolean.TRUE.equals(request.getReturnPaymentConditions())) {
			context.addFlag(Flags.RETURN_PAYMENT_CONDITIONS);
		}

		if (request.getPaymentMethod() != null) {
			context.paymentMethod(request.getPaymentMethod().getValue());
		}

		return context;
	}
}
