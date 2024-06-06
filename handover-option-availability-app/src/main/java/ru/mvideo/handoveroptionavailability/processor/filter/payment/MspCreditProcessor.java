package ru.mvideo.handoveroptionavailability.processor.filter.payment;

import java.time.Duration;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;
import ru.mvideo.handoveroptionavailability.config.CreditMaxDeliveryDaysProperties;
import ru.mvideo.handoveroptionavailability.model.external.msp.logistic.response.AvailabilityOption;
import ru.mvideo.handoveroptionavailability.processor.BaseProcessor;
import ru.mvideo.handoveroptionavailability.processor.filter.fit.ExtendedProduct;
import ru.mvideo.handoveroptionavailability.processor.model.Context;
import ru.mvideo.handoveroptionavailability.processor.model.Flags;

@Component
@RequiredArgsConstructor
public class MspCreditProcessor<T extends Context> extends BaseProcessor<T> {

	private final CreditMaxDeliveryDaysProperties creditMaxDeliveryDaysProperties;

	@Override
	protected Mono<T> executeProcessor(T context) {
		return Mono.fromCallable(() -> {
			final var now = LocalDate.now();
			final var approveCreditLeadTime = Duration.ofMinutes(context.creditApprovalLeadTime()).toDays();
			final var maxCreditTime = now.plusDays(creditMaxDeliveryDaysProperties.getMaxDeliveryDays());
			final var minStock = context.minStock();

			for (String option : context.options()) {
				final var optionContext = context.handoverOptionContext().get(option);
				if (optionContext.getPaymentConditions().contains("CREDIT")) {

					List<AvailabilityOption> creditAvailabilityOptions = new ArrayList<>();

					for (ExtendedProduct product : optionContext.getProducts()) {

						final var productAvailabilityOptions = optionContext.getAvailabilityOptions().stream()
								.filter(opt -> product.getProduct().getProductId().equals(opt.getMaterial()))
								.filter(opt -> opt.getAvailableStock() - minStock > product.getQty())
								.collect(Collectors.toList());

						if (productAvailabilityOptions.isEmpty()) {
							continue;
						}

						final var filteredDateOptions = productAvailabilityOptions.stream()
								.filter(opt -> now.plusDays(approveCreditLeadTime).isBefore(opt.getValidTo().toLocalDate()))
								.collect(Collectors.toList());

						if (filteredDateOptions.isEmpty()) {
							final var availabilityOptionMaxDate = productAvailabilityOptions.stream()
									.max(Comparator.comparing(AvailabilityOption::getAvailableDate)).get();
							final var unusedDays = approveCreditLeadTime
									- ChronoUnit.DAYS.between(availabilityOptionMaxDate.getValidTo().toLocalDate(), now);
							final var newDate = availabilityOptionMaxDate.getAvailableDate().plusDays(unusedDays);
							availabilityOptionMaxDate.setAvailableDate(newDate);
							filteredDateOptions.add(availabilityOptionMaxDate);
						}

						final var beforeMaxCreditTimeOptions = filteredDateOptions.stream()
								.filter(availabilityOption -> availabilityOption.getAvailableDate().isBefore(maxCreditTime)
										|| availabilityOption.getAvailableDate().isEqual(maxCreditTime))
								.collect(Collectors.toList());
						creditAvailabilityOptions.addAll(beforeMaxCreditTimeOptions);
					}

					if ("CREDIT".equals(context.paymentMethod())) {
						if (creditAvailabilityOptions.isEmpty()) {
							context.disableOption(option, "No valid calendars for credit");
						} else {
							final var collect = creditAvailabilityOptions.stream()
									.map(opt -> {
										opt.setAvailableStock(opt.getAvailableStock() - minStock);
										opt.setShowCaseStock(0);
										return opt;
									})
									.collect(Collectors.toList());
							optionContext.setAvailabilityOptions(collect);
						}
					} else if (context.flags().contains(Flags.RETURN_PAYMENT_CONDITIONS)) {
						if (creditAvailabilityOptions.isEmpty()) {
							final var notCreditConditions = optionContext.getPaymentConditions().stream()
									.filter(condition -> !"CREDIT".equals(condition))
									.collect(Collectors.toList());
							optionContext.setPaymentConditions(notCreditConditions);
						}
					}
				}
			}
			return context;
		});
	}

	@Override
	public boolean shouldRun(T context) {
		return ("CREDIT".equals(context.paymentMethod()) || context.flags().contains(Flags.RETURN_PAYMENT_CONDITIONS)) && checkCredit(context);
	}

	private boolean checkCredit(T context) {
		for (String option : context.options()) {
			final var optionContext = context.handoverOptionContext().get(option);
			if (optionContext.getPaymentConditions().stream().anyMatch("CREDIT"::equals)) {
				return true;
			}
		}
		return false;
	}
}
