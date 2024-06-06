package ru.mvideo.handoveroptionavailability.processor.context;

import java.util.List;
import java.util.function.Function;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;
import ru.mvideo.handoveroptionavailability.exception.HandoverOptionAvailabilityException;
import ru.mvideo.handoveroptionavailability.model.Material;
import ru.mvideo.handoveroptionavailability.processor.BaseProcessor;
import ru.mvideo.handoveroptionavailability.processor.filter.fit.ExtendedProduct;
import ru.mvideo.handoveroptionavailability.processor.model.Context;
import ru.mvideo.handoveroptionavailability.service.external.catalog.CatalogService;
import ru.mvideo.product.model.ProductDto;

@Component
@RequiredArgsConstructor
public class CatalogProcessor<T extends Context> extends BaseProcessor<T> {

	private final CatalogService catalogService;

	@Override
	protected Mono<T> executeProcessor(T context) {
		final var materials = context.materials();
		final var materialIds = materials.stream()
				.map(Material::getMaterial)
				.collect(Collectors.toList());
		return catalogService.getProductsRaw(materialIds)
				.publishOn(Schedulers.parallel())
				.map(products -> {
					products.forEach(product -> materialIds.remove(product.getProductId()));

					if (!materialIds.isEmpty()) {
						throw new HandoverOptionAvailabilityException(
								"Отсутствует номенклатурный номер материала в catalog-service ", 1105);
					}
					context.products(toExtendedProducts(products, materials));
					return context;
				});
	}

	private List<ExtendedProduct> toExtendedProducts(List<ProductDto> products, List<Material> materials) {
		final var productIdMaterials =
				materials.stream()
						.collect(Collectors.toMap(Material::getMaterial, Function.identity()));

		return products.stream()
				.filter(p -> productIdMaterials.containsKey(p.getProductId()))
				.map(product -> {
					final var material = productIdMaterials.get(product.getProductId());
					return new ExtendedProduct(product, material);
				})
				.collect(Collectors.toList());
	}

	@Override
	public boolean shouldRun(T context) {
		return true;
	}
}
