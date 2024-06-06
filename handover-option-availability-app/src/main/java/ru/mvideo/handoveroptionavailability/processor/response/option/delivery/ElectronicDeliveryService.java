package ru.mvideo.handoveroptionavailability.processor.response.option.delivery;

import lombok.RequiredArgsConstructor;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Service;
import ru.mvideo.handoveroptionavailability.mapper.AvailableAtMapper;
import ru.mvideo.handoveroptionavailability.model.DeliveryResponse;
import ru.mvideo.handoveroptionavailability.model.HandoverOption;
import ru.mvideo.handoveroptionavailability.processor.model.DeliveryContext;
import ru.mvideo.handoveroptionavailability.processor.model.Flags;

@Order(3)
@RequiredArgsConstructor
@Service
public class ElectronicDeliveryService extends DetailedHandoverOptionService {

	private final AvailableAtMapper responseMapper;

	@Override
	protected boolean support(DeliveryContext context) {
		return context.hasOption(HandoverOption.ELECTRONIC_DELIVERY.getValue());
	}

	@Override
	protected DeliveryResponse prepareResponse(DeliveryContext context) {
		final var optionContext = context.handoverOptionContext().get(HandoverOption.ELECTRONIC_DELIVERY.getValue());

		final boolean includeStocks = context.flags().contains(Flags.INCLUDE_STOCKS);
		final var availableAt = responseMapper.toDeliveryAvailableAt(optionContext.getAvailabilityOptions(), includeStocks);
		final var date = getAvailabilityDateForAvailableMaterials(availableAt);

		final var response = new DeliveryResponse();
		response.setHandoverOption(HandoverOption.ELECTRONIC_DELIVERY);
		response.setHandoverOptionMaterial(optionContext.getHandoverOptionMaterial());
		response.setMinPrice(optionContext.getMinPriceRule().getPrice().doubleValue());
		response.setAvailabilityDate(date);
		response.setAvailableAt(availableAt);
		response.setPaymentConditions(paymentConditions(context, optionContext));
		return response;
	}

}
