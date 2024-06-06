package ru.mvideo.handoveroptionavailability.processor.validation;

import java.util.ArrayList;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Predicate;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;
import ru.mvideo.handoveroptionavailability.config.MaterialsProperty;
import ru.mvideo.handoveroptionavailability.model.HandoverOption;
import ru.mvideo.handoveroptionavailability.model.ProductType;
import ru.mvideo.handoveroptionavailability.processor.BaseProcessor;
import ru.mvideo.handoveroptionavailability.processor.filter.fit.ExtendedProduct;
import ru.mvideo.handoveroptionavailability.processor.model.Context;
import ru.mvideo.handoveroptionavailability.processor.model.Flags;
import ru.mvideo.product.model.ProductDto;
import ru.mvideo.product.model.ProductFlagsDto;

@Component
@RequiredArgsConstructor
public class FindUnsupportedOptionsProcessor<T extends Context> extends BaseProcessor<T> {

	private final MaterialsProperty materialsProperty;

	private static final Set<String> HANDOVER_OPTION_WITH_SPLIT_BASKET = Set.of(
			HandoverOption.INTERVAL_DELIVERY.getValue(),
			HandoverOption.DPD_DELIVERY.getValue(),
			HandoverOption.ELECTRONIC_DELIVERY.getValue(),
			HandoverOption.PICKUP.getValue()
	);

	private static final Map<String, List<ProductType>> HANDOVER_OPTION_TYPE = Map.of(
			HandoverOption.ETA_DELIVERY.getValue(), List.of(ProductType.ENDLESS_SHELF, ProductType.COMMON),
			HandoverOption.EXACTLY_TIME_DELIVERY.getValue(), List.of(ProductType.ENDLESS_SHELF, ProductType.COMMON),
			HandoverOption.DPD_DELIVERY.getValue(), List.of(ProductType.COMMON),
			HandoverOption.INTERVAL_DELIVERY.getValue(), List.of(ProductType.ENDLESS_SHELF, ProductType.COMMON),
			HandoverOption.ELECTRONIC_DELIVERY.getValue(), List.of(ProductType.DIGITAL_CODE),
			HandoverOption.PICKUP_SEAMLESS.getValue(), List.of(ProductType.ENDLESS_SHELF, ProductType.COMMON),
			HandoverOption.PICKUP_PARTNER.getValue(), List.of(ProductType.COMMON),
			HandoverOption.PICKUP.getValue(),
			List.of(ProductType.ENDLESS_SHELF, ProductType.COMMON, ProductType.DIGITAL_CODE, ProductType.REQUIRED_MARKING)
	);

	private static final Predicate<ExtendedProduct> INDEPENDENT_SERVICE_PREDICATE = product -> {
		final var flags = product.getProduct().getFlags();
		return Boolean.TRUE.equals(flags.getIsService());
	};

	@Override
	protected Mono<T> executeProcessor(T context) {
		return Mono.fromCallable(() -> {
			final Map<ProductType, List<ExtendedProduct>> productTypes = processProducts(context.products(), context);

			for (String option : HANDOVER_OPTION_WITH_SPLIT_BASKET) {
				final var result = new ArrayList<ExtendedProduct>();
				if (context.options().contains(option)) {
					for (ProductType type : HANDOVER_OPTION_TYPE.get(option)) {
						if (productTypes.containsKey(type)) {
							final var products = productTypes.get(type);
							result.addAll(products);
						}
					}
				}
				if (result.isEmpty()) {
					context.disableOption(option, "Not found materials for delivery option");
				} else {
					final var optionContext = context.handoverOptionContext().get(option);
					optionContext.setProducts(result);
				}
			}

			if (productTypes.containsKey(ProductType.DIGITAL_CODE) || productTypes.containsKey(ProductType.REQUIRED_MARKING)) {
				context.disableOption(HandoverOption.ETA_DELIVERY.getValue(), "Unsupported product type");
				context.disableOption(HandoverOption.EXACTLY_TIME_DELIVERY.getValue(), "Unsupported product type");
				context.disableOption(HandoverOption.PICKUP_SEAMLESS.getValue(), "Unsupported product type");
				context.disableOption(HandoverOption.PICKUP_PARTNER.getValue(), "Unsupported product type");
			}

			if (productTypes.containsKey(ProductType.ENDLESS_SHELF)) {
				context.disableOption(HandoverOption.PICKUP_PARTNER.getValue(), "Unsupported product type");
			}

			return context;
		});
	}

	@Override
	public boolean shouldRun(T context) {
		return true;
	}

	private Map<ProductType, List<ExtendedProduct>> processProducts(List<ExtendedProduct> products, T context) {
		final Map<ProductType, List<ExtendedProduct>> productMap = new EnumMap<>(ProductType.class);
		for (ExtendedProduct product : products) {
			final var productType = defineProductType(product.getProduct());
			if (!productType.equals(ProductType.UNDEFINED)) {
				productMap.computeIfAbsent(productType, k -> new ArrayList<>())
						.add(product);
			}
		}
		if (productMap.isEmpty() && isIndependentServices(products)) {
			productMap.put(ProductType.DIGITAL_CODE, products);
			context.addFlag(Flags.INDEPENDENT_SERVICE);
		}
		return productMap;
	}

	private boolean isIndependentServices(List<ExtendedProduct> products) {
		return products.stream()
				.filter(INDEPENDENT_SERVICE_PREDICATE)
				.count() == products.size();
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
