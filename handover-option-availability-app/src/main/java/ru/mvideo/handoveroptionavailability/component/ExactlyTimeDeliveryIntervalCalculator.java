package ru.mvideo.handoveroptionavailability.component;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import org.springframework.stereotype.Component;
import ru.mvideo.handoveroptionavailability.config.DeliveryTimeProperties;
import ru.mvideo.handoveroptionavailability.model.AvailableInterval;
import ru.mvideo.handoveroptionavailability.model.external.msp.logistic.response.AvailabilityOption;
import ru.mvideo.handoveroptionavailability.processor.model.HandoverObject;
import ru.mvideo.handoveroptionavailability.processor.utils.IntervalsUtil;
import ru.mvideo.handoveroptionavailability.utils.WorkDayAdjusters;

@Component
public class ExactlyTimeDeliveryIntervalCalculator {

	private final DeliveryTimeProperties exactlyTimeDeliveryProperties;

	//Фактический интервал в который осуществляется доставка
	private final LocalTime deliveryStartTime;
	private final LocalTime deliveryEndTime;

	private static final DateTimeFormatter DATE_TIME_FORMATTER = DateTimeFormatter.ofPattern("HH:mm");
	private static final LocalTime ALL_DAY_END_TIME = LocalTime.of(23, 59, 59);

	public ExactlyTimeDeliveryIntervalCalculator(DeliveryTimeProperties exactlyTimeDeliveryProperties) {
		this.exactlyTimeDeliveryProperties = exactlyTimeDeliveryProperties;
		this.deliveryStartTime = exactlyTimeDeliveryProperties.getStartTime().plusMinutes(exactlyTimeDeliveryProperties.getDeliveryTime());
		this.deliveryEndTime = exactlyTimeDeliveryProperties.getEndTime().minusMinutes(exactlyTimeDeliveryProperties.getDeliveryReturnTime());
	}

	public List<AvailableInterval> toIntervals(List<AvailabilityOption> options, Double minPrice, List<HandoverObject> handoverObjects) {
		final var currentDate = LocalDate.now();
		final var requestDateTime = IntervalsUtil.calculateTimeZoneId(handoverObjects);
		final var roundedTime = requestDateTime.with(WorkDayAdjusters.roundRequestTime());
		final var todayTimeFrom = calculateDeliveryTime(roundedTime);

		final var intervals = new ArrayList<AvailableInterval>();

		AvailableInterval todayInterval = createInterval(options, minPrice, currentDate, todayTimeFrom, deliveryEndTime);
		if (todayInterval != null) {
			intervals.add(todayInterval);
		}

		AvailableInterval tomorrowInterval = createInterval(options, minPrice, currentDate.plusDays(1), deliveryStartTime, deliveryEndTime);
		if (tomorrowInterval != null) {
			intervals.add(tomorrowInterval);
		}

		return intervals;
	}

	public List<AvailableInterval> toIntervalsByWorkingSchedule(List<AvailabilityOption> options, Double minPrice,
	                                                            HandoverObject handoverObject, Integer deliveryDuration) {
		final var currentDate = LocalDate.now();
		final var requestDateTime = IntervalsUtil.calculateTimeZoneId(Collections.singletonList(handoverObject));
		final var todayTimeFrom = requestDateTime
				.with(WorkDayAdjusters.roundRequestTime())
				.with(WorkDayAdjusters.calculateDeliveryTime(
						handoverObject.getWorkStartTime(),
						handoverObject.getWorkEndTime(),
						deliveryDuration
				)).toLocalTime();
		LocalTime deliveryEndTime = ALL_DAY_END_TIME.equals(handoverObject.getWorkEndTime())
				? handoverObject.getWorkEndTime()
				: handoverObject.getWorkEndTime().minusMinutes(deliveryDuration);

		final var intervals = new ArrayList<AvailableInterval>();

		AvailableInterval todayInterval = createInterval(options, minPrice, currentDate, todayTimeFrom, deliveryEndTime);
		if (todayInterval != null) {
			intervals.add(todayInterval);
		}

		AvailableInterval tomorrowInterval = createInterval(
				options,
				minPrice,
				currentDate.plusDays(1),
				handoverObject.getWorkStartTime().plusMinutes(deliveryDuration),
				deliveryEndTime
		);
		if (tomorrowInterval != null) {
			intervals.add(tomorrowInterval);
		}

		return intervals;
	}

	private AvailableInterval createInterval(List<AvailabilityOption> options, Double minPrice, LocalDate currentDate,
	                                         LocalTime timeFrom, LocalTime timeTo) {
		if (timeFrom == null) {
			return null;
		}
		final var isDateAvailable = options.stream().anyMatch(option -> option.getAvailableDate().equals(currentDate));
		if (isDateAvailable) {
			return AvailableInterval.builder()
					.minPrice(minPrice)
					.availabilityDate(currentDate)
					.dateTimeFrom(timeFrom.format(DATE_TIME_FORMATTER))
					.dateTimeTo(timeTo.format(DATE_TIME_FORMATTER))
					.build();
		}
		return null;
	}

	private LocalTime calculateDeliveryTime(LocalDateTime requestDateTime) {
		var now = requestDateTime.toLocalDate();
		var todayRequestTime = LocalDateTime.of(now, exactlyTimeDeliveryProperties.getStartTime());

		if (requestDateTime.isBefore(todayRequestTime)) {
			return deliveryStartTime;
		} else {
			var todayDeliveryEndTime = LocalDateTime.of(now, deliveryEndTime);
			var deliveryTime = requestDateTime.plusMinutes(exactlyTimeDeliveryProperties.getDeliveryTime());

			if (deliveryTime.isBefore(todayDeliveryEndTime)) {
				return deliveryTime.toLocalTime();
			} else {
				return null;
			}
		}
	}
}
