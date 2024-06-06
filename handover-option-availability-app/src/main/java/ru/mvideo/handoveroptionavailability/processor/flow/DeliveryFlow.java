package ru.mvideo.handoveroptionavailability.processor.flow;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;
import ru.mvideo.handoveroptionavailability.processor.context.AvailabilityOptionsIndependentServiceProcessor;
import ru.mvideo.handoveroptionavailability.processor.context.AvailabilityOptionsProcessor;
import ru.mvideo.handoveroptionavailability.processor.context.CatalogComplementProcessor;
import ru.mvideo.handoveroptionavailability.processor.context.CatalogProcessor;
import ru.mvideo.handoveroptionavailability.processor.context.GeoPointProcessor;
import ru.mvideo.handoveroptionavailability.processor.context.HandoverObjectAttributesProcessor;
import ru.mvideo.handoveroptionavailability.processor.context.LoadMinPriceRulesProcessor;
import ru.mvideo.handoveroptionavailability.processor.context.LoadProvidersProcessor;
import ru.mvideo.handoveroptionavailability.processor.context.LoadRegionalZoneAttributesProcessor;
import ru.mvideo.handoveroptionavailability.processor.context.LoadZoneProcessor;
import ru.mvideo.handoveroptionavailability.processor.context.delivery.DeliveryTimeProcessor;
import ru.mvideo.handoveroptionavailability.processor.context.delivery.PickupObjectsInRadiusProcessor;
import ru.mvideo.handoveroptionavailability.processor.filter.AvailabilityOptionDatesCalculator;
import ru.mvideo.handoveroptionavailability.processor.filter.AvailabilityOptionFilter;
import ru.mvideo.handoveroptionavailability.processor.filter.DpdProductFilter;
import ru.mvideo.handoveroptionavailability.processor.filter.FilterHandoverOptions;
import ru.mvideo.handoveroptionavailability.processor.filter.FilterZonesByCoordinateProcessor;
import ru.mvideo.handoveroptionavailability.processor.filter.HandoverObjectTimeZoneFilterProcessor;
import ru.mvideo.handoveroptionavailability.processor.filter.MinRuleProcessor;
import ru.mvideo.handoveroptionavailability.processor.filter.ObjectsInDeliveryRadiusProcessor;
import ru.mvideo.handoveroptionavailability.processor.filter.StocksAndShowcaseProcessor;
import ru.mvideo.handoveroptionavailability.processor.filter.fit.KnapsackProblemProcessor;
import ru.mvideo.handoveroptionavailability.processor.filter.payment.FilterOptionsProcessor;
import ru.mvideo.handoveroptionavailability.processor.filter.payment.MspCreditProcessor;
import ru.mvideo.handoveroptionavailability.processor.filter.payment.conditions.MappingPaymentMethodProcessor;
import ru.mvideo.handoveroptionavailability.processor.filter.payment.conditions.MappingWithFilterPaymentMethodProcessor;
import ru.mvideo.handoveroptionavailability.processor.filter.payment.credit.FilterCreditConditionProcessor;
import ru.mvideo.handoveroptionavailability.processor.filter.payment.credit.NoDataForCreditProcessor;
import ru.mvideo.handoveroptionavailability.processor.filter.payment.credit.ZoneApprovalLeadTimeProcessor;
import ru.mvideo.handoveroptionavailability.processor.model.DeliveryContext;
import ru.mvideo.handoveroptionavailability.processor.validation.FindUnsupportedOptionsProcessor;
import ru.mvideo.handoveroptionavailability.processor.validation.FindUnsupportedProductsProcessor;
import ru.mvideo.handoveroptionavailability.processor.validation.OrderQtyValidationProcessor;
import ru.mvideo.handoveroptionavailability.processor.validation.ProductAttributesValidationProcessor;

@RequiredArgsConstructor
@Component
public class DeliveryFlow implements Flow<DeliveryContext> {

	private final LoadZoneProcessor<DeliveryContext> loadZoneProcessor;
	private final LoadProvidersProcessor<DeliveryContext> loadProvidersProcessor;
	private final LoadMinPriceRulesProcessor<DeliveryContext> loadMinPriceRulesProcessor;
	private final LoadRegionalZoneAttributesProcessor<DeliveryContext> loadRegionalZoneAttributesProcessor;
	private final GeoPointProcessor geoPointProcessor;
	private final CatalogProcessor<DeliveryContext> catalogProcessor;
	private final CatalogComplementProcessor<DeliveryContext> catalogComplementProcessor;
	private final PickupObjectsInRadiusProcessor pickupObjectsInRadiusProcessor;
	private final AvailabilityOptionsProcessor<DeliveryContext> availabilityOptionsProcessor;
	private final AvailabilityOptionsIndependentServiceProcessor<DeliveryContext> availabilityOptionsIndependentServiceProcessor;
	private final KnapsackProblemProcessor<DeliveryContext> knapsackProblemProcessor;

