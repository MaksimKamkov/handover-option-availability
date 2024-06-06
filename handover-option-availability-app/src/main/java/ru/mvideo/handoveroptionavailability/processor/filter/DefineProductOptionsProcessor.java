package ru.mvideo.handoveroptionavailability.processor.filter;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;
import ru.mvideo.handoveroptionavailability.config.MaterialsProperty;
import ru.mvideo.handoveroptionavailability.model.HandoverOption;
import ru.mvideo.handoveroptionavailability.model.ProductType;
import ru.mvideo.handoveroptionavailability.processor.BaseProcessor;
import ru.mvideo.handoveroptionavailability.processor.filter.fit.ExtendedProduct;
import ru.mvideo.handoveroptionavailability.processor.model.BatchContext;
import ru.mvideo.handoveroptionavailability.processor.model.Flags;
import ru.mvideo.product.model.ProductDto;
import ru.mvideo.product.model.ProductFlagsDto;

@Component
@RequiredArgsConstructor
public class DefineProductOptionsProcessor extends BaseProcessor<BatchContext> {

	private final MaterialsProperty materialsProperty;

	private static final Predicate<ExtendedProduct> INDEPENDENT_SERVICE_PREDICATE = product -> {
		final var flags = product.getProduct().getFlags();
		return Boolean.TRUE.equals(flags.getIsService());
	};

	@Override
	protected Mono<BatchContext> executeProcessor(BatchContext context) {
		return Mono.fromCallable(() -> {
			final Map<String, Set<String>> materialHandoverOptions = new HashMap<>();

			for (ExtendedProduct product : context.products()) {
				final var productType = defineProductType(product.getProduct());

				if (!ProductType.UNDEFINED.equals(productType) && product.getPrice() != null) {
					final var options = defineProductOptions(productType).stream()
							.filter(option -> context.options().contains(option))
							.collect(Collectors.toSet());
					materialHandoverOptions.put(product.getProduct().getProductId(), options);
				}
			}
			if (materialHandoverOptions.isEmpty() && isIndependentServices(context.products())) {
				for (ExtendedProduct product : context.products()) {
					materialHandoverOptions.put(product.getProduct().getProductId(), Set.of(HandoverOption.ELECTRONIC_DELIVERY.getValue()));
				}
				context.addFlag(Flags.INDEPENDENT_SERVICE);
			}
			context.materialHandoverOption(materialHandoverOptions);
			return context;
		});
	}

	@Override
	public boolean shouldRun(BatchContext context) {
		return true;
	}

	private boolean isIndependentServices(List<ExtendedProduct> products) {
		return products.stream()
				.filter(INDEPENDENT_SERVICE_PREDICATE)
				.count() == products.size();
	}

	private Set<String> defineProductOptions(ProductType type) {
		return switch (type) {
			case COMMON -> Set.of(
					HandoverOption.ETA_DELIVERY.getValue(),
					HandoverOption.EXACTLY_TIME_DELIVERY.getValue(),
					HandoverOption.INTERVAL_DELIVERY.getValue(),
					HandoverOption.DPD_DELIVERY.getValue(),
					HandoverOption.PICKUP.getValue(),
					HandoverOption.PICKUP_PARTNER.getValue(),
					HandoverOption.PICKUP_SEAMLESS.getValue());
			case ENDLESS_SHELF -> Set.of(
					HandoverOption.ETA_DELIVERY.getValue(),
					HandoverOption.EXACTLY_TIME_DELIVERY.getValue(),
					HandoverOption.INTERVAL_DELIVERY.getValue(),
					HandoverOption.PICKUP.getValue(),
					HandoverOption.PICKUP_SEAMLESS.getValue());
			case DIGITAL_CODE -> Set.of(HandoverOption.ELECTRONIC_DELIVERY.getValue(), HandoverOption.PICKUP.getValue());
			case REQUIRED_MARKING -> Set.of(HandoverOption.PICKUP.getValue());
			default -> Collections.emptySet();
		};
	}

	private ProductType defineProductType(ProductDto product) {
		final var flags = product.getFlags();
		if (flags != null && Boolean.TRUE.equals(flags.getIsDigitalCode())) {
			return ProductType.DIGITAL_CODE;
		}

		if (product.getGroupIds() != null) {
			final var ignoreMaterials = materialsProperty.getRequiredLabelGroups();
			if (ignoreMaterials.contains(product.getGroupIds().getSapGroupId())) {
				return ProductType.REQUIRED_MARKING;
			}
		}

		final var attributes = product.getSapAttributes();
		if (attributes != null && attributes.getConstants() != null && "US".equals(attributes.getConstants().get("ZUNLSHELF"))) {
			return ProductType.ENDLESS_SHELF;
		}

		if (isCommonProduct(flags)) {
			return ProductType.COMMON;
		}

		return ProductType.UNDEFINED;
	}

	private boolean isCommonProduct(ProductFlagsDto flags) {
		if (flags == null) {
			return true;
		}

		final var isDigitalCode = !(flags.getIsDigitalCode() == null || Boolean.FALSE.equals(flags.getIsDigitalCode()));
		final var isService = !(flags.getIsService() == null || Boolean.FALSE.equals(flags.getIsService()));
		final var isDelivery = !(flags.getIsDelivery() == null || Boolean.FALSE.equals(flags.getIsDelivery()));

		return !Boolean.FALSE.equals(flags.getIsInventoryControl()) && !isDigitalCode && !isService && !isDelivery;
	}
}
