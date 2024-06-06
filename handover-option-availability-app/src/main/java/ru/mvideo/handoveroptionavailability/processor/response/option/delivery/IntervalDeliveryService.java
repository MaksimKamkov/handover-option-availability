package ru.mvideo.handoveroptionavailability.processor.response.option.delivery;

import java.time.LocalDate;
import lombok.RequiredArgsConstructor;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Service;
import ru.mvideo.handoveroptionavailability.config.CreditMaxDeliveryDaysProperties;
import ru.mvideo.handoveroptionavailability.mapper.AvailableAtMapper;
import ru.mvideo.handoveroptionavailability.model.DeliveryResponse;
import ru.mvideo.handoveroptionavailability.model.HandoverOption;
import ru.mvideo.handoveroptionavailability.processor.model.DeliveryContext;
import ru.mvideo.handoveroptionavailability.processor.model.Flags;

@Order(4)
@RequiredArgsConstructor
@Service
public class IntervalDeliveryService extends DetailedHandoverOptionService {

	private final AvailableAtMapper responseMapper;
	private final CreditMaxDeliveryDaysProperties creditMaxDeliveryDaysProperties;

	@Override
	protected boolean support(DeliveryContext context) {
		return context.hasOption(HandoverOption.INTERVAL_DELIVERY.getValue());
	}

	@Override
	protected DeliveryResponse prepareResponse(DeliveryContext context) {
		final var optionContext = context.handoverOptionContext().get(HandoverOption.INTERVAL_DELIVERY.getValue());

		final boolean includeStocks = context.flags().contains(Flags.INCLUDE_STOCKS);
		final var availableAt = responseMapper.toDeliveryAvailableAt(optionContext.getAvailabilityOptions(), includeStocks);
		final var date = getAvailabilityDateForAvailableMaterials(availableAt);

		LocalDate maxDeliveryDate = null;
		if (context.paymentMethod() != null && context.paymentMethod().contains("CREDIT")) {
			maxDeliveryDate = LocalDate.now().plusDays(creditMaxDeliveryDaysProperties.getMaxDeliveryDays());
		}

		final var response = new DeliveryResponse();
		response.setHandoverOption(HandoverOption.INTERVAL_DELIVERY);
		response.setHandoverOptionMaterial(optionContext.getHandoverOptionMaterial());
		response.setMinPrice(optionContext.getMinPriceRule().getPrice().doubleValue());
		response.setAvailabilityDate(date);
		response.setAvailableAt(availableAt);
		response.setMaxDeliveryDate(maxDeliveryDate);
		response.setPaymentConditions(paymentConditions(context, optionContext));
		return response;
	}

}
