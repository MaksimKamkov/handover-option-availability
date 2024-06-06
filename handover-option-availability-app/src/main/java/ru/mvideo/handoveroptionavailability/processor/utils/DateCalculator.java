package ru.mvideo.handoveroptionavailability.processor.utils;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import lombok.experimental.UtilityClass;
import ru.mvideo.handoveroptionavailability.model.external.msp.logistic.response.AvailabilityOption;

@UtilityClass
public class DateCalculator {

	public List<AvailabilityOption> enrichAvailabilityOptionDates(List<AvailabilityOption> availabilityOptions) {

		final var maxDate = availabilityOptions.stream()
				.map(AvailabilityOption::getAvailableDate)
				.max(LocalDate::compareTo)
				.orElse(LocalDate.now());

		final var result = new ArrayList<AvailabilityOption>();

		final var handoverMap = availabilityOptions.stream()
				.collect(Collectors.groupingBy(AvailabilityOption::getHandoverObject));

		for (Map.Entry<String, List<AvailabilityOption>> entry : handoverMap.entrySet()) {

			final var materialMap = entry.getValue().stream()
					.collect(Collectors.groupingBy(AvailabilityOption::getMaterial));

			for (Map.Entry<String, List<AvailabilityOption>> entry2 : materialMap.entrySet()) {

				final var stockMap = entry2.getValue().stream()
						.collect(Collectors.groupingBy(AvailabilityOption::getStockObject));

				for (List<AvailabilityOption> optionList : stockMap.values()) {
					result.addAll(addDates(optionList, maxDate));
				}
			}
		}
		return result;
	}

	private List<AvailabilityOption> addDates(List<AvailabilityOption> availabilityOptions, LocalDate maxDate) {
		List<AvailabilityOption> result = new ArrayList<>();

		final var option = availabilityOptions.get(0);

		final var minDate = availabilityOptions.stream()
				.map(AvailabilityOption::getAvailableDate)
				.min(LocalDate::compareTo)
				.orElse(LocalDate.now());

		long duration = ChronoUnit.DAYS.between(option.getAvailableDate(), maxDate);

		for (long i = 0; i <= duration; i++) {
			final var newOption = option.toBuilder().build();
			newOption.setAvailableDate(minDate.plusDays(i));
			result.add(newOption);
		}

		return result;
	}
}
