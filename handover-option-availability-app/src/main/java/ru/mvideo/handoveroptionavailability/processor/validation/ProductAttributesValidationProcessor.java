package ru.mvideo.handoveroptionavailability.processor.validation;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;
import ru.mvideo.handoveroptionavailability.model.HandoverOption;
import ru.mvideo.handoveroptionavailability.model.external.SapAttribute;
import ru.mvideo.handoveroptionavailability.processor.BaseProcessor;
import ru.mvideo.handoveroptionavailability.processor.filter.fit.ExtendedProduct;
import ru.mvideo.handoveroptionavailability.processor.model.Context;
import ru.mvideo.product.model.ProductDto;

@Component
@RequiredArgsConstructor
public class ProductAttributesValidationProcessor<T extends Context> extends BaseProcessor<T> {

	@Override
	protected Mono<T> executeProcessor(T context) {
		return Mono.fromCallable(() -> {
			for (ExtendedProduct product : context.products()) {
				if (product.getProduct().getComplementMaterials() == null && missedDimensionAttributes(product.getProduct())) {
					final var reason = String.format(
							"Product %s does not contain the required attributes", product.getProduct().getProductId());
					context.disableOption(HandoverOption.ETA_DELIVERY.getValue(), reason);
					context.disableOption(HandoverOption.EXACTLY_TIME_DELIVERY.getValue(), reason);
					context.disableOption(HandoverOption.PICKUP_SEAMLESS.getValue(), reason);
					context.disableOption(HandoverOption.PICKUP_PARTNER.getValue(), reason);
				}
			}
			return context;
		});
	}

	@Override
	public boolean shouldRun(T context) {
		return context.hasOption(HandoverOption.EXACTLY_TIME_DELIVERY.getValue())
				|| context.hasOption(HandoverOption.ETA_DELIVERY.getValue())
				|| context.hasOption(HandoverOption.PICKUP_PARTNER.getValue())
				|| context.hasOption(HandoverOption.PICKUP_SEAMLESS.getValue());
	}

	private boolean missedDimensionAttributes(ProductDto product) {
		final var sapAttributes = product.getSapAttributes();
		if (sapAttributes == null) {
			return true;
		}

		final var constants = sapAttributes.getConstants();
		if (constants == null) {
			return true;
		}

		return constants.get(SapAttribute.DIMENSION_MEASURE) == null
				|| constants.get(SapAttribute.LENGTH) == null
				|| constants.get(SapAttribute.WIDTH) == null
				|| constants.get(SapAttribute.HEIGHT) == null
				|| constants.get(SapAttribute.WEIGHT_MEASURE) == null
				|| constants.get(SapAttribute.WEIGHT) == null;
	}
}
