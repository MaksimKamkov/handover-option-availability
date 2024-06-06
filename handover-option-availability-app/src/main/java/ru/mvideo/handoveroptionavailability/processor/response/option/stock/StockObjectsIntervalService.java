package ru.mvideo.handoveroptionavailability.processor.response.option.stock;

import org.springframework.stereotype.Service;
import ru.mvideo.handoveroptionavailability.model.HandoverOption;
import ru.mvideo.handoveroptionavailability.processor.model.StockObjectsContext;

@Service
public class StockObjectsIntervalService extends StockObjectsHandoverOptionService {
	@Override
	protected boolean support(StockObjectsContext context) {
		return context.hasOption(HandoverOption.INTERVAL_DELIVERY.getValue());
	}

}
