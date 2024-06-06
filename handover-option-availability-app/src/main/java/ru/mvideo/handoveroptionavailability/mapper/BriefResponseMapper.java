package ru.mvideo.handoveroptionavailability.mapper;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.function.Consumer;
import java.util.stream.Collectors;
import lombok.Getter;
import org.springframework.stereotype.Component;
import ru.mvideo.handoveroptionavailability.model.BriefApplicableTo;
import ru.mvideo.handoveroptionavailability.model.external.msp.logistic.response.AvailabilityOption;
import ru.mvideo.handoveroptionavailability.utils.GroupingUtils;

@Component
public class BriefResponseMapper {

	public List<BriefApplicableTo> toApplicableTo(List<AvailabilityOption> availabilityOptions) {

		Map<String, Map<LocalDate, List<AvailabilityOption>>> materialDateMap =
				GroupingUtils.toMaterialDateMap(availabilityOptions);

		return materialDateMap.entrySet().stream()
				.map(entry -> {
					final var applicableTo = new BriefApplicableTo();
					applicableTo.setMaterial(entry.getKey());

					Optional<Map.Entry<LocalDate, List<AvailabilityOption>>> minDateAvailabilityOptions =
							entry.getValue().entrySet().stream().min(Map.Entry.comparingByKey());

					if (minDateAvailabilityOptions.isEmpty()) {
						return null;
					}

					applicableTo.setAvailabilityDate(minDateAvailabilityOptions.get().getKey());

					var options = minDateAvailabilityOptions.get().getValue();

					final var stockCounter = options.stream()
							.collect(StockCounter::new, StockCounter::accept, StockCounter::combine);
					applicableTo.setQty(stockCounter.getQty() + stockCounter.getShowcaseQty());
					applicableTo.setPrepaidQty(stockCounter.getPrepaidQty());
					applicableTo.setShowcaseQty(stockCounter.getShowcaseQty());

					return applicableTo;
				})
				.filter(Objects::nonNull)
				.collect(Collectors.toList());
	}

	@Getter
	static class StockCounter implements Consumer<AvailabilityOption> {
		private int qty = 0;
		private int prepaidQty = 0;
		private int showcaseQty = 0;

		@Override
		public void accept(AvailabilityOption option) {
			qty += option.getAvailableStock();
			showcaseQty += option.getShowCaseStock();
			if (option.isPrepaidOnly()) {
				prepaidQty += option.getAvailableStock() + option.getShowCaseStock();
			}
		}

		public void combine(StockCounter other) {
			qty += other.qty;
			prepaidQty += other.prepaidQty;
			showcaseQty += other.showcaseQty;
		}
	}
}
