package ru.mvideo.handoveroptionavailability.processor.response.option.brief;

import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.function.Consumer;
import java.util.stream.Collectors;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Service;
import ru.mvideo.handoveroptionavailability.model.BriefApplicableTo;
import ru.mvideo.handoveroptionavailability.model.BriefOption;
import ru.mvideo.handoveroptionavailability.model.HandoverOption;
import ru.mvideo.handoveroptionavailability.model.PartnerBrand;
import ru.mvideo.handoveroptionavailability.model.SumObjectsOfDate;
import ru.mvideo.handoveroptionavailability.model.external.msp.logistic.response.AvailabilityOption;
import ru.mvideo.handoveroptionavailability.processor.model.BriefAndPickupContext;
import ru.mvideo.handoveroptionavailability.utils.GroupingUtils;
import ru.mvideo.lards.restriction.model.KnapsackBriefResponse;

@Order(1)
@Service
@RequiredArgsConstructor
public class PickupPartnerBriefService extends BriefPickupHandoverOptionService {

	@Override
	protected boolean support(BriefAndPickupContext context) {
		return context.hasOption(HandoverOption.PICKUP_PARTNER.getValue());
	}

	@Override
	protected BriefOption prepareResponse(BriefAndPickupContext context) {
		final var optionContext = context.handoverOptionContext().get(HandoverOption.PICKUP_PARTNER.getValue());

		final var availabilityOptions = optionContext.getAvailabilityOptions();
		//https://jira.mvideo.ru/jira/browse/CS-5994 Для некоторых опций возвращать признак предоплаты всегда true
		availabilityOptions.forEach(opt -> opt.setPrepaidOnly(true));

		final var applicableTo = toApplicableTo(availabilityOptions);
		final var pickupPoints = context.briefPickupPoints();
		final var availabilityDate = pickupPoints.getMinAvailabilityDate();
		final var partnerBrands = collectPartnerBrands(pickupPoints);

		final var response = new BriefOption();
		response.setHandoverOption(HandoverOption.fromValue(optionContext.getHandoverOption()));
		response.setMinPrice(optionContext.getMinPriceRule().getPrice().doubleValue());
		response.setApplicableTo(applicableTo);
		response.setAvailabilityDate(availabilityDate);
		response.setPartnerBrand(partnerBrands);
		response.setSumObjectsOfDate(List.of(new SumObjectsOfDate(pickupPoints.getQtyPickupPoints(), availabilityDate)));
		response.setPaymentConditions(paymentConditions(context, optionContext));
		return response;
	}

	private List<PartnerBrand> collectPartnerBrands(KnapsackBriefResponse response) {
		return response.getPartnerBrand().stream()
				.map(pp -> PartnerBrand.valueOf(pp.name()))
				.distinct()
				.collect(Collectors.toList());
	}

	public List<BriefApplicableTo> toApplicableTo(List<AvailabilityOption> availabilityOptions) {
		return GroupingUtils.toMaterialDateMap(availabilityOptions).entrySet().stream()
				.map(entry -> {
					final var minDate = entry.getValue()
							.entrySet().stream()
							.min(Map.Entry.comparingByKey());

					if (minDate.isEmpty()) {
						return null;
					}

					final List<AvailabilityOption> options = minDate.get().getValue();
					removeDuplicateOptions(options);//не удалять

					final var stockCounter = options.stream()
							.collect(StockCounter::new, StockCounter::accept, StockCounter::combine);

					final var applicableTo = new BriefApplicableTo();
					applicableTo.setAvailabilityDate(minDate.get().getKey());
					applicableTo.setQty(stockCounter.getQty() + stockCounter.getShowcaseQty());
					applicableTo.setPrepaidQty(stockCounter.getPrepaidQty());
					applicableTo.setShowcaseQty(stockCounter.getShowcaseQty());
					applicableTo.setMaterial(entry.getKey());
					return applicableTo;
				})
				.filter(Objects::nonNull)
				.collect(Collectors.toList());
	}

	//операция требуется из-за бага в MSP, при котором могут быть получены несколько опций по одному объекту https://jira.mvideo.ru/jira/browse/CS-944
	private void removeDuplicateOptions(List<AvailabilityOption> options) {
		final var stocks = new HashSet<String>();
		final var iterator = options.iterator();
		while (iterator.hasNext()) {
			final var option = iterator.next();
			final var stockObject = option.getStockObject();
			if (stocks.contains(stockObject)) {
				iterator.remove();
			} else {
				stocks.add(stockObject);
			}
		}
	}

	@Getter
	static class StockCounter implements Consumer<AvailabilityOption> {
		private int qty = 0;
		private int prepaidQty = 0;
		private int showcaseQty = 0;

		@Override
		public void accept(AvailabilityOption option) {
			qty += option.getAvailableStock();
			showcaseQty += option.getShowCaseStock();
			if (option.isPrepaidOnly()) {
				prepaidQty += option.getAvailableStock() + option.getShowCaseStock();
			}
		}

		public void combine(StockCounter other) {
			qty += other.qty;
			prepaidQty += other.prepaidQty;
			showcaseQty += other.showcaseQty;
		}
	}
}
