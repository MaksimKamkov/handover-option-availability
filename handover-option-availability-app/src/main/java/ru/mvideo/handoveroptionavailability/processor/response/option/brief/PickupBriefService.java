package ru.mvideo.handoveroptionavailability.processor.response.option.brief;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Service;
import ru.mvideo.handoveroptionavailability.config.CreditMaxDeliveryDaysProperties;
import ru.mvideo.handoveroptionavailability.mapper.AvailableAtMapper;
import ru.mvideo.handoveroptionavailability.mapper.BriefResponseMapper;
import ru.mvideo.handoveroptionavailability.model.BriefApplicableTo;
import ru.mvideo.handoveroptionavailability.model.BriefOption;
import ru.mvideo.handoveroptionavailability.model.HandoverOption;
import ru.mvideo.handoveroptionavailability.model.PickupAvailableAt;
import ru.mvideo.handoveroptionavailability.model.SumObjectsOfDate;
import ru.mvideo.handoveroptionavailability.model.external.msp.logistic.response.AvailabilityOption;
import ru.mvideo.handoveroptionavailability.processor.model.BriefAndPickupContext;
import ru.mvideo.handoveroptionavailability.processor.model.Flags;
import ru.mvideo.handoveroptionavailability.processor.utils.DateCalculator;

@Order(0)
@Service
@RequiredArgsConstructor
public class PickupBriefService extends BriefPickupHandoverOptionService {

	private final BriefResponseMapper responseMapper;
	private final AvailableAtMapper availableAtMapper;
	private final CreditMaxDeliveryDaysProperties creditMaxDeliveryDaysProperties;

	@Override
	protected boolean support(BriefAndPickupContext context) {
		return context.hasOption(HandoverOption.PICKUP.getValue());
	}

	@Override
	protected BriefOption prepareResponse(BriefAndPickupContext context) {

		final var optionContext = context.handoverOptionContext().get(HandoverOption.PICKUP.getValue());

		LocalDate maxDeliveryDate = null;
		if (context.paymentMethod() != null && context.paymentMethod().contains("CREDIT")) {
			maxDeliveryDate = LocalDate.now().plusDays(creditMaxDeliveryDaysProperties.getMaxDeliveryDays());
		}

		final var response = new BriefOption();
		response.setHandoverOption(HandoverOption.fromValue(optionContext.getHandoverOption()));
		response.setMinPrice(optionContext.getMinPriceRule().getPrice().doubleValue());
		response.setApplicableTo(responseMapper.toApplicableTo(optionContext.getAvailabilityOptions()));

		if (context.flags().contains(Flags.INCLUDE_PICKUP_OBJECT)) {
			final var pickupAvailableAts = availableAtMapper.toPickupAvailableAt(optionContext.getAvailabilityOptions(), false);
			for (PickupAvailableAt availableAt : pickupAvailableAts) {
				final var limitAvailableMaterials = availableAt.getAvailableMaterials().stream()
						.limit(1)
						.collect(Collectors.toList());
				availableAt.setAvailableMaterials(limitAvailableMaterials);
			}
			response.getApplicableTo().get(0).setAvailableAt(pickupAvailableAts);
		}

		final var sumObjectsOfDate = calculateSumObjectsOfDate(optionContext.getAvailabilityOptions());
		response.setSumObjectsOfDate(sumObjectsOfDate);

		response.setAvailabilityDate(getAvailabilityDate(response.getApplicableTo()));
		response.setMaxDeliveryDate(maxDeliveryDate);
		response.setPaymentConditions(paymentConditions(context, optionContext));
		return response;
	}

	protected LocalDate getAvailabilityDate(List<BriefApplicableTo> applicableTo) {
		return applicableTo.stream()
				.map(BriefApplicableTo::getAvailabilityDate)
				.min(LocalDate::compareTo)
				.orElse(null);
	}

	private List<SumObjectsOfDate> calculateSumObjectsOfDate(List<AvailabilityOption> availabilityOptions) {
		final var options = DateCalculator.enrichAvailabilityOptionDates(availabilityOptions);
		return options.stream()
				.collect(Collectors.groupingBy(AvailabilityOption::getAvailableDate))
				.entrySet().stream()
				.map(this::toSumObjectsOfDate)
				.collect(Collectors.toList());
	}

	private SumObjectsOfDate toSumObjectsOfDate(Map.Entry<LocalDate, List<AvailabilityOption>> dateOptionsEntry) {
		final var sumObjectsOfDate = new SumObjectsOfDate();
		sumObjectsOfDate.setAvailabilityDate(dateOptionsEntry.getKey());
		final var objects = dateOptionsEntry.getValue().stream()
				.map(AvailabilityOption::getHandoverObject)
				.collect(Collectors.toSet());
		sumObjectsOfDate.setQtyObjects(objects.size());
		return sumObjectsOfDate;
	}
}
