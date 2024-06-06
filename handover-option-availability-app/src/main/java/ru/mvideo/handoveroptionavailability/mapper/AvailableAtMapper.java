package ru.mvideo.handoveroptionavailability.mapper;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import lombok.Getter;
import org.springframework.stereotype.Component;
import ru.mvideo.availability_chains.model.RelatedObjectsDetails;
import ru.mvideo.handoveroptionavailability.model.AvailableDate;
import ru.mvideo.handoveroptionavailability.model.DetailedApplicableTo;
import ru.mvideo.handoveroptionavailability.model.PickupAvailableAt;
import ru.mvideo.handoveroptionavailability.model.ReservationAvailableAt;
import ru.mvideo.handoveroptionavailability.model.StockValue;
import ru.mvideo.handoveroptionavailability.model.external.msp.logistic.response.AvailabilityOption;

@Component
public class AvailableAtMapper {

	public List<PickupAvailableAt> toPickupSeamlessAvailableAt(Map<RelatedObjectsDetails, List<AvailabilityOption>> availabilityOptions,
	                                                           boolean includeStocks) {
		final var result = new ArrayList<PickupAvailableAt>();
		for (Map.Entry<RelatedObjectsDetails, List<AvailabilityOption>> entry : availabilityOptions.entrySet()) {
			final var recipient = entry.getKey().getObjectRecipient();
			final var source = entry.getKey().getObjectSource();
			final var options = entry.getValue();
			//https://jira.mvideo.ru/jira/browse/CS-4206 Для pickup-seamless флаг prepaid должны быть всегда true
			options.forEach(option -> option.setPrepaidOnly(true));
			final var availableMaterials = toAvailabilityDates(options, source, includeStocks);

			final var availableAt = new PickupAvailableAt();
			availableAt.setHandoverObject(recipient);
			availableAt.setAvailableMaterials(availableMaterials);
			result.add(availableAt);
		}
		return result;
	}

	public List<PickupAvailableAt> toPickupAvailableAt(List<AvailabilityOption> availabilityOptions, boolean includeStocks) {
		final var groupedOptions = availabilityOptions.stream()
				.collect(Collectors.groupingBy(AvailabilityOption::getHandoverObject));

		final var result = new ArrayList<PickupAvailableAt>();
		for (Map.Entry<String, List<AvailabilityOption>> entry : groupedOptions.entrySet()) {
			final var handoverObject = entry.getKey();
			final var options = entry.getValue();
			final var availableMaterials = toAvailabilityDates(options, handoverObject, includeStocks);

			final var availableAt = new PickupAvailableAt();
			availableAt.setHandoverObject(handoverObject);
			availableAt.setAvailableMaterials(availableMaterials);
			result.add(availableAt);
		}
		return result;
	}

	public List<ReservationAvailableAt> toDeliveryAvailableAt(List<AvailabilityOption> availabilityOptions, boolean includeStocks) {
		final var groupedOptions = availabilityOptions.stream()
				.collect(Collectors.groupingBy(AvailabilityOption::getHandoverObject));

		final var result = new ArrayList<ReservationAvailableAt>();
		for (Map.Entry<String, List<AvailabilityOption>> entry : groupedOptions.entrySet()) {
			final var handoverObject = entry.getKey();
			final var options = entry.getValue();
			final var availableMaterials = toAvailabilityDates(options, handoverObject, includeStocks);

			var reservationAvailableAt = new ReservationAvailableAt();
			reservationAvailableAt.setHandoverObject(handoverObject);
			reservationAvailableAt.setAvailableDates(availableMaterials);
			result.add(reservationAvailableAt);
		}
		return result;
	}

	private List<AvailableDate> toAvailabilityDates(List<AvailabilityOption> dateMap, String handoverObject, boolean includeStocks) {
		final var optionByDate = dateMap.stream()
				.collect(Collectors.groupingBy(AvailabilityOption::getAvailableDate));

		final var availableDates = new ArrayList<AvailableDate>();
		for (Map.Entry<LocalDate, List<AvailabilityOption>> entry : optionByDate.entrySet()) {
			final var date = entry.getKey();
			final var options = entry.getValue();
			final var applicableTo = toApplicableList(options, handoverObject, includeStocks);

			final var availableDate = new AvailableDate();
			availableDate.setDate(date);
			availableDate.setApplicableTo(applicableTo);
			availableDates.add(availableDate);
		}

		availableDates.sort(Comparator.comparing(AvailableDate::getDate));
		return availableDates;
	}

	private List<DetailedApplicableTo> toApplicableList(List<AvailabilityOption> availabilityOptions, String handoverObject,
	                                                    boolean includeStocks) {
		final var groupedByMaterial = availabilityOptions.stream()
				.collect(Collectors.groupingBy(AvailabilityOption::getMaterial));

		final var details = new ArrayList<DetailedApplicableTo>();
		for (Map.Entry<String, List<AvailabilityOption>> entry : groupedByMaterial.entrySet()) {
			final var stocks = includeStocks ? new HashMap<String, StockValue>()
					: Collections.<String, StockValue>emptyMap();
			final var counters = new Counters();

			for (AvailabilityOption option : entry.getValue()) {
				counters.increment(option, handoverObject);

				if (includeStocks) {
					final var stock = new StockValue();
					stock.setQty(option.getAvailableStock());
					stock.setPrepaid(option.isPrepaidOnly());
					stock.setPriority(option.getStockObjectPriority());
					stock.setShowcaseQty(option.getShowCaseStock());
					stocks.put(option.getStockObject(), stock);
				}
			}

			final var material = entry.getKey();

			final var detail = new DetailedApplicableTo();
			detail.setStocks(stocks);
			detail.setMaterial(material);
			detail.setQty(counters.getQty());
			detail.setShowcaseQty(counters.getShowcaseQty());
			detail.setPrepaidQty(counters.getPrepaidQty());
			detail.setHandoverObjectQty(counters.getHandoverObjectQty());
			details.add(detail);
		}

		return details;
	}

	@Getter
	static class Counters {
		int qty;
		int handoverObjectQty;
		int prepaidQty;
		int showcaseQty;

		public void increment(AvailabilityOption option, String handoverObject) {
			qty += option.getAvailableStock() + option.getShowCaseStock();
			showcaseQty += option.getShowCaseStock();
			if (option.isPrepaidOnly()) {
				prepaidQty += option.getAvailableStock() + option.getShowCaseStock();
			}
			if (handoverObject.equals(option.getStockObject())) {
				handoverObjectQty += option.getAvailableStock() + option.getShowCaseStock();
			}
		}
	}
}
