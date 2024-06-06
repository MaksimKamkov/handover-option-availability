package ru.mvideo.handoveroptionavailability.service;

import java.time.LocalDate;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import lombok.experimental.UtilityClass;
import ru.mvideo.handoveroptionavailability.model.external.msp.logistic.response.AvailabilityOption;
import ru.mvideo.handoveroptionavailability.processor.filter.fit.ExtendedProduct;
import ru.mvideo.handoveroptionavailability.processor.model.HandoverObject;

@UtilityClass
public class StocksAndShowcase {

	public static List<AvailabilityOption> pickupPartnerAvailabilityOptions(List<AvailabilityOption> availabilityOptions,
	                                                                        Map<String, Integer> products) {
		final var options = availabilityOptions.stream()
				.filter(option -> products.containsKey(option.getMaterial()))
				.filter(option -> option.getAvailableStock() >= products.get(option.getMaterial()))
				.collect(Collectors.toList());

		final var availabilityOptionsInOneStockObject = getAvailabilityOptionsInOneStockObject(options, products.size());
		return getAvailabilityOptionsForOneDate(availabilityOptionsInOneStockObject, products.size());
	}

	public static List<AvailabilityOption> seamlessAvailabilityOptions(List<AvailabilityOption> availabilityOptions,
	                                                                   List<ExtendedProduct> products,
	                                                                   List<HandoverObject> handoverObjects) {
		final var productQtyMap = products.stream()
				.collect(Collectors.toMap(orderContext -> orderContext.getProduct().getProductId(), ExtendedProduct::getQty));

		Set<String> zoneItemsHandoverObjectIds = handoverObjects.stream()
				.map(HandoverObject::getObjectId)
				.collect(Collectors.toSet());

		final var filterAvailabilityOptions = availabilityOptions.stream()
				.filter(option -> option.getHandoverObject().equals(option.getStockObject()))
				.filter(option -> zoneItemsHandoverObjectIds.contains(option.getHandoverObject()))
				.filter(option -> option.getAvailableStock() >= productQtyMap.get(option.getMaterial()))
				.filter(option -> option.getValidTo().toLocalDate().equals(LocalDate.now()))
				.filter(option -> option.getAvailableDate().equals(LocalDate.now()))
				.collect(Collectors.toList());

		return getAvailabilityOptionsInOneStockObject(filterAvailabilityOptions, products.size());
	}

	public List<AvailabilityOption> exactlyAvailabilityOptions(List<AvailabilityOption> availabilityOptions,
	                                                           List<ExtendedProduct> products,
	                                                           boolean isShowcaseDeliveryAvailable) {
		final var productQty = products.stream()
				.collect(Collectors.toMap(orderContext -> orderContext.getProduct().getProductId(), ExtendedProduct::getQty));

		final var availabilityOptionsOnHandoverObject = availabilityOptions.stream()
				.filter(option -> option.getHandoverObject().equals(option.getStockObject()))
				.collect(Collectors.toList());

		final var stockAvailabilityOptions = availabilityOptionsOnHandoverObject.stream()
				.filter(option -> option.getAvailableStock() >= productQty.get(option.getMaterial()))
				.filter(option -> option.getValidTo().toLocalDate().equals(LocalDate.now())
						|| option.getValidTo().toLocalDate().equals(LocalDate.now().plusDays(1)))
				.filter(option -> option.getAvailableDate().equals(LocalDate.now())
						|| option.getAvailableDate().equals(LocalDate.now().plusDays(1)))
				.collect(Collectors.toList());

		final var stocks = getAvailabilityOptionsInOneStockObject(stockAvailabilityOptions, products.size());

		if (!stocks.isEmpty()) {
			return stocks;
		}

		if (isShowcaseDeliveryAvailable) {
			final var stockAndShowcaseAvailabilityOptions = availabilityOptionsOnHandoverObject.stream()
					.filter(option -> option.getAvailableStock() + option.getShowCaseStock() >= productQty.get(option.getMaterial()))
					.filter(option -> option.getAvailableDate().equals(LocalDate.now())
							|| option.getAvailableDate().equals(LocalDate.now().plusDays(1)))
					.filter(option -> option.getValidTo().toLocalDate().equals(LocalDate.now())
							|| option.getValidTo().toLocalDate().equals(LocalDate.now().plusDays(1)))
					.collect(Collectors.toList());

			return getAvailabilityOptionsInOneStockObject(stockAndShowcaseAvailabilityOptions, products.size());
		}

		return Collections.emptyList();
	}

