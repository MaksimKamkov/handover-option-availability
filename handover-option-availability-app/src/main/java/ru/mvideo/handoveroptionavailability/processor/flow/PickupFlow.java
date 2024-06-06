package ru.mvideo.handoveroptionavailability.processor.flow;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;
import ru.mvideo.handoveroptionavailability.processor.context.AvailabilityOptionsProcessor;
import ru.mvideo.handoveroptionavailability.processor.context.CatalogComplementProcessor;
import ru.mvideo.handoveroptionavailability.processor.context.CatalogProcessor;
import ru.mvideo.handoveroptionavailability.processor.context.HandoverObjectAttributesProcessor;
import ru.mvideo.handoveroptionavailability.processor.context.LoadMinPriceRulesProcessor;
import ru.mvideo.handoveroptionavailability.processor.context.LoadProvidersProcessor;
import ru.mvideo.handoveroptionavailability.processor.context.LoadRegionalZoneAttributesProcessor;
import ru.mvideo.handoveroptionavailability.processor.context.LoadZoneProcessor;
import ru.mvideo.handoveroptionavailability.processor.context.brief.HandoverObjectsForPickupProcessor;
import ru.mvideo.handoveroptionavailability.processor.context.brief.PickupObjectsProcessor;
import ru.mvideo.handoveroptionavailability.processor.context.brief.PickupPointsDetailProcessor;
import ru.mvideo.handoveroptionavailability.processor.context.brief.SeamlessProcessor;
import ru.mvideo.handoveroptionavailability.processor.filter.AvailabilityOptionDatesCalculator;
import ru.mvideo.handoveroptionavailability.processor.filter.AvailabilityOptionFilter;
import ru.mvideo.handoveroptionavailability.processor.filter.DpdProductFilter;
import ru.mvideo.handoveroptionavailability.processor.filter.FilterHandoverOptions;
import ru.mvideo.handoveroptionavailability.processor.filter.MinRuleProcessor;
import ru.mvideo.handoveroptionavailability.processor.filter.PickupPartnerFilterProcessor;
import ru.mvideo.handoveroptionavailability.processor.filter.StocksAndShowcaseProcessor;
import ru.mvideo.handoveroptionavailability.processor.filter.fit.KnapsackProblemProcessor;
import ru.mvideo.handoveroptionavailability.processor.filter.payment.FilterOptionsProcessor;
import ru.mvideo.handoveroptionavailability.processor.filter.payment.MspCreditProcessor;
import ru.mvideo.handoveroptionavailability.processor.filter.payment.conditions.MappingPaymentMethodProcessor;
import ru.mvideo.handoveroptionavailability.processor.filter.payment.conditions.MappingWithFilterPaymentMethodProcessor;
import ru.mvideo.handoveroptionavailability.processor.filter.payment.credit.FilterCreditConditionProcessor;
import ru.mvideo.handoveroptionavailability.processor.filter.payment.credit.NoDataForCreditProcessor;
import ru.mvideo.handoveroptionavailability.processor.filter.payment.credit.ZoneApprovalLeadTimeProcessor;
import ru.mvideo.handoveroptionavailability.processor.model.BriefAndPickupContext;
import ru.mvideo.handoveroptionavailability.processor.validation.FindUnsupportedOptionsProcessor;
import ru.mvideo.handoveroptionavailability.processor.validation.FindUnsupportedProductsProcessor;
import ru.mvideo.handoveroptionavailability.processor.validation.OrderQtyValidationProcessor;
import ru.mvideo.handoveroptionavailability.processor.validation.ProductAttributesValidationProcessor;

@RequiredArgsConstructor
@Component
public class PickupFlow implements Flow<BriefAndPickupContext> {

	private final LoadRegionalZoneAttributesProcessor<BriefAndPickupContext> loadRegionalZoneAttributesProcessor;
	private final LoadZoneProcessor<BriefAndPickupContext> loadZoneProcessor;
	private final LoadProvidersProcessor<BriefAndPickupContext> loadProvidersProcessor;
	private final LoadMinPriceRulesProcessor<BriefAndPickupContext> loadPriceRulesProcessor;
	private final FilterHandoverOptions<BriefAndPickupContext> filterHandoverOptions;

	private final CatalogProcessor<BriefAndPickupContext> catalogProcessor;
	private final CatalogComplementProcessor<BriefAndPickupContext> catalogComplementProcessor;
	private final FindUnsupportedProductsProcessor<BriefAndPickupContext> findUnsupportedProductsProcessor;
	private final FindUnsupportedOptionsProcessor<BriefAndPickupContext> findUnsupportedOptionsProcessor;
	private final OrderQtyValidationProcessor<BriefAndPickupContext> orderQtyValidationProcessor;
	private final ProductAttributesValidationProcessor<BriefAndPickupContext> productAttributesValidationProcessor;
	private final DpdProductFilter<BriefAndPickupContext> dpdProductFilter;

