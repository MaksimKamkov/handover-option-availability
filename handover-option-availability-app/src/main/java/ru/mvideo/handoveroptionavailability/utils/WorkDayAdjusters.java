package ru.mvideo.handoveroptionavailability.utils;

import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.temporal.ChronoField;
import java.time.temporal.ChronoUnit;
import java.time.temporal.TemporalAdjuster;

public final class WorkDayAdjusters {

	private WorkDayAdjusters() {
	}

	public static TemporalAdjuster roundRequestTime() {
		return (temporal) -> {
			final var minute = temporal.get(ChronoField.MINUTE_OF_HOUR);
			if (minute < 30) {
				return temporal.with(ChronoField.MINUTE_OF_HOUR, 0);
			} else {
				return temporal.with(ChronoField.MINUTE_OF_HOUR, 0).plus(1, ChronoUnit.HOURS);
			}
		};
	}

	public static TemporalAdjuster startCurrentWorkDay(int fromHour, int toHour) {
		return (temporal) -> {
			final var requestHour = temporal.get(ChronoField.HOUR_OF_DAY);
			if (requestHour < fromHour) {
				return temporal.with(ChronoField.HOUR_OF_DAY, fromHour)
						.plus(2, ChronoUnit.HOURS);
			} else {
				if (requestHour + 2 < toHour - 2) {
					return temporal.plus(2, ChronoUnit.HOURS);
				} else {
					return null;
				}
			}
		};
	}

	public static TemporalAdjuster startNextWorkDay(int fromHour) {
		return (temporal) -> temporal.with(ChronoField.HOUR_OF_DAY, fromHour)
				.plus(2, ChronoUnit.HOURS);
	}

	public static TemporalAdjuster endWorkDay(int toHour) {
		return (temporal) -> temporal.with(ChronoField.HOUR_OF_DAY, toHour)
				.minus(2, ChronoUnit.HOURS);
	}

	public static TemporalAdjuster endNextWorkDay(int toHour) {
		return (temporal) -> temporal.with(ChronoField.HOUR_OF_DAY, toHour)
				.minus(2, ChronoUnit.HOURS);
	}

	public static TemporalAdjuster calculateDeliveryTime(LocalTime startTime, LocalTime endTime, Integer deliveryDuration) {
		return temporal -> {
			var requestDateTime = LocalDateTime.from(temporal);
			var now = requestDateTime.toLocalDate();
			var todayRequestTime = LocalDateTime.of(now, startTime);

			if (requestDateTime.isBefore(todayRequestTime)) {
				return todayRequestTime.plusMinutes(deliveryDuration);
			} else {
				var todayDeliveryEndTime = LocalDateTime.of(now, endTime.minusMinutes(deliveryDuration));
				var deliveryTime = requestDateTime.plusMinutes(deliveryDuration);

				if (deliveryTime.isBefore(todayDeliveryEndTime)) {
					return deliveryTime;
				} else {
					return null;
				}
			}
		};
	}

}
