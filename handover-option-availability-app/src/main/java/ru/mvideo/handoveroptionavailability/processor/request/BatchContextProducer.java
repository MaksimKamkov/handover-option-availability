package ru.mvideo.handoveroptionavailability.processor.request;

import java.util.List;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;
import ru.mvideo.handoveroptionavailability.model.BatchRequest;
import ru.mvideo.handoveroptionavailability.model.HandoverOption;
import ru.mvideo.handoveroptionavailability.model.Material;
import ru.mvideo.handoveroptionavailability.model.MaterialWithoutQty;
import ru.mvideo.handoveroptionavailability.processor.model.BatchContext;
import ru.mvideo.handoveroptionavailability.utils.RequestValidationUtils;

@Slf4j
@Component
public class BatchContextProducer implements ContextProducer<BatchRequest, BatchContext> {

	private static final List<HandoverOption> ALLOWED_OPTIONS = List.of(
			HandoverOption.ETA_DELIVERY,
			HandoverOption.ELECTRONIC_DELIVERY,
			HandoverOption.INTERVAL_DELIVERY,
			HandoverOption.PICKUP,
			HandoverOption.PICKUP_PARTNER
	);

	@Override
	public Mono<BatchContext> produce(BatchRequest request) {
		return Mono.fromCallable(() -> {
			final var requestMaterials = request.getMaterials();
			RequestValidationUtils.requireUniqueMaterialsWithoutQty(requestMaterials);

			final var materials = requestMaterials.stream()
					.map(this::materialWithoutQtyToMaterial)
					.toList();

			var handoverOptions = request.getHandoverOption();

			handoverOptions = handoverOptions.stream()
					.filter(ALLOWED_OPTIONS::contains)
					.toList();

			final var options = options(handoverOptions);
			final var handoverOptionContext = optionContexts(options);

			return BatchContext.builder()
					.requestHandoverOptions(options)
					.handoverOptionContext(handoverOptionContext)
					.regionId(request.getRegionId())
					.retailBrand(request.getRetailBrand())
					.materials(materials)
					.build();
		});
	}

	private Material materialWithoutQtyToMaterial(MaterialWithoutQty mat) {
		final var material = new Material();
		material.setMaterial(mat.getMaterial());
		material.setQty(1);
		material.setPrice(mat.getPrice());
		return material;
	}
}
