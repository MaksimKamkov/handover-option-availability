package ru.mvideo.handoveroptionavailability.utils;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import lombok.experimental.UtilityClass;
import ru.mvideo.handoveroptionavailability.model.AvailableInterval;
import ru.mvideo.handoveroptionavailability.model.external.msp.logistic.response.AvailabilityOption;
import ru.mvideo.handoveroptionavailability.processor.model.HandoverObject;

@UtilityClass
public class CalculateTimeUtils {

	private static final int DEFAULT_TIME_ZONE = 3;
	//CS-3569 Для опции "exactly-time-delivery" интервалы буду отдаваться с 11 до 22 (вместо с 12 до 20)
	//Принимается константа, что любой магазин любого региона ежедневно работает с 10:00 до 22:00
	private static final LocalTime START_DAY_TIME = LocalTime.of(9, 0, 0);
	private static final LocalTime END_DAY_TIME = LocalTime.of(22, 0, 0);

	public static LocalDateTime seamlessCalculateDateAndTime(LocalDateTime requestDateTime,
	                                                         Integer deliveryTime,
	                                                         Integer returnTime,
	                                                         LocalTime startTime,
	                                                         LocalTime endTime) {

		var startDay = LocalDateTime.of(requestDateTime.toLocalDate(), startTime);
		var endDay = LocalDateTime.of(requestDateTime.toLocalDate(), endTime).minusMinutes(returnTime);

		if (requestDateTime.isBefore(startDay)) {
			requestDateTime = LocalDateTime.of(requestDateTime.toLocalDate(), startTime.plusMinutes(deliveryTime));
		} else {
			var deliveryDateTime = requestDateTime.plusMinutes(deliveryTime);
			if (deliveryDateTime.isAfter(endDay)) {
				requestDateTime = LocalDateTime.of(requestDateTime.toLocalDate().plusDays(1), startTime.plusMinutes(deliveryTime));
			} else {
				requestDateTime = deliveryDateTime;
			}
		}

		var requestLocalTime = requestDateTime.toLocalTime();
		if (requestLocalTime.getMinute() >= 1 && requestLocalTime.getMinute() <= 29) {
			requestLocalTime = LocalTime.of(requestLocalTime.getHour(), 30);
		} else if (requestLocalTime.getMinute() >= 31) {
			requestLocalTime = LocalTime.of(requestLocalTime.getHour() + 1, 0);
		}

		return LocalDateTime.of(requestDateTime.toLocalDate(), requestLocalTime);
	}

	public static List<AvailableInterval> calculateAvailableIntervals(List<AvailabilityOption> availabilityOptions,
	                                                                  Double minPrice,
	                                                                  LocalTime requestTime) {

		if (requestTime.getMinute() >= 30) {
			requestTime = requestTime.plusHours(1);
		}
		requestTime = requestTime.truncatedTo(ChronoUnit.HOURS);

		var timeFrom = START_DAY_TIME;
		//CS-3569 Для опции "exactly-time-delivery" интервалы буду отдаваться с 11 до 22 (вместо с 12 до 20)
		var timeTo = END_DAY_TIME;//время на обратную дорогу, сейчас убрано

		var dtf = DateTimeFormatter.ofPattern("HH:mm");

		List<AvailableInterval> availableIntervals = new ArrayList<>();

		if (requestTime.isBefore(timeFrom)) {
			timeFrom = timeFrom.plusHours(2);
		} else {
			var deliveryTime = requestTime.plusHours(2);
			if (deliveryTime.equals(LocalTime.of(0, 0))) {
				deliveryTime = LocalTime.of(23, 59, 59);
			}
			if (deliveryTime.isAfter(timeTo)) {
				timeFrom = null;
			} else {
				timeFrom = requestTime.plusHours(2);
			}
		}

		boolean existDateNow = availabilityOptions.stream()
				.anyMatch(option -> option.getAvailableDate().equals(LocalDate.now()));

		if (timeFrom != null && existDateNow && timeFrom != timeTo) {
			var availableIntervalToday = new AvailableInterval();
			availableIntervalToday.setMinPrice(minPrice);
			availableIntervalToday.setAvailabilityDate(LocalDate.now());
			availableIntervalToday.setDateTimeFrom(timeFrom.format(dtf));
			availableIntervalToday.setDateTimeTo(timeTo.format(dtf));
			availableIntervals.add(availableIntervalToday);
		}

		boolean existDateTomorrow = availabilityOptions.stream()
				.anyMatch(option -> option.getAvailableDate().equals(LocalDate.now().plusDays(1)));

		if (existDateTomorrow) {
			var availableIntervalToday = new AvailableInterval();
			availableIntervalToday.setMinPrice(minPrice);
			availableIntervalToday.setAvailabilityDate(LocalDate.now().plusDays(1));
			availableIntervalToday.setDateTimeFrom(LocalTime.of(12, 0).toString());
			availableIntervalToday.setDateTimeTo(timeTo.toString());
			availableIntervals.add(availableIntervalToday);
		}
		return availableIntervals;
	}

	public ZoneId calculateTimeZoneId(List<HandoverObject> zoneObjects) {
		return zoneObjects.stream()
				.map(HandoverObject::getTimeZone)
				.filter(Objects::nonNull)
				.findAny()
				.orElse(ZoneId.ofOffset("UTC", ZoneOffset.ofHours(DEFAULT_TIME_ZONE)));
	}
}
