package ru.mvideo.handoveroptionavailability.processor.response.option.brief;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Service;
import ru.mvideo.handoveroptionavailability.config.CreditMaxDeliveryDaysProperties;
import ru.mvideo.handoveroptionavailability.mapper.BriefResponseMapper;
import ru.mvideo.handoveroptionavailability.model.AvailableInterval;
import ru.mvideo.handoveroptionavailability.model.BriefApplicableTo;
import ru.mvideo.handoveroptionavailability.model.BriefOption;
import ru.mvideo.handoveroptionavailability.model.HandoverOption;
import ru.mvideo.handoveroptionavailability.processor.model.BriefAndPickupContext;
import ru.mvideo.msp.quota.model.AvailableQuota;
import ru.mvideo.msp.quota.model.QuotaAvailabilityResponse;

@Order(2)
@Slf4j
@Service
@RequiredArgsConstructor
public class IntervalBriefService extends BriefDeliveryHandoverOptionService {

	private final BriefResponseMapper responseMapper;
	private final CreditMaxDeliveryDaysProperties creditMaxDeliveryDaysProperties;

	@Override
	protected boolean support(BriefAndPickupContext context) {
		return context.hasOption(HandoverOption.INTERVAL_DELIVERY.getValue());
	}

	@Override
	protected BriefOption prepareResponse(BriefAndPickupContext context) {
		final var optionContext = context.handoverOptionContext().get(HandoverOption.INTERVAL_DELIVERY.getValue());

		QuotaAvailabilityResponse quotas = context.quotas();

		List<AvailableInterval> intervals = new ArrayList<>();
		Optional<Double> expressPrice = getExpress(quotas);
		if (expressPrice.isPresent()) {
			final var availableInterval = new AvailableInterval();
			availableInterval.setMinPrice(expressPrice.get());
			availableInterval.setAvailabilityDate(LocalDate.now());
			availableInterval.setType("express");
			intervals.add(availableInterval);
		} else {
			log.info("No express quotes from quota-service for interval-delivery");
		}

		Optional<AvailableQuota> regularPrice = getRegular(quotas);
		if (regularPrice.isPresent()) {
			final var availableInterval = new AvailableInterval();
			availableInterval.setMinPrice(regularPrice.get().getPrice());
			availableInterval.setAvailabilityDate(regularPrice.get().getDate());
			availableInterval.setType("regular");
			intervals.add(availableInterval);
		} else {
			log.info("No regular quotes from quota-service for interval-delivery");
		}

		LocalDate maxDeliveryDate = null;
		if (context.paymentMethod() != null && context.paymentMethod().contains("CREDIT")) {
			maxDeliveryDate = LocalDate.now().plusDays(creditMaxDeliveryDaysProperties.getMaxDeliveryDays());
		}

		final var response = new BriefOption();
		response.setHandoverOption(HandoverOption.fromValue(optionContext.getHandoverOption()));

		response.setApplicableTo(responseMapper.toApplicableTo(optionContext.getAvailabilityOptions()));
		response.setAvailabilityDate(getAvailabilityDate(response.getApplicableTo()));

		response.setAvailableIntervals(intervals);
		response.setMinPrice(getMinPrice(intervals));

		response.setMaxDeliveryDate(maxDeliveryDate);
		response.setPaymentConditions(paymentConditions(context, optionContext));
		return response;
	}

	private LocalDate getAvailabilityDate(List<BriefApplicableTo> applicableTo) {
		LocalDate date = null;
		for (BriefApplicableTo applicable : applicableTo) {
			final var availabilityDate = applicable.getAvailabilityDate();
			if (date == null || date.compareTo(availabilityDate) < 0) {
				date = availabilityDate;
			}
		}
		return date;
	}

	private Double getMinPrice(List<AvailableInterval> intervals) {
		return intervals.stream()
				.map(AvailableInterval::getMinPrice)
				.min(Double::compareTo)
				.orElse(null);
	}

	private Optional<Double> getExpress(QuotaAvailabilityResponse response) {
		return response.getResponseBody().getAvailableQuotes()
				.stream()
				.filter(quota -> quota.getDate().equals(LocalDate.now())
						&& quota.getQuotaParams().stream()
						.anyMatch(param -> "duration".equals(param.getKey()) && "0".equals(param.getValue()))
				)
				.map(AvailableQuota::getPrice)
				.min(Double::compareTo);
	}

	private Optional<AvailableQuota> getRegular(QuotaAvailabilityResponse response) {
		return response.getResponseBody().getAvailableQuotes()
				.stream()
				.filter(quota -> quota.getQuotaParams().stream()
						.anyMatch(param -> "duration".equals(param.getKey()) && "1".equals(param.getValue()))
				)
				.min(Comparator.comparing(AvailableQuota::getPrice));
	}
}
