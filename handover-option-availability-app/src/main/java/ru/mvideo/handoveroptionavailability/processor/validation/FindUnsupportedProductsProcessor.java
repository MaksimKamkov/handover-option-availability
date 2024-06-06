package ru.mvideo.handoveroptionavailability.processor.validation;

import java.util.List;
import java.util.function.Predicate;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;
import ru.mvideo.handoveroptionavailability.processor.BaseProcessor;
import ru.mvideo.handoveroptionavailability.processor.filter.fit.ExtendedProduct;
import ru.mvideo.handoveroptionavailability.processor.model.Context;

@Slf4j
@Component
public class FindUnsupportedProductsProcessor<T extends Context> extends BaseProcessor<T> {

	private static final Predicate<ExtendedProduct> DIGITAL_CODE_PREDICATE = product -> {
		final var flags = product.getProduct().getFlags();

		return Boolean.TRUE.equals(flags.getIsDigitalCode()) && Boolean.TRUE.equals(flags.getIsDelivery());
	};

	private static final Predicate<ExtendedProduct> GOODS_PREDICATE = product -> {
		final var attributes = product.getProduct().getSapAttributes();

		if (attributes.getConstants() != null) {
			final var constants = attributes.getConstants();
			return constants.containsKey("mc_source") && "GDS".equals(constants.get("mc_source"));
		}

		return false;
	};

	@Override
	protected Mono<T> executeProcessor(T context) {
		return Mono.defer(() -> {
			final var unsupportedMaterials = context.products().stream()
					.filter(product -> product.getProduct().getComplementMaterials() != null)
					.filter(DIGITAL_CODE_PREDICATE)
					.filter(GOODS_PREDICATE)
					.findAny();

			if (unsupportedMaterials.isPresent()) {
				log.warn("Unsupported product [{}]", unsupportedMaterials.get().getProduct().getProductId());
				return Mono.empty();
			}

			final var products = excludeDelivery(context.products());
			if (products.isEmpty()) {
				log.warn("No products satisfying the rules were found, whose attributes were obtained from catalog-service");
				return Mono.empty();
			}
			context.products(products);
			return Mono.just(context);
		}).subscribeOn(Schedulers.parallel());
	}

	private List<ExtendedProduct> excludeDelivery(List<ExtendedProduct> products) {
		return products.stream()
				.filter(product -> {
					final var flags = product.getProduct().getFlags();
					return flags == null || flags.getIsDelivery() == null || Boolean.FALSE.equals(flags.getIsDelivery());
				})
				.toList();
	}

	@Override
	public boolean shouldRun(T context) {
		return true;
	}

}
