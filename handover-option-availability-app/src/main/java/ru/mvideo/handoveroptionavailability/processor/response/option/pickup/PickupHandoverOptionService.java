package ru.mvideo.handoveroptionavailability.processor.response.option.pickup;

import java.util.List;
import java.util.stream.Collectors;
import ru.mvideo.handoveroptionavailability.model.PaymentMethod;
import ru.mvideo.handoveroptionavailability.model.PickupResponseItem;
import ru.mvideo.handoveroptionavailability.processor.model.BriefAndPickupContext;
import ru.mvideo.handoveroptionavailability.processor.model.Flags;
import ru.mvideo.handoveroptionavailability.processor.model.OptionContext;
import ru.mvideo.handoveroptionavailability.processor.response.option.CommonHandoverOptionService;

public abstract class PickupHandoverOptionService extends CommonHandoverOptionService<BriefAndPickupContext, PickupResponseItem> {

	protected List<PaymentMethod> paymentConditions(BriefAndPickupContext context, OptionContext optionContext) {
		List<PaymentMethod> paymentConditions = null;
		if (context.flags().contains(Flags.RETURN_PAYMENT_CONDITIONS)) {
			paymentConditions = optionContext.getPaymentConditions().stream()
					.map(PaymentMethod::fromValue)
					.collect(Collectors.toList());
		}
		return paymentConditions;
	}
}
