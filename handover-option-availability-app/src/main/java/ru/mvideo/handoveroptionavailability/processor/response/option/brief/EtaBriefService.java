package ru.mvideo.handoveroptionavailability.processor.response.option.brief;

import java.time.LocalDate;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Service;
import ru.mvideo.handoveroptionavailability.mapper.BriefResponseMapper;
import ru.mvideo.handoveroptionavailability.model.BriefApplicableTo;
import ru.mvideo.handoveroptionavailability.model.BriefOption;
import ru.mvideo.handoveroptionavailability.model.HandoverOption;
import ru.mvideo.handoveroptionavailability.processor.model.BriefAndPickupContext;

@Order(0)
@Service
@RequiredArgsConstructor
public class EtaBriefService extends BriefDeliveryHandoverOptionService {

	private final BriefResponseMapper responseMapper;

	@Override
	protected boolean support(BriefAndPickupContext context) {
		return context.hasOption(HandoverOption.ETA_DELIVERY.getValue());
	}

	@Override
	protected BriefOption prepareResponse(BriefAndPickupContext context) {

		final var optionContext = context.handoverOptionContext().get(HandoverOption.ETA_DELIVERY.getValue());

		final var availabilityOptions = optionContext.getAvailabilityOptions();
		//https://jira.mvideo.ru/jira/browse/CS-5994 Для некоторых опций возвращать признак предоплаты всегда true
		availabilityOptions.forEach(opt -> opt.setPrepaidOnly(true));

		var response = new BriefOption();
		response.setHandoverOption(HandoverOption.fromValue(optionContext.getHandoverOption()));
		response.setMinPrice(optionContext.getMinPriceRule().getPrice().doubleValue());
		response.setApplicableTo(responseMapper.toApplicableTo(availabilityOptions));
		response.setAvailabilityDate(getAvailabilityDate(response.getApplicableTo()));
		response.setEta(120);
		response.setPaymentConditions(paymentConditions(context, optionContext));
		return response;
	}

	protected LocalDate getAvailabilityDate(List<BriefApplicableTo> applicableTo) {
		return applicableTo.stream()
				.map(BriefApplicableTo::getAvailabilityDate)
				.min(LocalDate::compareTo)
				.orElse(null);
	}
}
