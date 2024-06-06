package ru.mvideo.handoveroptionavailability.processor.flow;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;
import ru.mvideo.handoveroptionavailability.processor.context.AvailabilityOptionsProcessor;
import ru.mvideo.handoveroptionavailability.processor.context.stock.LoadPickupPointsByIdProcessor;
import ru.mvideo.handoveroptionavailability.processor.filter.stock.PickupPartnerStockFilterProcessor;
import ru.mvideo.handoveroptionavailability.processor.model.StockObjectsContext;

@RequiredArgsConstructor
@Component
public class StockObjectsFlow implements Flow<StockObjectsContext> {
	private final AvailabilityOptionsProcessor<StockObjectsContext> availabilityOptionsProcessor;
	private final LoadPickupPointsByIdProcessor pickupPointsProcessor;
	private final PickupPartnerStockFilterProcessor pickupPartnerFilterProcessor;

	@Override
	public Mono<StockObjectsContext> process(StockObjectsContext context) {
		return availabilityOptionsProcessor.process(context)
				.flatMap(pickupPointsProcessor::process)
				.flatMap(pickupPartnerFilterProcessor::process);
	}
}
