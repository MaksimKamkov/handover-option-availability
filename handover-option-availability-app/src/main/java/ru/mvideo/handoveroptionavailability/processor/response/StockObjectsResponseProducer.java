package ru.mvideo.handoveroptionavailability.processor.response;

import java.util.List;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Flux;
import ru.mvideo.handoveroptionavailability.model.Material;
import ru.mvideo.handoveroptionavailability.model.StockObjectsResponseItem;
import ru.mvideo.handoveroptionavailability.processor.model.StockObjectsContext;
import ru.mvideo.handoveroptionavailability.processor.response.option.stock.StockObjectsHandoverOptionService;

@Component
@RequiredArgsConstructor
public class StockObjectsResponseProducer implements ResponseProducer<Flux<StockObjectsResponseItem>, StockObjectsContext> {
	private final List<StockObjectsHandoverOptionService> services;

	@Override
	public Flux<StockObjectsResponseItem> produce(StockObjectsContext context) {
		return Flux.create(sink -> {
			final var materials = context.materials();
			for (Material material : materials) {
				context.currentMaterial(material);
				services.stream()
						.map(service -> service.getOption(context))
						.filter(Optional::isPresent)
						.map(Optional::get)
						.forEach(sink::next);
			}
			sink.complete();
		});
	}
}
