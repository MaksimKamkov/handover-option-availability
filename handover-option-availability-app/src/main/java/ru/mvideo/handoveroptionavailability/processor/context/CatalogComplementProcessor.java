package ru.mvideo.handoveroptionavailability.processor.context;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;
import ru.mvideo.handoveroptionavailability.exception.HandoverOptionAvailabilityException;
import ru.mvideo.handoveroptionavailability.processor.BaseProcessor;
import ru.mvideo.handoveroptionavailability.processor.filter.fit.ExtendedProduct;
import ru.mvideo.handoveroptionavailability.processor.model.Context;
import ru.mvideo.handoveroptionavailability.service.external.catalog.CatalogService;
import ru.mvideo.product.model.ProductComplementMaterialDto;

@Component
@RequiredArgsConstructor
public class CatalogComplementProcessor<T extends Context> extends BaseProcessor<T> {

	private final CatalogService catalogService;

	@Override
	protected Mono<T> executeProcessor(T context) {
		return Mono.defer(() -> {
			final var productQty = new HashMap<String, Integer>();
			for (ExtendedProduct product : context.products()) {
				final var complementMaterials = product.getProduct().getComplementMaterials();
				if (complementMaterials != null) {
					for (ProductComplementMaterialDto complementMaterial : complementMaterials) {
						productQty.put(complementMaterial.getMaterialNumber(), product.getQty());
					}
				}
			}

			return catalogService.getProductsRaw(new ArrayList<>(productQty.keySet()))
					.publishOn(Schedulers.parallel())
					.map(products -> {
						final var componentIds = new HashSet<>(productQty.keySet());
						products.forEach(product -> componentIds.remove(product.getProductId()));

						if (!componentIds.isEmpty()) {
							throw new HandoverOptionAvailabilityException(
									"Отсутствует номенклатурный номер материала в catalog-service ", 1105);
						}

						final var components = products.stream()
								.map(product -> new ExtendedProduct(product, productQty.get(product.getProductId())))
								.toList();

						context.products().addAll(components);
						return context;
					});
		});
	}

	@Override
	public boolean shouldRun(T context) {
		return context.products().stream().anyMatch(product -> product.getProduct().getComplementMaterials() != null);
	}
}
