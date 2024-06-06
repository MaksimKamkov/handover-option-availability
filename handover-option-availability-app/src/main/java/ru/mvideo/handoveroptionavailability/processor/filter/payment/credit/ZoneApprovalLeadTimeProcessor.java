package ru.mvideo.handoveroptionavailability.processor.filter.payment.credit;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;
import ru.mvideo.clop.cache.common.Cache;
import ru.mvideo.handoveroptionavailability.config.CreditMaxDeliveryDaysProperties;
import ru.mvideo.handoveroptionavailability.processor.BaseProcessor;
import ru.mvideo.handoveroptionavailability.processor.model.Context;
import ru.mvideo.handoveroptionavailability.processor.model.Flags;
import ru.mvideo.lards.zone.model.PaymentAttributeValue;
import ru.mvideo.lards.zone.model.ZoneAttributePaymentResponse;

@Component
@RequiredArgsConstructor
public class ZoneApprovalLeadTimeProcessor<T extends Context> extends BaseProcessor<T> {

	private final Cache<String, ZoneAttributePaymentResponse> zoneCreditApprovalLeadTime;
	private final CreditMaxDeliveryDaysProperties creditMaxDeliveryDaysProperties;

	@Override
	protected Mono<T> executeProcessor(T context) {
		return zoneCreditApprovalLeadTime.get(context.regionId())
				.doOnSuccess(response -> {
					if (response != null && response.getAttributes() != null && !response.getAttributes().isEmpty()) {
						for (PaymentAttributeValue attribute : response.getAttributes()) {
							if ("creditApprovalLeadTime".equals(attribute.getName())) {
								var creditApprovalLeadTime = Integer.parseInt(attribute.getValue());
								context.creditApprovalLeadTime(creditApprovalLeadTime);
							}
						}
					}
					context.minStock(creditMaxDeliveryDaysProperties.getMinStock());
				}).then(Mono.just(context));
	}

	@Override
	public boolean shouldRun(T context) {
		return (context.flags().contains(Flags.RETURN_PAYMENT_CONDITIONS) || context.paymentMethod() != null) && checkCredit(context);
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
