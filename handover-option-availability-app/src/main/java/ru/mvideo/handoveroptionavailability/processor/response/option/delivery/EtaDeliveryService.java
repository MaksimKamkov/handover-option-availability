package ru.mvideo.handoveroptionavailability.processor.response.option.delivery;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Service;
import ru.mvideo.handoveroptionavailability.mapper.AvailableAtMapper;
import ru.mvideo.handoveroptionavailability.model.DeliveryResponse;
import ru.mvideo.handoveroptionavailability.model.HandoverOption;
import ru.mvideo.handoveroptionavailability.model.ReservationAvailableAt;
import ru.mvideo.handoveroptionavailability.processor.model.DeliveryContext;
import ru.mvideo.handoveroptionavailability.processor.model.Flags;
import ru.mvideo.handoveroptionavailability.processor.model.HandoverObject;
import ru.mvideo.handoveroptionavailability.processor.model.OptionContext;

@Order(0)
@Service
@RequiredArgsConstructor
public class EtaDeliveryService extends DetailedHandoverOptionService {

	private static final int ETA_DELIVERY_TIME = 120;

	private final AvailableAtMapper responseMapper;

	@Override
	protected boolean support(DeliveryContext context) {
		return context.hasOption(HandoverOption.ETA_DELIVERY.getValue());
	}

	@Override
	protected DeliveryResponse prepareResponse(DeliveryContext context) {
		final var optionContext = context.handoverOptionContext().get(HandoverOption.ETA_DELIVERY.getValue());

		boolean includeStocks = context.flags().contains(Flags.INCLUDE_STOCKS);

		final var availabilityOptions = optionContext.getAvailabilityOptions();
		//https://jira.mvideo.ru/jira/browse/CS-5994 Для некоторых опций возвращать признак предоплаты всегда true
		availabilityOptions.forEach(opt -> opt.setPrepaidOnly(true));

		final var availableAt = responseMapper.toDeliveryAvailableAt(availabilityOptions, includeStocks);

		final var sortedAvailableAt = sortByDistanceAndLimit10Entries(optionContext, availableAt);

		final var response = new DeliveryResponse();
		response.setHandoverOption(HandoverOption.fromValue(optionContext.getHandoverOption()));
		response.setHandoverOptionMaterial(optionContext.getHandoverOptionMaterial());
		response.setMinPrice(optionContext.getMinPriceRule().getPrice().doubleValue());
		response.setAvailableAt(sortedAvailableAt);
		response.setAvailabilityDate(LocalDate.now());
		response.setEta(context.getDeliveryDuration() == null ? ETA_DELIVERY_TIME : context.getDeliveryDuration());
		response.setPaymentConditions(paymentConditions(context, optionContext));
		return response;
	}

	private List<ReservationAvailableAt> sortByDistanceAndLimit10Entries(OptionContext optionContext, List<ReservationAvailableAt> availableAt) {
		final var objectsIdsInDeliveryRadius = optionContext.getHandoverObjects().stream()
				.map(HandoverObject::getObjectId)
				.toList();

		final var reservationAvailableAtsSorted = new ArrayList<ReservationAvailableAt>();
		for (String objectId : objectsIdsInDeliveryRadius) {
			for (ReservationAvailableAt at : availableAt) {
				if (objectId.equals(at.getHandoverObject())) {
					reservationAvailableAtsSorted.add(at);
				}
			}
		}
		return reservationAvailableAtsSorted.stream()
				.limit(10)
				.collect(Collectors.toList());
	}
}
