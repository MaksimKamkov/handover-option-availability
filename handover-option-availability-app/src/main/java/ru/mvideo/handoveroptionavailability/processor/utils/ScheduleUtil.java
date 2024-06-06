package ru.mvideo.handoveroptionavailability.processor.utils;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.util.Set;
import lombok.experimental.UtilityClass;
import org.apache.commons.collections4.CollectionUtils;

@UtilityClass
public class ScheduleUtil {

	public LocalDate closestWorkDay(LocalDate date, Set<DayOfWeek> schedule) {
		if (CollectionUtils.isEmpty(schedule)) {
			return date;
		}

		LocalDate check = date;
		while (!schedule.contains(check.getDayOfWeek())) {
			check = check.plusDays(1L);
		}
		return check;
	}
}
