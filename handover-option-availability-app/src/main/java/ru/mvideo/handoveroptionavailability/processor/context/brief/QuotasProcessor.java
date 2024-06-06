package ru.mvideo.handoveroptionavailability.processor.context.brief;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;
import ru.mvideo.handoveroptionavailability.model.HandoverOption;
import ru.mvideo.handoveroptionavailability.model.Material;
import ru.mvideo.handoveroptionavailability.model.external.msp.logistic.response.AvailabilityOption;
import ru.mvideo.handoveroptionavailability.processor.BaseProcessor;
import ru.mvideo.handoveroptionavailability.processor.model.BriefAndPickupContext;
import ru.mvideo.handoveroptionavailability.service.external.msp.quotas.QuotasService;

@Component
@RequiredArgsConstructor
public class QuotasProcessor extends BaseProcessor<BriefAndPickupContext> {

	private final QuotasService quotasService;

	@Override
	protected Mono<BriefAndPickupContext> executeProcessor(BriefAndPickupContext context) {
		return Mono.defer(() -> {
			final var optionContext = context.handoverOptionContext().get(HandoverOption.INTERVAL_DELIVERY.getValue());
			final var availabilityOptions = optionContext.getAvailabilityOptions();

			final var dates = new ArrayList<LocalDate>();
			for (Material material : context.materials()) {
				availabilityOptions.stream()
						.filter(option -> option.getMaterial().equals(material.getMaterial()))
						.map(AvailabilityOption::getAvailableDate)
						.min(LocalDate::compareTo)
						.ifPresent(dates::add);
			}

			final var localDate = findNearestDate(dates);

			return quotasService.getQuotas(context.materials(), context.regionId(), context.retailBrand(), localDate)
					.map(response -> {
						if (response == null || response.getResponseBody() == null
								|| response.getResponseBody().getAvailableQuotes() == null) {
							context.disableOption(HandoverOption.INTERVAL_DELIVERY.getValue(), "Empty response from quota-service");
							return context;
						}
						context.quotas(response);
						return context;
					});
		}).subscribeOn(Schedulers.boundedElastic());
	}

	private LocalDate findNearestDate(List<LocalDate> dates) {
		LocalDate date = null;
		for (LocalDate availabilityDate : dates) {
			if (date == null || date.compareTo(availabilityDate) < 0) {
				date = availabilityDate;
			}
		}
		return date;
	}

	@Override
	public boolean shouldRun(BriefAndPickupContext context) {
		return context.hasOption(HandoverOption.INTERVAL_DELIVERY.getValue());
	}
}
