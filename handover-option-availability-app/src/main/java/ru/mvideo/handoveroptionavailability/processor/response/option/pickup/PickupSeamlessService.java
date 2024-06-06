package ru.mvideo.handoveroptionavailability.processor.response.option.pickup;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Service;
import ru.mvideo.availability_chains.model.RelatedObjectsDetails;
import ru.mvideo.handoveroptionavailability.config.DeliveryTimeProperties;
import ru.mvideo.handoveroptionavailability.mapper.AvailableAtMapper;
import ru.mvideo.handoveroptionavailability.model.AvailableDate;
import ru.mvideo.handoveroptionavailability.model.HandoverOption;
import ru.mvideo.handoveroptionavailability.model.PickupAvailableAt;
import ru.mvideo.handoveroptionavailability.model.PickupResponseItem;
import ru.mvideo.handoveroptionavailability.model.external.msp.logistic.response.AvailabilityOption;
import ru.mvideo.handoveroptionavailability.processor.model.BriefAndPickupContext;
import ru.mvideo.handoveroptionavailability.processor.model.Flags;
import ru.mvideo.handoveroptionavailability.processor.model.HandoverObject;
import ru.mvideo.handoveroptionavailability.processor.utils.IntervalsUtil;
import ru.mvideo.handoveroptionavailability.utils.CalculateTimeUtils;

@Order(2)
@Service
@RequiredArgsConstructor
public class PickupSeamlessService extends PickupHandoverOptionService {

	private static final DateTimeFormatter TIME_FORMATTER = DateTimeFormatter.ofPattern("HH:mm");

	private final AvailableAtMapper responseMapper;
	private final DeliveryTimeProperties seamlessProperties;

	@Override
	protected boolean support(BriefAndPickupContext context) {
		return context.hasOption(HandoverOption.PICKUP_SEAMLESS.getValue());
	}

	@Override
	protected PickupResponseItem prepareResponse(BriefAndPickupContext context) {

		final var optionContext = context.handoverOptionContext().get(HandoverOption.PICKUP_SEAMLESS.getValue());
		final boolean includeStocks = context.flags().contains(Flags.INCLUDE_STOCKS);

		final var availableCourierObjects = context.handoverObjects().stream()
				.filter(object -> object.getIsAutoCourierAvailable() != null)
				.filter(HandoverObject::getIsAutoCourierAvailable)
				.map(HandoverObject::getObjectId)
				.collect(Collectors.toSet());

		Map<RelatedObjectsDetails, List<AvailabilityOption>> relatedObjectSourceAvailabilityOptionMap =
				context.seamlessRelatedObjectsDetails().stream()
						.filter(object -> availableCourierObjects.contains(object.getObjectSource())
								&& availableCourierObjects.contains(object.getObjectRecipient()))
						.collect(Collectors.toMap(
								object -> object,
								object -> optionContext.getAvailabilityOptions().stream()
										.filter(option -> option.getHandoverObject().equals(object.getObjectSource()))
										.collect(Collectors.toList())
						));

		var pickupAvailableAts = responseMapper.toPickupSeamlessAvailableAt(relatedObjectSourceAvailabilityOptionMap, includeStocks);

		calculateAvailableAtTimeAndAvailabilityDate(pickupAvailableAts, context.seamlessRelatedObjectsDetails(), optionContext.getHandoverObjects());

		final var response = new PickupResponseItem();
		response.setHandoverOption(HandoverOption.fromValue(optionContext.getHandoverOption()));
		response.setHandoverOptionMaterial(optionContext.getHandoverOptionMaterial());
		response.setMinPrice(optionContext.getMinPriceRule().getPrice().doubleValue());
		response.setAvailableAt(pickupAvailableAts);
		response.setAvailabilityDate(availabilityDate(pickupAvailableAts));
		response.setPaymentConditions(paymentConditions(context, optionContext));

		return response;
	}

	private LocalDate availabilityDate(List<PickupAvailableAt> availableAts) {
		return availableAts.stream()
				.flatMap(availableAt -> availableAt.getAvailableMaterials().stream()
						.map(AvailableDate::getDate))
				.min(LocalDate::compareTo)
				.orElse(null);
	}

	private void calculateAvailableAtTimeAndAvailabilityDate(List<PickupAvailableAt> pickupAvailableAts,
	                                                         List<RelatedObjectsDetails> seamlessRelatedObjectsDetails,
	                                                         List<HandoverObject> handoverObjects) {

		final var requestDateTime = IntervalsUtil.calculateTimeZoneId(handoverObjects);

		for (PickupAvailableAt availableAt : pickupAvailableAts) {
			Optional<RelatedObjectsDetails> relatedObject = seamlessRelatedObjectsDetails.stream()
					.filter(relatedObjectsDetails -> relatedObjectsDetails.getObjectRecipient().equals(availableAt.getHandoverObject()))
					.findAny();
			if (relatedObject.isPresent()) {
				for (AvailableDate availableDate : availableAt.getAvailableMaterials()) {
					calculateTimeAndDate(requestDateTime, relatedObject.get().getLeadTime(), availableDate);
				}
			}
		}
	}

	private void calculateTimeAndDate(LocalDateTime requestDateTime, Integer deliveryTime, AvailableDate availableDate) {
		var startTime = seamlessProperties.getStartTime();
		var endTime = seamlessProperties.getEndTime();
		var returnTime = seamlessProperties.getDeliveryReturnTime();
		LocalDateTime dateTime =
				CalculateTimeUtils.seamlessCalculateDateAndTime(requestDateTime, deliveryTime, returnTime, startTime, endTime);

		availableDate.setDate(dateTime.toLocalDate());
		availableDate.setTime(dateTime.toLocalTime().format(TIME_FORMATTER));
	}
}
