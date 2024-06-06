package ru.mvideo.handoveroptionavailability.processor.filter;

import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;
import ru.mvideo.handoveroptionavailability.model.HandoverOption;
import ru.mvideo.handoveroptionavailability.model.external.SapAttribute;
import ru.mvideo.handoveroptionavailability.processor.BaseProcessor;
import ru.mvideo.handoveroptionavailability.processor.model.Context;
import ru.mvideo.product.model.ProductDto;

@Component
@RequiredArgsConstructor
public class DpdProductFilter<T extends Context> extends BaseProcessor<T> {

	@Override
	protected Mono<T> executeProcessor(T context) {
		return Mono.fromCallable(() -> {
			final var optionContext = context.handoverOptionContext().get(HandoverOption.DPD_DELIVERY.getValue());
			final var products = optionContext.getProducts().stream()
					.filter(product -> !missedWeightAttribute(product.getProduct()))
					.collect(Collectors.toList());
			if (products.isEmpty()) {
				context.disableOption(HandoverOption.DPD_DELIVERY.getValue(), "Products do not contain weight attribute");
			} else {
				optionContext.setProducts(products);
			}

			return context;
		});
	}

	@Override
	public boolean shouldRun(T context) {
		return context.hasOption(HandoverOption.DPD_DELIVERY.getValue());
	}

	private boolean missedWeightAttribute(ProductDto product) {
		final var sapAttributes = product.getSapAttributes();
		if (sapAttributes == null) {
			return false;
		}

		final var constants = sapAttributes.getConstants();
		if (constants == null) {
			return false;
		}

		return constants.get(SapAttribute.WEIGHT_MEASURE) == null
				|| constants.get(SapAttribute.WEIGHT) == null;

	}
}