	private final PickupObjectsProcessor<BriefAndPickupContext> pickupObjectsProcessor;
	private final HandoverObjectAttributesProcessor<BriefAndPickupContext> handoverObjectAttributesProcessor;

	private final AvailabilityOptionsProcessor<BriefAndPickupContext> availabilityOptionsProcessor;

	private final PickupPointsDetailProcessor pickupPointsProcessor;

	private final KnapsackProblemProcessor<BriefAndPickupContext> knapsackProblemProcessor;
	private final MinRuleProcessor<BriefAndPickupContext> minRuleProcessor;
	private final AvailabilityOptionFilter<BriefAndPickupContext> availabilityOptionFilter;
	private final StocksAndShowcaseProcessor<BriefAndPickupContext> stocksAndShowcaseProcessor;

	private final PickupPartnerFilterProcessor pickupPartnerFilterProcessor;
	private final SeamlessProcessor seamlessProcessor;

	private final AvailabilityOptionDatesCalculator<BriefAndPickupContext> availabilityOptionDatesCalculator;
	private final HandoverObjectsForPickupProcessor handoverObjectsForPickupProcessor;

	private final MappingPaymentMethodProcessor<BriefAndPickupContext> mappingPaymentMethodProcessor;
	private final MappingWithFilterPaymentMethodProcessor<BriefAndPickupContext> mappingWithFilterPaymentMethodProcessor;
	private final FilterOptionsProcessor<BriefAndPickupContext> filterOptionsProcessor;
	private final ZoneApprovalLeadTimeProcessor<BriefAndPickupContext> zoneApprovalLeadTimeProcessor;
	private final FilterCreditConditionProcessor<BriefAndPickupContext> filterCreditConditionProcessor;
	private final NoDataForCreditProcessor<BriefAndPickupContext> noDataForCreditProcessor;
	private final MspCreditProcessor<BriefAndPickupContext> mspCreditProcessor;

	@Override
	public Mono<BriefAndPickupContext> process(BriefAndPickupContext context) {
		return callChainMain(context)
				.flatMap(knapsackProblemProcessor::process)
				.flatMap(availabilityOptionFilter::process)
				.flatMap(mspCreditProcessor::process)
				.flatMap(stocksAndShowcaseProcessor::process)
				.flatMap(pickupPartnerFilterProcessor::process)
				.flatMap(pickupPointsProcessor::process)
				.flatMap(loadPriceRulesProcessor::process)
				.flatMap(minRuleProcessor::process)
				.flatMap(seamlessProcessor::process)
				.flatMap(availabilityOptionDatesCalculator::process);
	}

	public Mono<BriefAndPickupContext> callChainMain(BriefAndPickupContext context) {
		return Mono.zip(
				providersPriceRuleChain(context),
				Mono.zip(productChain(context), pickupHandoverObjectsChain(context)),
				paymentConditionChain(context).flatMap(tupl -> creditChain(context))
		)
				.flatMap(tuple -> availabilityOptionsProcessor.process(context))
				.then(Mono.just(context));
	}

	private Mono<BriefAndPickupContext> productChain(BriefAndPickupContext context) {
		return catalogProcessor.process(context)
				.flatMap(catalogComplementProcessor::process)
				.flatMap(findUnsupportedProductsProcessor::process)
				.flatMap(findUnsupportedOptionsProcessor::process)
				.flatMap(orderQtyValidationProcessor::process)
				.flatMap(productAttributesValidationProcessor::process)
				.flatMap(dpdProductFilter::process);
	}

	private Mono<BriefAndPickupContext> providersPriceRuleChain(BriefAndPickupContext context) {
		return loadRegionalZoneAttributesProcessor.process(context)
				.flatMap(loadZoneProcessor::process)
				.flatMap(loadProvidersProcessor::process)
				.flatMap(filterHandoverOptions::process);
	}

	private Mono<BriefAndPickupContext> pickupHandoverObjectsChain(BriefAndPickupContext context) {
		return handoverObjectsForPickupProcessor.process(context)
				.flatMap(pickupObjectsProcessor::process)
				.flatMap(handoverObjectAttributesProcessor::process);
	}

	private Mono<BriefAndPickupContext> paymentConditionChain(BriefAndPickupContext context) {
		return mappingPaymentMethodProcessor.process(context)
				.flatMap(mappingWithFilterPaymentMethodProcessor::process)
				.flatMap(filterOptionsProcessor::process);
	}

	private Mono<BriefAndPickupContext> creditChain(BriefAndPickupContext context) {
		return zoneApprovalLeadTimeProcessor.process(context)
				.flatMap(filterCreditConditionProcessor::process)
				.flatMap(noDataForCreditProcessor::process)
				.flatMap(filterOptionsProcessor::process);
	}
}
