package ru.mvideo.handoveroptionavailability.processor.response;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Flux;
import ru.mvideo.handoveroptionavailability.config.CreditMaxDeliveryDaysProperties;
import ru.mvideo.handoveroptionavailability.model.AccessoriesItem;
import ru.mvideo.handoveroptionavailability.model.AccessoriesStock;
import ru.mvideo.handoveroptionavailability.model.external.msp.logistic.response.AvailabilityOption;
import ru.mvideo.handoveroptionavailability.processor.model.AccessoriesContext;
import ru.mvideo.handoveroptionavailability.utils.GroupingUtils;

@Slf4j
@Component
@RequiredArgsConstructor
public class AccessoriesResponseProducer implements ResponseProducer<Flux<AccessoriesItem>, AccessoriesContext> {

	private final CreditMaxDeliveryDaysProperties creditMaxDeliveryDaysProperties;

	@Override
	public Flux<AccessoriesItem> produce(AccessoriesContext context) {

		final var now = LocalDate.now();
		final var maxDeliveryDays = creditMaxDeliveryDaysProperties.getMaxDeliveryDays();
		final var maxDeliveryDate = now.plusDays(maxDeliveryDays);

		List<AccessoriesItem> accessoriesItems = new ArrayList<>();

		for (String opt : context.options()) {
			final var optionContext = context.handoverOptionContext().get(opt);

			final var materialHandoverObjectAvailabilityOption =
					GroupingUtils.toMaterialHandoverObject(optionContext.getAvailabilityOptions());

			accessoriesItems.addAll(toAccessoriesItems(materialHandoverObjectAvailabilityOption, maxDeliveryDate));

		}
		return Flux.fromIterable(accessoriesItems);
	}

	private List<AccessoriesItem> toAccessoriesItems(Map<String, Map<String, AvailabilityOption>> map, LocalDate maxDeliveryDate) {
		List<AccessoriesItem> accessoriesItems = new ArrayList<>();

		for (Map.Entry<String, Map<String, AvailabilityOption>> entry : map.entrySet()) {
			final var accessoriesItem = new AccessoriesItem();
			accessoriesItem.material(entry.getKey());
			accessoriesItem.setMaxDeliveryDate(maxDeliveryDate);
			accessoriesItem.stocks(toAccessoriesStocks(entry.getValue()));
			accessoriesItems.add(accessoriesItem);
		}

		return accessoriesItems;
	}

	private List<AccessoriesStock> toAccessoriesStocks(Map<String, AvailabilityOption> map) {
		List<AccessoriesStock> stocks = new ArrayList<>();
		for (Map.Entry<String, AvailabilityOption> entry : map.entrySet()) {
			final var accessoriesStock = toAccessoriesStock(entry.getValue());
			stocks.add(accessoriesStock);
		}
		return stocks;
	}

	private AccessoriesStock toAccessoriesStock(AvailabilityOption option) {
		final var accessoriesStock = new AccessoriesStock();
		accessoriesStock.setObjectId(option.getStockObject());
		accessoriesStock.setAvailableDate(option.getAvailableDate());
		accessoriesStock.setQty(option.getAvailableStock());
		accessoriesStock.setShowcaseQty(option.getShowCaseStock());
		accessoriesStock.setPrepaid(option.isPrepaidOnly());
		accessoriesStock.setPriority(option.getStockObjectPriority());
		return accessoriesStock;
	}
}
