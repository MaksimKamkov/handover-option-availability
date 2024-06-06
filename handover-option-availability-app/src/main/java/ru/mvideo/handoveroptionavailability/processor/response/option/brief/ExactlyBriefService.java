package ru.mvideo.handoveroptionavailability.processor.response.option.brief;

import java.time.LocalDate;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Service;
import ru.mvideo.handoveroptionavailability.component.ExactlyTimeDeliveryIntervalCalculator;
import ru.mvideo.handoveroptionavailability.mapper.BriefResponseMapper;
import ru.mvideo.handoveroptionavailability.model.AvailableInterval;
import ru.mvideo.handoveroptionavailability.model.BriefOption;
import ru.mvideo.handoveroptionavailability.model.HandoverOption;
import ru.mvideo.handoveroptionavailability.processor.model.BriefAndPickupContext;

@Order(1)
@Service
@RequiredArgsConstructor
public class ExactlyBriefService extends BriefDeliveryHandoverOptionService {

	private final BriefResponseMapper responseMapper;
	private final ExactlyTimeDeliveryIntervalCalculator intervalCalculator;

	@Override
	protected boolean support(BriefAndPickupContext context) {
		return context.hasOption(HandoverOption.EXACTLY_TIME_DELIVERY.getValue());
	}

	@Override
	protected BriefOption prepareResponse(BriefAndPickupContext context) {
		final var optionContext = context.handoverOptionContext().get(HandoverOption.EXACTLY_TIME_DELIVERY.getValue());

		final var availabilityOptions = optionContext.getAvailabilityOptions();

		//https://jira.mvideo.ru/jira/browse/CS-5994 Для некоторых опций возвращать признак предоплаты всегда true
		availabilityOptions.forEach(opt -> opt.setPrepaidOnly(true));

		final var price = optionContext.getMinPriceRule().getPrice().doubleValue();
		final var availableIntervals = intervalCalculator.toIntervals(availabilityOptions, price, optionContext.getHandoverObjects());
		if (availableIntervals.isEmpty()) {
			return null;
		}

		var response = new BriefOption();
		response.setHandoverOption(HandoverOption.fromValue(optionContext.getHandoverOption()));
		response.setAvailableIntervals(availableIntervals);
		response.setAvailabilityDate(getAvailabilityDate(availableIntervals));
		response.setMinPrice(optionContext.getMinPriceRule().getPrice().doubleValue());
		response.setApplicableTo(responseMapper.toApplicableTo(availabilityOptions));
		response.setPaymentConditions(paymentConditions(context, optionContext));
		return response;
	}

	protected LocalDate getAvailabilityDate(List<AvailableInterval> availableIntervals) {
		return availableIntervals.stream()
				.map(AvailableInterval::getAvailabilityDate)
				.min(LocalDate::compareTo)
				.orElse(null);
	}
}
