package ru.mvideo.handoveroptionavailability.processor.response.option.delivery;

import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;
import java.util.stream.Collectors;
import ru.mvideo.handoveroptionavailability.model.AvailableDate;
import ru.mvideo.handoveroptionavailability.model.DeliveryResponse;
import ru.mvideo.handoveroptionavailability.model.DetailedApplicableTo;
import ru.mvideo.handoveroptionavailability.model.PaymentMethod;
import ru.mvideo.handoveroptionavailability.model.ReservationAvailableAt;
import ru.mvideo.handoveroptionavailability.processor.model.DeliveryContext;
import ru.mvideo.handoveroptionavailability.processor.model.Flags;
import ru.mvideo.handoveroptionavailability.processor.model.OptionContext;
import ru.mvideo.handoveroptionavailability.processor.response.option.CommonHandoverOptionService;

public abstract class DetailedHandoverOptionService extends CommonHandoverOptionService<DeliveryContext, DeliveryResponse> {

	protected LocalDate getAvailabilityDateForAvailableMaterials(List<ReservationAvailableAt> availableAt) {
		//сбор самых ранних дат доставки товара
		final var earliestMaterialAvailabilityDates = new HashMap<String, LocalDate>();
		for (ReservationAvailableAt at : availableAt) {
			for (AvailableDate availableDate : at.getAvailableDates()) {
				final var date = availableDate.getDate();
				for (DetailedApplicableTo applicableTo : availableDate.getApplicableTo()) {
					final var minDate = earliestMaterialAvailabilityDates.get(applicableTo.getMaterial());
					if (minDate == null || minDate.compareTo(date) > 0) {
						earliestMaterialAvailabilityDates.put(applicableTo.getMaterial(), date);
					}
				}
			}
		}

		//получение максимальной даты, это дата начиная с которой будут доступны все товары
		return earliestMaterialAvailabilityDates.values().stream().max(LocalDate::compareTo).orElse(null);
	}

	protected List<PaymentMethod> paymentConditions(DeliveryContext context, OptionContext optionContext) {
		List<PaymentMethod> paymentConditions = null;
		if (context.flags().contains(Flags.RETURN_PAYMENT_CONDITIONS)) {
			paymentConditions = optionContext.getPaymentConditions().stream()
					.map(PaymentMethod::fromValue)
					.collect(Collectors.toList());
		}
		return paymentConditions;
	}
}
