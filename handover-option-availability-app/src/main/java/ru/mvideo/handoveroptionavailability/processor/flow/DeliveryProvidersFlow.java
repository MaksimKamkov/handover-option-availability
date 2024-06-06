package ru.mvideo.handoveroptionavailability.processor.flow;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;
import ru.mvideo.handoveroptionavailability.processor.context.CatalogComplementProcessor;
import ru.mvideo.handoveroptionavailability.processor.context.CatalogProcessor;
import ru.mvideo.handoveroptionavailability.processor.context.LoadProvidersProcessor;
import ru.mvideo.handoveroptionavailability.processor.context.deliveryproviders.FilterMaterialNumberProvider;
import ru.mvideo.handoveroptionavailability.processor.context.deliveryproviders.FindCoordinateProcessor;
import ru.mvideo.handoveroptionavailability.processor.context.deliveryproviders.IntersectionZoneProcessor;
import ru.mvideo.handoveroptionavailability.processor.context.deliveryproviders.PriceRuleProcessor;
import ru.mvideo.handoveroptionavailability.processor.context.deliveryproviders.ProvidersInDistanceProcessor;
import ru.mvideo.handoveroptionavailability.processor.filter.FilterHandoverOptions;
import ru.mvideo.handoveroptionavailability.processor.filter.fit.KnapsackProblemProcessor;
import ru.mvideo.handoveroptionavailability.processor.model.DeliveryProvidersContext;
import ru.mvideo.handoveroptionavailability.processor.validation.FindUnsupportedOptionsProcessor;
import ru.mvideo.handoveroptionavailability.processor.validation.FindUnsupportedProductsProcessor;
import ru.mvideo.handoveroptionavailability.processor.validation.OrderQtyValidationProcessor;
import ru.mvideo.handoveroptionavailability.processor.validation.ProductAttributesValidationProcessor;

@RequiredArgsConstructor
@Component
public class DeliveryProvidersFlow implements Flow<DeliveryProvidersContext> {

	private final IntersectionZoneProcessor intersectionZoneProcessor;
	private final LoadProvidersProcessor<DeliveryProvidersContext> loadProvidersProcessor;
	private final FilterHandoverOptions<DeliveryProvidersContext> filterHandoverOptions;
	private final FilterMaterialNumberProvider filterMaterialNumberProvider;
	private final PriceRuleProcessor priceRuleProcessor;

	private final CatalogProcessor<DeliveryProvidersContext> catalogProcessor;
	private final CatalogComplementProcessor<DeliveryProvidersContext> catalogComplementProcessor;
	private final FindUnsupportedProductsProcessor<DeliveryProvidersContext> findUnsupportedProductsProcessor;
	private final FindUnsupportedOptionsProcessor<DeliveryProvidersContext> findUnsupportedOptionsProcessor;
	private final OrderQtyValidationProcessor<DeliveryProvidersContext> orderQtyValidationProcessor;
	private final ProductAttributesValidationProcessor<DeliveryProvidersContext> productAttributesValidationProcessor;

	private final FindCoordinateProcessor findCoordinateProcessor;
	private final ProvidersInDistanceProcessor providersInDistanceProcessor;
	private final KnapsackProblemProcessor<DeliveryProvidersContext> knapsackProblemProcessor;

	@Override
	public Mono<DeliveryProvidersContext> process(DeliveryProvidersContext context) {
		return Mono.zip(
				findCoordinateProcessor.process(context).flatMap(tuple -> providersChain(context)),
				productChain(context)
		)
				.flatMap(tuple -> providersInDistanceProcessor.process(context))
				.flatMap(knapsackProblemProcessor::process);
	}

	private Mono<DeliveryProvidersContext> providersChain(DeliveryProvidersContext context) {
		return intersectionZoneProcessor.process(context)
				.flatMap(loadProvidersProcessor::process)
				.flatMap(filterMaterialNumberProvider::process)
				.flatMap(filterHandoverOptions::process)
				.flatMap(priceRuleProcessor::process);
	}

	private Mono<DeliveryProvidersContext> productChain(DeliveryProvidersContext context) {
		return catalogProcessor.process(context)
				.flatMap(catalogComplementProcessor::process)
				.flatMap(findUnsupportedProductsProcessor::process)
				.flatMap(findUnsupportedOptionsProcessor::process)
				.flatMap(orderQtyValidationProcessor::process)
				.flatMap(productAttributesValidationProcessor::process);
	}
}
