package ru.mvideo.handoveroptionavailability.processor.response.option.brief;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Comparator;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Service;
import ru.mvideo.availability_chains.model.RelatedObjectsDetails;
import ru.mvideo.handoveroptionavailability.config.DeliveryTimeProperties;
import ru.mvideo.handoveroptionavailability.mapper.BriefResponseMapper;
import ru.mvideo.handoveroptionavailability.model.BriefOption;
import ru.mvideo.handoveroptionavailability.model.HandoverOption;
import ru.mvideo.handoveroptionavailability.processor.model.BriefAndPickupContext;
import ru.mvideo.handoveroptionavailability.processor.model.HandoverObject;
import ru.mvideo.handoveroptionavailability.processor.utils.IntervalsUtil;
import ru.mvideo.handoveroptionavailability.utils.CalculateTimeUtils;

@Order(2)
@Service
@RequiredArgsConstructor
public class PickupSeamlessBriefService extends BriefPickupHandoverOptionService {

	private static final DateTimeFormatter TIME_FORMATTER = DateTimeFormatter.ofPattern("HH:mm");

	private final BriefResponseMapper responseMapper;
	private final DeliveryTimeProperties seamlessProperties;

	@Override
	protected boolean support(BriefAndPickupContext context) {
		return context.hasOption(HandoverOption.PICKUP_SEAMLESS.getValue());
	}

	@Override
	protected BriefOption prepareResponse(BriefAndPickupContext context) {
		final var optionContext = context.handoverOptionContext().get(HandoverOption.PICKUP_SEAMLESS.getValue());

		final var availabilityOptions = optionContext.getAvailabilityOptions();
		//https://jira.mvideo.ru/jira/browse/CS-4206 Для pickup-seamless флаг prepaid должны быть всегда true
		availabilityOptions.forEach(opt -> opt.setPrepaidOnly(true));
		final var applicableTo = responseMapper.toApplicableTo(availabilityOptions);

		final var availabilityDate = getAvailabilityDate(context.seamlessRelatedObjectsDetails(), optionContext.getHandoverObjects());

		final var response = new BriefOption();
		response.setHandoverOption(HandoverOption.fromValue(optionContext.getHandoverOption()));
		response.setMinPrice(optionContext.getMinPriceRule().getPrice().doubleValue());
		response.setApplicableTo(applicableTo);
		response.setAvailabilityDate(availabilityDate.toLocalDate());
		response.setTime(availabilityDate.toLocalTime().format(TIME_FORMATTER));
		response.setPaymentConditions(paymentConditions(context, optionContext));
		return response;
	}

	private LocalDateTime getAvailabilityDate(List<RelatedObjectsDetails> seamlessObjects, List<HandoverObject> handoverObjects) {

		final var requestDateTime = IntervalsUtil.calculateTimeZoneId(handoverObjects);

		final var relatedObjectWithMinDistance = seamlessObjects.stream()
				.min(Comparator.comparingDouble(RelatedObjectsDetails::getDistance));

		var startTime = seamlessProperties.getStartTime();
		var endTime = seamlessProperties.getEndTime();
		var deliveryTime = seamlessProperties.getDeliveryTime();
		if (relatedObjectWithMinDistance.isPresent()) {
			deliveryTime = relatedObjectWithMinDistance.get().getLeadTime();
		}
		var returnTime = seamlessProperties.getDeliveryReturnTime();

		return CalculateTimeUtils.seamlessCalculateDateAndTime(requestDateTime, deliveryTime, returnTime, startTime, endTime);
	}
}
