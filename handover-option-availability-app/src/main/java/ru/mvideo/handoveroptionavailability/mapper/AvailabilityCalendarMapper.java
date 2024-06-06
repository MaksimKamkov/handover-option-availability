package ru.mvideo.handoveroptionavailability.mapper;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.stereotype.Component;
import ru.mvideo.handoveroptionavailability.model.external.msp.logistic.response.AvailabilityOption;
import ru.mvideo.handoveroptionavailability.model.external.msp.logistic.responsenextcalendar.AvailabilityOptionNextCalendar;

@RequiredArgsConstructor
@Component
public class AvailabilityCalendarMapper {

	private final AvailabilityOptionMapper mapper;

	public List<AvailabilityOption> map(AvailabilityOptionNextCalendar calendar) {
		if (CollectionUtils.isEmpty(calendar.getAvailabilityDates())) {
			return Collections.emptyList();
		}

		return calendar.getAvailabilityDates().stream()
				.map(date -> mapper.map(calendar, date))
				.collect(Collectors.toList());
	}
}
