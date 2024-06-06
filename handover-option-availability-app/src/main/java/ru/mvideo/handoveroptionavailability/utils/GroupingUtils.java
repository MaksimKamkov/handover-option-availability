package ru.mvideo.handoveroptionavailability.utils;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collector;
import java.util.stream.Collectors;
import lombok.experimental.UtilityClass;
import ru.mvideo.handoveroptionavailability.model.external.msp.logistic.response.AvailabilityOption;

@UtilityClass
public class GroupingUtils {

	public Map<String, Map<String, AvailabilityOption>> toMaterialHandoverObject(List<AvailabilityOption> availabilityOptions) {
		final var groupedOptions = availabilityOptions.stream()
				.collect(Collectors.groupingBy(AvailabilityOption::getMaterial));

		final var result = new HashMap<String, Map<String, AvailabilityOption>>();

		for (Map.Entry<String, List<AvailabilityOption>> entry : groupedOptions.entrySet()) {
			final var groupedByStockObjectOptions = new HashMap<String, AvailabilityOption>();
			for (AvailabilityOption option : entry.getValue()) {
				groupedByStockObjectOptions.putIfAbsent(option.getStockObject(), option);
			}
			result.put(entry.getKey(), groupedByStockObjectOptions);
		}

		return result;
	}

	public Map<String, Map<LocalDate, List<AvailabilityOption>>> toHandoverObjectDateMap(List<AvailabilityOption> options) {
		return group(options, Collectors.groupingBy(AvailabilityOption::getHandoverObject));
	}

	public Map<String, Map<LocalDate, List<AvailabilityOption>>> toMaterialDateMap(List<AvailabilityOption> options) {
		return group(options, Collectors.groupingBy(AvailabilityOption::getMaterial));
	}

	private Map<String, Map<LocalDate, List<AvailabilityOption>>> group(List<AvailabilityOption> options,
	                                                                    Collector<AvailabilityOption, ?, Map<String, List<AvailabilityOption>>> collector) {
		final var handoverMap = options.stream().collect(collector);

		final var result = new HashMap<String, Map<LocalDate, List<AvailabilityOption>>>();
		for (Map.Entry<String, List<AvailabilityOption>> entry : handoverMap.entrySet()) {
			final var key = entry.getKey();
			final var groupedByDateOptions = new HashMap<LocalDate, List<AvailabilityOption>>();
			for (AvailabilityOption option : entry.getValue()) {
				final var dateOptions = groupedByDateOptions.computeIfAbsent(option.getAvailableDate(), k -> new ArrayList<>());
				final var availabilityOption = dateOptions.stream()
						.filter(o -> o.getMaterial().equals(option.getMaterial()))
						.filter(o -> o.getStockObject().equals(option.getStockObject()))
						.findAny();
				if (availabilityOption.isEmpty()) {
					dateOptions.add(option);
				}

			}
			result.put(key, groupedByDateOptions);
		}
		return result;
	}


}
