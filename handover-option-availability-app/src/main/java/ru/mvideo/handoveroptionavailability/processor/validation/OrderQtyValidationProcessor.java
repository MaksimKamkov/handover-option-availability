package ru.mvideo.handoveroptionavailability.processor.validation;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;
import ru.mvideo.handoveroptionavailability.model.HandoverOption;
import ru.mvideo.handoveroptionavailability.processor.BaseProcessor;
import ru.mvideo.handoveroptionavailability.processor.filter.fit.ExtendedProduct;
import ru.mvideo.handoveroptionavailability.processor.model.Context;

/**
 * Процессор выполняется только в том случае, если в запросе указаны eta-delivery или exactly-time-delivery.
 * В результате выполнения процессора опции могут быть добавлены в список игнорируемых, если не выполняется условие
 * на количество товаров
 */
@Component
@RequiredArgsConstructor
public class OrderQtyValidationProcessor<T extends Context> extends BaseProcessor<T> {

	private static final int MAX_MATERIAL_COUNT = 5;

	@Override
	protected Mono<T> executeProcessor(T context) {
		return Mono.fromCallable(() -> {
			final var totalCount = context.products().stream()
					.filter(product -> product.getProduct().getComplementMaterials() == null)
					.map(ExtendedProduct::getQty)
					.reduce(0, Integer::sum);

			if (totalCount > MAX_MATERIAL_COUNT) {
				final var reason = String.format("The number of materials [%s] exceeds the limit", totalCount);
				context.disableOption(HandoverOption.EXACTLY_TIME_DELIVERY.getValue(), reason);
				context.disableOption(HandoverOption.ETA_DELIVERY.getValue(), reason);
				context.disableOption(HandoverOption.PICKUP_SEAMLESS.getValue(), reason);
				context.disableOption(HandoverOption.PICKUP_PARTNER.getValue(), reason);
			}

			return context;
		});
	}

	@Override
	public boolean shouldRun(T context) {
		return context.hasOption(HandoverOption.EXACTLY_TIME_DELIVERY.getValue())
				|| context.hasOption(HandoverOption.ETA_DELIVERY.getValue())
				|| context.hasOption(HandoverOption.PICKUP_SEAMLESS.getValue())
				|| context.hasOption(HandoverOption.PICKUP_PARTNER.getValue());
	}

}
