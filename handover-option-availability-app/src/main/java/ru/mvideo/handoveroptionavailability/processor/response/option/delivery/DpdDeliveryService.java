package ru.mvideo.handoveroptionavailability.processor.response.option.delivery;

import java.time.LocalDate;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Service;
import ru.mvideo.handoveroptionavailability.mapper.AvailableAtMapper;
import ru.mvideo.handoveroptionavailability.model.AvailableDate;
import ru.mvideo.handoveroptionavailability.model.DeliveryResponse;
import ru.mvideo.handoveroptionavailability.model.HandoverOption;
import ru.mvideo.handoveroptionavailability.model.ReservationAvailableAt;
import ru.mvideo.handoveroptionavailability.processor.model.DeliveryContext;
import ru.mvideo.handoveroptionavailability.processor.model.Flags;

@Order(2)
@RequiredArgsConstructor
@Service
public class DpdDeliveryService extends DetailedHandoverOptionService {

	private final AvailableAtMapper responseMapper;

	@Override
	protected boolean support(DeliveryContext context) {
		return context.hasOption(HandoverOption.DPD_DELIVERY.getValue());
	}

	@Override
	protected DeliveryResponse prepareResponse(DeliveryContext context) {

		final var optionContext = context.handoverOptionContext().get(HandoverOption.DPD_DELIVERY.getValue());
		final var products = optionContext.getProducts();

		boolean includeStocks = context.flags().contains(Flags.INCLUDE_STOCKS);
		final var availableAt = responseMapper.toDeliveryAvailableAt(optionContext.getAvailabilityOptions(), includeStocks);
		final var availabilityDate = getAvailabilityDate(availableAt, products.size());

		final var response = new DeliveryResponse();
		response.setHandoverOption(HandoverOption.DPD_DELIVERY);
		response.setHandoverOptionMaterial(optionContext.getHandoverOptionMaterial());
		response.setMinPrice(optionContext.getMinPriceRule().getPrice().doubleValue());
		response.setAvailabilityDate(availabilityDate);
		response.setAvailableAt(availableAt);
		response.setPaymentConditions(paymentConditions(context, optionContext));
		return response;
	}

	private LocalDate getAvailabilityDate(List<ReservationAvailableAt> availableAt, int productsCount) {
		return availableAt.stream()
				.flatMap(date -> date.getAvailableDates().stream()
						.filter(d -> d.getApplicableTo().size() == productsCount)
						.map(AvailableDate::getDate))
				.min(LocalDate::compareTo)
				.orElse(null);
	}
}
