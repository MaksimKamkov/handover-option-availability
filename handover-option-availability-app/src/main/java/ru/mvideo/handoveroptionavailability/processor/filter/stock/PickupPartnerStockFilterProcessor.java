package ru.mvideo.handoveroptionavailability.processor.filter.stock;

import static ru.mvideo.handoveroptionavailability.service.StocksAndShowcase.pickupPartnerAvailabilityOptions;

import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;
import ru.mvideo.handoveroptionavailability.model.HandoverOption;
import ru.mvideo.handoveroptionavailability.model.Material;
import ru.mvideo.handoveroptionavailability.processor.BaseProcessor;
import ru.mvideo.handoveroptionavailability.processor.model.StockObjectsContext;
import ru.mvideo.handoveroptionavailability.utils.SapCodeUtils;

@Component
@RequiredArgsConstructor
public class PickupPartnerStockFilterProcessor extends BaseProcessor<StockObjectsContext> {

	@Override
	protected Mono<StockObjectsContext> executeProcessor(StockObjectsContext context) {
		return Mono.fromCallable(() -> {
			final var sapCodes = SapCodeUtils.collectPickupPointsSapCodes(context.pickupPoints(), context.retailBrand());
			final var materials = context.materials().stream()
					.collect(Collectors.toMap(Material::getMaterial, Material::getQty));

			final var options = pickupPartnerAvailabilityOptions(context.availabilityOptions(), materials).stream()
					.filter(option -> sapCodes.contains(option.getHandoverObject()))
					.collect(Collectors.toList());

			if (options.isEmpty()) {
				context.disableOption(HandoverOption.PICKUP_PARTNER.getValue(), "Stores do not contain items to deliver the entire order");
			}

			return context;
		});
	}

	@Override
	public boolean shouldRun(StockObjectsContext context) {
		return context.hasOption(HandoverOption.PICKUP_PARTNER.getValue());
	}
}
