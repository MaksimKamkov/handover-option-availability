package ru.mvideo.handoveroptionavailability.processor.flow;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;
import ru.mvideo.handoveroptionavailability.processor.context.AvailabilityOptionsIndependentServiceProcessor;
import ru.mvideo.handoveroptionavailability.processor.context.CatalogComplementProcessor;
import ru.mvideo.handoveroptionavailability.processor.context.CatalogProcessor;
import ru.mvideo.handoveroptionavailability.processor.context.HandoverObjectAttributesProcessor;
import ru.mvideo.handoveroptionavailability.processor.context.LoadProvidersProcessor;
import ru.mvideo.handoveroptionavailability.processor.context.LoadZoneProcessor;
import ru.mvideo.handoveroptionavailability.processor.context.batch.BatchAvailabilityOptionsFilterProcessor;
import ru.mvideo.handoveroptionavailability.processor.context.batch.BatchAvailabilityOptionsProcessor;
import ru.mvideo.handoveroptionavailability.processor.context.batch.KnapsackBatchProblemProcessor;
import ru.mvideo.handoveroptionavailability.processor.context.batch.LoadBatchMinPriceRulesProcessor;
import ru.mvideo.handoveroptionavailability.processor.context.batch.PickupPointsBatchProcessor;
import ru.mvideo.handoveroptionavailability.processor.context.brief.PickupObjectsProcessor;
import ru.mvideo.handoveroptionavailability.processor.filter.DefineProductOptionsProcessor;
import ru.mvideo.handoveroptionavailability.processor.filter.FilterHandoverOptions;
import ru.mvideo.handoveroptionavailability.processor.filter.HandoverObjectTimeZoneFilterProcessor;
import ru.mvideo.handoveroptionavailability.processor.model.BatchContext;
import ru.mvideo.handoveroptionavailability.processor.validation.FindUnsupportedProductsProcessor;
import ru.mvideo.handoveroptionavailability.processor.validation.ProductAttributesValidationProcessor;

@RequiredArgsConstructor
@Component
public class BatchFlow implements Flow<BatchContext> {

	private final CatalogProcessor<BatchContext> catalogProcessor;
	private final CatalogComplementProcessor<BatchContext> catalogComplementProcessor;
	private final FindUnsupportedProductsProcessor<BatchContext> findUnsupportedProductsProcessor;
	private final ProductAttributesValidationProcessor<BatchContext> productAttributesValidationProcessor;
	private final DefineProductOptionsProcessor defineProductOptionsProcessor;

	private final LoadZoneProcessor<BatchContext> loadZoneProcessor;
	private final LoadProvidersProcessor<BatchContext> loadProvidersProcessor;
	private final FilterHandoverOptions<BatchContext> filterHandoverOptions;

	private final PickupObjectsProcessor<BatchContext> pickupObjectsProcessor;
	private final HandoverObjectAttributesProcessor<BatchContext> handoverObjectAttributesProcessor;
	private final HandoverObjectTimeZoneFilterProcessor<BatchContext> handoverObjectTimeZoneFilterProcessor;

	private final LoadBatchMinPriceRulesProcessor loadPriceRulesProcessor;
	private final KnapsackBatchProblemProcessor knapsackBatchProblemProcessor;
	private final BatchAvailabilityOptionsProcessor availabilityOptionsProcessor;
	private final BatchAvailabilityOptionsFilterProcessor availabilityOptionsFilterProcessor;
	private final PickupPointsBatchProcessor pickupPointsProcessor;
	private final AvailabilityOptionsIndependentServiceProcessor<BatchContext> availabilityOptionsIndependentServiceProcessor;

	@Override
	public Mono<BatchContext> process(BatchContext context) {
		return callChainMain(context).flatMap(pickupPointsProcessor::process);
	}

	public Mono<BatchContext> callChainMain(BatchContext context) {
		return Mono.zip(providersAndPriceRulesChain(context), productChain(context), pickupHandoverObjectsChain(context))
				.flatMap(tuple ->
						Mono.zip(availabilityOptionsProcessor.process(context)
										.flatMap(availabilityOptionsIndependentServiceProcessor::process)
										.flatMap(availabilityOptionsFilterProcessor::process),
								knapsackBatchProblemProcessor.process(context),
								loadPriceRulesProcessor.process(context))
				)
				.then(Mono.just(context));
	}

	private Mono<BatchContext> productChain(BatchContext context) {
		return catalogProcessor.process(context)
				.flatMap(catalogComplementProcessor::process)
				.flatMap(findUnsupportedProductsProcessor::process)
				.flatMap(productAttributesValidationProcessor::process)
				.flatMap(defineProductOptionsProcessor::process);
	}

	private Mono<BatchContext> providersAndPriceRulesChain(BatchContext context) {
		return loadZoneProcessor.process(context)
				.flatMap(loadProvidersProcessor::process)
				.flatMap(filterHandoverOptions::process);
	}

	private Mono<BatchContext> pickupHandoverObjectsChain(BatchContext context) {
		return pickupObjectsProcessor.process(context)
				.flatMap(handoverObjectAttributesProcessor::process)
				.flatMap(handoverObjectTimeZoneFilterProcessor::process);
	}
}
