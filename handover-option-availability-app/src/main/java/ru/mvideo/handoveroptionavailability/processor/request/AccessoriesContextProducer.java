package ru.mvideo.handoveroptionavailability.processor.request;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;
import ru.mvideo.handoveroptionavailability.model.AccessoriesRequest;
import ru.mvideo.handoveroptionavailability.model.HandoverOption;
import ru.mvideo.handoveroptionavailability.processor.filter.fit.ExtendedProduct;
import ru.mvideo.handoveroptionavailability.processor.model.AccessoriesContext;
import ru.mvideo.handoveroptionavailability.utils.RequestValidationUtils;

@Slf4j
@Component
public class AccessoriesContextProducer implements ContextProducer<AccessoriesRequest, AccessoriesContext> {

	private static final List<HandoverOption> ALLOWED_OPTIONS = List.of(
			HandoverOption.INTERVAL_DELIVERY,
			HandoverOption.PICKUP
	);

	@Override
	public Mono<AccessoriesContext> produce(AccessoriesRequest request) {
		return Mono.fromCallable(() -> {
			final var materials = request.getMaterials();
			RequestValidationUtils.requireUniqueMaterials(materials);
			RequestValidationUtils.requireQtyMaterials(materials);

			var handoverOptions = List.of(request.getHandoverOption()).stream()
					.filter(ALLOWED_OPTIONS::contains)
					.toList();

			final var options = options(handoverOptions);
			final var handoverOptionContext = optionContexts(options);

			final var context = AccessoriesContext.builder()
					.requestHandoverOptions(options)
					.handoverOptionContext(handoverOptionContext)
					.regionId(request.getRegionId())
					.retailBrand(request.getRetailBrand())
					.deliveryDate(request.getDeliveryDate())
					.materials(materials)
					.build();

			context.pickupObjectIds().add(request.getHandoverObject());

			setSupportingData(context);

			return context;
		});
	}

	//необходимо для переиспользования процессоров
	private void setSupportingData(AccessoriesContext context) {

		final var products = context.materials().stream()
				.map(ExtendedProduct::new)
				.collect(Collectors.toList());

		context.products(products);
		context.paymentMethod("CREDIT");
		context.providers(new ArrayList<>());

		for (String option : context.options()) {
			final var optionContext = context.handoverOptionContext().get(option);
			optionContext.setPaymentConditions(List.of("CREDIT"));
			optionContext.setProducts(products);
		}
	}
}
