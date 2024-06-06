package ru.mvideo.handoveroptionavailability.processor.response.option.stock;

import java.util.stream.Collectors;
import ru.mvideo.handoveroptionavailability.model.MaterialStockObject;
import ru.mvideo.handoveroptionavailability.model.StockObjectsResponseItem;
import ru.mvideo.handoveroptionavailability.processor.model.StockObjectsContext;
import ru.mvideo.handoveroptionavailability.processor.response.option.CommonHandoverOptionService;

public abstract class StockObjectsHandoverOptionService extends CommonHandoverOptionService<StockObjectsContext, StockObjectsResponseItem> {

	@Override
	protected StockObjectsResponseItem prepareResponse(StockObjectsContext context) {
		final var material = context.currentMaterial();

		final var stocks = context.availabilityOptions().stream()
				.filter(option -> material.getMaterial().equals(option.getMaterial()))
				.map(option -> MaterialStockObject.builder()
						.objectId(option.getStockObject())
						.qty(option.getAvailableStock())
						.showcaseQty(option.getShowCaseStock())
						.prepaid(option.isPrepaidOnly())
						.availableDate(option.getAvailableDate())
						.priority(option.getStockObjectPriority())
						.build())
				.collect(Collectors.toList());

		return StockObjectsResponseItem.builder()
				.material(material.getMaterial())
				.stocks(stocks)
				.build();
	}
}