	public List<AvailabilityOption> etaAvailabilityOptions(List<AvailabilityOption> availabilityOptions,
	                                                       List<ExtendedProduct> products,
	                                                       boolean isShowcaseDeliveryAvailable) {
		final var productQty = products.stream()
				.collect(Collectors.toMap(orderContext -> orderContext.getProduct().getProductId(), ExtendedProduct::getQty));

		final var availabilityOptionsOnHandoverObject = availabilityOptions.stream()
				.filter(option -> option.getHandoverObject().equals(option.getStockObject()))
				.filter(option -> option.getValidTo().toLocalDate().equals(LocalDate.now()))
				.filter(option -> option.getAvailableDate().equals(LocalDate.now()))
				.collect(Collectors.toList());

		final var stockAvailabilityOptions = availabilityOptionsOnHandoverObject.stream()
				.filter(option -> option.getAvailableStock() >= productQty.get(option.getMaterial()))
				.collect(Collectors.toList());

		final var stockAndShowcaseAvailabilityOptions = availabilityOptionsOnHandoverObject.stream()
				.filter(option -> option.getAvailableStock() + option.getShowCaseStock() >= productQty.get(option.getMaterial()))
				.collect(Collectors.toList());

		final var stocks = getAvailabilityOptionsInOneStockObject(stockAvailabilityOptions, products.size());

		return (stocks.isEmpty() && isShowcaseDeliveryAvailable)
				? getAvailabilityOptionsInOneStockObject(stockAndShowcaseAvailabilityOptions, products.size())
				: stocks;
	}

	public List<AvailabilityOption> etaPreorderAvailabilityOptions(List<AvailabilityOption> availabilityOptions,
	                                                               List<ExtendedProduct> products,
	                                                               boolean isShowcaseDeliveryAvailable) {
		final var productQty = products.stream()
				.collect(Collectors.toMap(orderContext -> orderContext.getProduct().getProductId(), ExtendedProduct::getQty));

		final var availabilityOptionsOnHandoverObject = availabilityOptions.stream()
				.filter(option -> option.getHandoverObject().equals(option.getStockObject()))
				.filter(option -> option.getValidTo().toLocalDate().equals(LocalDate.now()))
				.filter(option -> option.getAvailableDate().equals(LocalDate.now().plusDays(1)))
				.filter(option -> option.getPreorderPhase() == null || "open-sale".equals(option.getPreorderPhase()))
				.collect(Collectors.toList());

		final var stockAvailabilityOptions = availabilityOptionsOnHandoverObject.stream()
				.filter(option -> option.getAvailableStock() >= productQty.get(option.getMaterial()))
				.collect(Collectors.toList());

		final var stockAndShowcaseAvailabilityOptions = availabilityOptionsOnHandoverObject.stream()
				.filter(option -> option.getAvailableStock() + option.getShowCaseStock() >= productQty.get(option.getMaterial()))
				.collect(Collectors.toList());

		final var stocks = getAvailabilityOptionsInOneStockObject(stockAvailabilityOptions, products.size());

		return (stocks.isEmpty() && isShowcaseDeliveryAvailable)
				? getAvailabilityOptionsInOneStockObject(stockAndShowcaseAvailabilityOptions, products.size())
				: stocks;
	}

	private List<AvailabilityOption> getAvailabilityOptionsForOneDate(List<AvailabilityOption> availabilityOptions, int size) {

		final var groupedOptions = availabilityOptions.stream()
				.collect(Collectors.groupingBy(
						AvailabilityOption::getAvailableDate,
						Collectors.mapping(
								option -> option,
								Collectors.toSet())
						)
				);

		return groupedOptions.values().stream()
				.filter(value -> allMaterialsInOneHandoverObject(value, size))
				.flatMap(Collection::stream)
				.collect(Collectors.toList());
	}

	private List<AvailabilityOption> getAvailabilityOptionsInOneStockObject(List<AvailabilityOption> availabilityOptions, int size) {
		final var groupedOptions = availabilityOptions.stream()
				.collect(Collectors.groupingBy(
						AvailabilityOption::getStockObject,
						Collectors.mapping(
								option -> option,
								Collectors.toSet())
						)
				);

		return groupedOptions.values().stream()
				.filter(value -> allMaterialsInOneHandoverObject(value, size))
				.flatMap(Collection::stream)
				.collect(Collectors.toList());
	}

	private boolean allMaterialsInOneHandoverObject(Set<AvailabilityOption> availabilityOptions, int size) {
		return availabilityOptions.stream()
				.map(AvailabilityOption::getMaterial)
				.collect(Collectors.toSet()).size() == size;
	}
}
