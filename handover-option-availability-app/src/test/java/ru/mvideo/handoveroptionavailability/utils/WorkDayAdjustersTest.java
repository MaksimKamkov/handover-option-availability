package ru.mvideo.handoveroptionavailability.utils;

import java.time.LocalDateTime;
import java.time.LocalTime;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

public class WorkDayAdjustersTest {

	@Test
	public void shouldRoundRequestTime() {
		final var ceilTime = LocalTime.of(10, 26).with(WorkDayAdjusters.roundRequestTime());
		Assertions.assertEquals(LocalTime.of(10, 0), ceilTime);

		final var floorTime = LocalTime.of(10, 36).with(WorkDayAdjusters.roundRequestTime());
		Assertions.assertEquals(LocalTime.of(11, 0), floorTime);
	}

	@Test
	public void shouldReturnStartOfCurrentDayPeriod() {
		final var requestBeforeWorkTime = LocalTime.of(9, 0).with(WorkDayAdjusters.startCurrentWorkDay(10, 20));
		Assertions.assertEquals(LocalTime.of(12, 0), requestBeforeWorkTime);

		final var requestInWorkTime = LocalTime.of(14, 0).with(WorkDayAdjusters.startCurrentWorkDay(10, 20));
		Assertions.assertEquals(LocalTime.of(16, 0), requestInWorkTime);

		final var requestAfterWorkTime = LocalTime.of(19, 0).with(WorkDayAdjusters.startCurrentWorkDay(10, 20));
		Assertions.assertNull(requestAfterWorkTime);
	}

	@Test
	public void shouldReturnEndOfCurrentDayPeriod() {
		final var requestBeforeWorkTime = LocalTime.of(9, 0).with(WorkDayAdjusters.endWorkDay(20));
		Assertions.assertEquals(LocalTime.of(18, 0), requestBeforeWorkTime);

		final var requestInWorkTime = LocalTime.of(14, 0).with(WorkDayAdjusters.endWorkDay(20));
		Assertions.assertEquals(LocalTime.of(18, 0), requestInWorkTime);

		final var requestAfterWorkTime = LocalTime.of(19, 0).with(WorkDayAdjusters.endWorkDay(20));
		Assertions.assertEquals(LocalTime.of(18, 0), requestAfterWorkTime);
	}

	@Test
	public void shouldReturnStartOfNextDayPeriod() {
		Assertions.assertEquals(LocalTime.of(12, 0), LocalTime.of(14, 0).with(WorkDayAdjusters.startNextWorkDay(10)));
	}

	@Test
	public void shouldReturnEndOfNextDayPeriod() {
		Assertions.assertEquals(LocalTime.of(18, 0), LocalTime.of(14, 0).with(WorkDayAdjusters.endNextWorkDay(20)));
	}

	@Test
	void calculateDeliveryTimeWithRequestTimeAfterStartTime() {
		LocalDateTime requestTime = LocalDateTime.of(2022, 4, 4, 12, 0);
		LocalTime startTime = LocalTime.of(0, 0);
		LocalTime endTime = LocalTime.of(23, 59, 59);
		int deliveryDuration = 120;

		LocalDateTime actual = requestTime.with(WorkDayAdjusters.calculateDeliveryTime(startTime, endTime, deliveryDuration));

		Assertions.assertNotNull(actual);
		Assertions.assertEquals(requestTime.toLocalTime().plusMinutes(deliveryDuration), actual.toLocalTime());
	}

	@Test
	void calculateDeliveryTimeWithRequestTimeBeforeStartTime() {
		LocalDateTime requestTime = LocalDateTime.of(2022, 4, 4, 12, 0);
		LocalTime startTime = LocalTime.of(14, 0);
		LocalTime endTime = LocalTime.of(23, 59, 59);
		int deliveryDuration = 120;

		LocalDateTime actual = requestTime.with(WorkDayAdjusters.calculateDeliveryTime(startTime, endTime, deliveryDuration));

		Assertions.assertNotNull(actual);
		Assertions.assertEquals(startTime.plusMinutes(deliveryDuration), actual.toLocalTime());
	}


	@Test
	void calculateDeliveryTimeWithRequestTimeAfterEndTime() {
		LocalDateTime requestTime = LocalDateTime.of(2022, 4, 4, 15, 0);
		LocalTime startTime = LocalTime.of(14, 0);
		LocalTime endTime = LocalTime.of(17, 0);
		int deliveryDuration = 120;

		LocalDateTime actual = requestTime.with(WorkDayAdjusters.calculateDeliveryTime(startTime, endTime, deliveryDuration));

		Assertions.assertNull(actual);
	}
}
