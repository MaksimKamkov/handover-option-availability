package ru.mvideo.handoveroptionavailability.processor.response.option.pickup;

import java.time.LocalDate;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Service;
import ru.mvideo.handoveroptionavailability.config.CreditMaxDeliveryDaysProperties;
import ru.mvideo.handoveroptionavailability.mapper.AvailableAtMapper;
import ru.mvideo.handoveroptionavailability.model.AvailableDate;
import ru.mvideo.handoveroptionavailability.model.HandoverOption;
import ru.mvideo.handoveroptionavailability.model.PickupAvailableAt;
import ru.mvideo.handoveroptionavailability.model.PickupResponseItem;
import ru.mvideo.handoveroptionavailability.processor.model.BriefAndPickupContext;
import ru.mvideo.handoveroptionavailability.processor.model.Flags;

@Order(0)
@Service
@RequiredArgsConstructor
public class PickupService extends PickupHandoverOptionService {

	private final AvailableAtMapper responseMapper;
	private final CreditMaxDeliveryDaysProperties creditMaxDeliveryDaysProperties;

	@Override
	protected boolean support(BriefAndPickupContext context) {
		return context.hasOption(HandoverOption.PICKUP.getValue());
	}

	@Override
	protected PickupResponseItem prepareResponse(BriefAndPickupContext context) {
		final var optionContext = context.handoverOptionContext().get(HandoverOption.PICKUP.getValue());
		final boolean includeStocks = context.flags().contains(Flags.INCLUDE_STOCKS);
		final var pickupAvailableAts = responseMapper.toPickupAvailableAt(optionContext.getAvailabilityOptions(), includeStocks);

		LocalDate maxDeliveryDate = null;
		if (context.paymentMethod() != null && context.paymentMethod().contains("CREDIT")) {
			maxDeliveryDate = LocalDate.now().plusDays(creditMaxDeliveryDaysProperties.getMaxDeliveryDays());
		}

		final var response = new PickupResponseItem();
		response.setHandoverOption(HandoverOption.fromValue(optionContext.getHandoverOption()));
		response.setHandoverOptionMaterial(optionContext.getHandoverOptionMaterial());
		response.setMinPrice(optionContext.getMinPriceRule().getPrice().doubleValue());
		response.setAvailableAt(pickupAvailableAts);
		response.setAvailabilityDate(getAvailabilityDate(pickupAvailableAts));
		response.setMaxDeliveryDate(maxDeliveryDate);
		response.setPaymentConditions(paymentConditions(context, optionContext));
		return response;
	}

	private LocalDate getAvailabilityDate(List<PickupAvailableAt> availableAt) {
		return availableAt.stream()
				.flatMap(date -> date.getAvailableMaterials().stream().map(AvailableDate::getDate))
				.min(LocalDate::compareTo)
				.orElse(null);
	}
}
