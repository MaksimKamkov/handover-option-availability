package ru.mvideo.handoveroptionavailability.processor;

import java.time.LocalTime;
import java.time.temporal.ChronoField;
import java.time.temporal.ChronoUnit;
import java.time.temporal.Temporal;
import java.time.temporal.TemporalAdjuster;

public class IntervalDeliveryValidation {

	public static void main(String[] args) {
		System.out.println(LocalTime.of(10, 10).with(new A()));
		System.out.println(LocalTime.of(10, 31).with(new A()));
	}

	static class A implements TemporalAdjuster {

		@Override
		public Temporal adjustInto(Temporal temporal) {
			final var minute = temporal.get(ChronoField.MINUTE_OF_HOUR);
			if (minute < 30) {
				return temporal.with(ChronoField.MINUTE_OF_HOUR, 0);
			} else {
				return temporal.with(ChronoField.MINUTE_OF_HOUR, 0).plus(1, ChronoUnit.HOURS);
			}
		}
	}
}

