package ru.mvideo.handoveroptionavailability.processor.response.option.delivery;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Service;
import ru.mvideo.handoveroptionavailability.component.ExactlyTimeDeliveryIntervalCalculator;
import ru.mvideo.handoveroptionavailability.mapper.AvailableAtMapper;
import ru.mvideo.handoveroptionavailability.model.AvailableInterval;
import ru.mvideo.handoveroptionavailability.model.DeliveryResponse;
import ru.mvideo.handoveroptionavailability.model.HandoverOption;
import ru.mvideo.handoveroptionavailability.model.ReservationAvailableAt;
import ru.mvideo.handoveroptionavailability.model.external.msp.logistic.response.AvailabilityOption;
import ru.mvideo.handoveroptionavailability.processor.model.DeliveryContext;
import ru.mvideo.handoveroptionavailability.processor.model.Flags;
import ru.mvideo.handoveroptionavailability.processor.model.HandoverObject;
import ru.mvideo.handoveroptionavailability.processor.model.OptionContext;

@Order(1)
@Service
@RequiredArgsConstructor
public class ExactlyTimeDeliveryService extends DetailedHandoverOptionService {

	private final AvailableAtMapper responseMapper;
	private final ExactlyTimeDeliveryIntervalCalculator intervalCalculator;

	@Override
	protected boolean support(DeliveryContext context) {
		return context.hasOption(HandoverOption.EXACTLY_TIME_DELIVERY.getValue());
	}

	@Override
	protected DeliveryResponse prepareResponse(DeliveryContext context) {
		final var optionContext = context.handoverOptionContext().get(HandoverOption.EXACTLY_TIME_DELIVERY.getValue());

		final var availabilityOptions = optionContext.getAvailabilityOptions();
		//https://jira.mvideo.ru/jira/browse/CS-5994 Для некоторых опций возвращать признак предоплаты всегда true
		availabilityOptions.forEach(opt -> opt.setPrepaidOnly(true));

		final var price = optionContext.getMinPriceRule().getPrice().doubleValue();
		final List<AvailableInterval> availableIntervals = availableIntervals(
				context.getDeliveryDuration(),
				optionContext,
				availabilityOptions,
				price
		);
		if (availableIntervals.isEmpty()) {
			return null;
		}

		boolean includeStocks = context.flags().contains(Flags.INCLUDE_STOCKS);
		final var availableAt = responseMapper.toDeliveryAvailableAt(availabilityOptions, includeStocks);

		final var sortedAvailableAt = sortByDistanceAndLimit10Entries(optionContext, availableAt);

		final var response = new DeliveryResponse();
		response.setHandoverOption(HandoverOption.fromValue(optionContext.getHandoverOption()));
		response.setHandoverOptionMaterial(optionContext.getHandoverOptionMaterial());
		response.setMinPrice(price);
		response.setAvailableAt(sortedAvailableAt);
		response.setAvailableIntervals(availableIntervals);
		response.setAvailabilityDate(getAvailabilityDate(availableIntervals));
		response.setPaymentConditions(paymentConditions(context, optionContext));
		return response;
	}

	protected LocalDate getAvailabilityDate(List<AvailableInterval> availableIntervals) {
		return availableIntervals.stream()
				.map(AvailableInterval::getAvailabilityDate)
				.min(LocalDate::compareTo)
				.orElse(null);
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

	private Optional<HandoverObject> findNearestHandoverObjectWithWorkingSchedule(List<HandoverObject> handoverObjects) {
		return handoverObjects.stream()
				.min(Comparator.comparing(HandoverObject::getDistance));
	}


	private List<AvailableInterval> availableIntervals(Integer deliveryDuration, OptionContext optionContext,
	                                                   List<AvailabilityOption> availabilityOptions, double price) {
		List<HandoverObject> handoverObjects = optionContext.getHandoverObjects();
		if (deliveryDuration != null) {
			Optional<HandoverObject> nearestHandoverObject = findNearestHandoverObjectWithWorkingSchedule(handoverObjects);
			if (nearestHandoverObject.isPresent()) {
				HandoverObject handoverObject = nearestHandoverObject.get();
				return handoverObject.getWorkStartTime() != null && handoverObject.getWorkEndTime() != null
						? intervalCalculator.toIntervalsByWorkingSchedule(availabilityOptions, price, handoverObject, deliveryDuration)
						: intervalCalculator.toIntervals(availabilityOptions, price, handoverObjects);
			}
		}
		return intervalCalculator.toIntervals(availabilityOptions, price, handoverObjects);
	}
}
