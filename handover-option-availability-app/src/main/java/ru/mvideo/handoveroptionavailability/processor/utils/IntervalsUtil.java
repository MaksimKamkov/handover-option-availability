package ru.mvideo.handoveroptionavailability.processor.utils;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;
import lombok.experimental.UtilityClass;
import ru.mvideo.handoveroptionavailability.processor.model.HandoverObject;
import ru.mvideo.handoveroptionavailability.utils.CalculateTimeUtils;

@UtilityClass
public class IntervalsUtil {

	public LocalDateTime calculateTimeZoneId(List<HandoverObject> handoverObjects) {
		final var zoneId = CalculateTimeUtils.calculateTimeZoneId(handoverObjects);
		return LocalDateTime.of(LocalDate.now(), LocalTime.now(zoneId));
	}
}