	private final FindUnsupportedProductsProcessor<DeliveryContext> findUnsupportedProductsProcessor;
	private final FindUnsupportedOptionsProcessor<DeliveryContext> findUnsupportedOptionsProcessor;
	private final FilterZonesByCoordinateProcessor filterZonesByCoordinateProcessor;
	private final FilterHandoverOptions<DeliveryContext> filterHandoverOptions;

	private final MinRuleProcessor<DeliveryContext> minRuleProcessor;
	private final ObjectsInDeliveryRadiusProcessor<DeliveryContext> objectsInDeliveryRadiusProcessor;
	private final AvailabilityOptionFilter<DeliveryContext> availabilityOptionFilter;
	private final StocksAndShowcaseProcessor<DeliveryContext> stocksAndShowcaseProcessor;

	private final OrderQtyValidationProcessor<DeliveryContext> orderQtyValidationProcessor;
	private final ProductAttributesValidationProcessor<DeliveryContext> productAttributesValidationProcessor;

	private final DpdProductFilter<DeliveryContext> dpdProductFilter;

	private final HandoverObjectAttributesProcessor<DeliveryContext> handoverObjectAttributesProcessor;
	private final HandoverObjectTimeZoneFilterProcessor<DeliveryContext> handoverObjectTimeZoneFilterProcessor;

	private final AvailabilityOptionDatesCalculator<DeliveryContext> availabilityOptionDatesCalculator;

	private final MappingPaymentMethodProcessor<DeliveryContext> mappingPaymentMethodProcessor;
	private final MappingWithFilterPaymentMethodProcessor<DeliveryContext> mappingWithFilterPaymentMethodProcessor;
	private final FilterOptionsProcessor<DeliveryContext> filterOptionsProcessor;
	private final ZoneApprovalLeadTimeProcessor<DeliveryContext> zoneApprovalLeadTimeProcessor;
	private final FilterCreditConditionProcessor<DeliveryContext> filterCreditConditionProcessor;
	private final NoDataForCreditProcessor<DeliveryContext> noDataForCreditProcessor;
	private final MspCreditProcessor<DeliveryContext> mspCreditProcessor;

	private final DeliveryTimeProcessor deliveryTimeProcessor;

	@Override
	public Mono<DeliveryContext> process(DeliveryContext context) {
		return callChainMain(context)
				.flatMap(availabilityOptionsIndependentServiceProcessor::process)
				.flatMap(knapsackProblemProcessor::process)
				.flatMap(availabilityOptionFilter::process)
				.flatMap(mspCreditProcessor::process)
				.flatMap(stocksAndShowcaseProcessor::process)
				.flatMap(loadMinPriceRulesProcessor::process)
				.flatMap(minRuleProcessor::process)
				.flatMap(objectsInDeliveryRadiusProcessor::process)
				.flatMap(availabilityOptionDatesCalculator::process)
				.flatMap(deliveryTimeProcessor::process);
	}

	public Mono<DeliveryContext> callChainMain(DeliveryContext context) {
		return Mono.zip(
						providersAndGeocoderChain(context)
								.flatMap(pickupObjectsInRadiusProcessor::process)
								.flatMap(handoverObjectAttributesProcessor::process)
								.flatMap(handoverObjectTimeZoneFilterProcessor::process)
								.flatMap(availabilityOptionsProcessor::process),
						productChain(context),
						paymentConditionChain(context).flatMap(tuple -> creditChain(context))
				).map(tuple -> context); // subsequent postprocessing should be skipped if one of the processors returns Mono.empty()
	}

	private Mono<DeliveryContext> productChain(DeliveryContext context) {
		return catalogProcessor.process(context)
				.flatMap(catalogComplementProcessor::process)
				.flatMap(findUnsupportedProductsProcessor::process)
				.flatMap(findUnsupportedOptionsProcessor::process)
				.flatMap(orderQtyValidationProcessor::process)
				.flatMap(productAttributesValidationProcessor::process)
				.flatMap(dpdProductFilter::process);
	}

	private Mono<DeliveryContext> providersAndGeocoderChain(DeliveryContext context) {
		return geoPointProcessor.process(context)
				.flatMap(loadRegionalZoneAttributesProcessor::process)
				.flatMap(loadZoneProcessor::process)
				.flatMap(loadProvidersProcessor::process)
				.flatMap(filterZonesByCoordinateProcessor::process)
				.flatMap(filterHandoverOptions::process);
	}

	private Mono<DeliveryContext> paymentConditionChain(DeliveryContext context) {
		return mappingPaymentMethodProcessor.process(context)
				.flatMap(mappingWithFilterPaymentMethodProcessor::process)
				.flatMap(filterOptionsProcessor::process);
	}

	private Mono<DeliveryContext> creditChain(DeliveryContext context) {
		return zoneApprovalLeadTimeProcessor.process(context)
				.flatMap(filterCreditConditionProcessor::process)
				.flatMap(noDataForCreditProcessor::process)
				.flatMap(filterOptionsProcessor::process);
	}
}
