package ru.mvideo.handoveroptionavailability.processor.flow;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;
import ru.mvideo.handoveroptionavailability.processor.context.AvailabilityOptionsProcessor;
import ru.mvideo.handoveroptionavailability.processor.filter.AvailabilityOptionFilter;
import ru.mvideo.handoveroptionavailability.processor.filter.accessories.MspDeliveryDateFilterProcessor;
import ru.mvideo.handoveroptionavailability.processor.filter.payment.MspCreditProcessor;
import ru.mvideo.handoveroptionavailability.processor.filter.payment.credit.NoDataForCreditProcessor;
import ru.mvideo.handoveroptionavailability.processor.filter.payment.credit.ZoneApprovalLeadTimeProcessor;
import ru.mvideo.handoveroptionavailability.processor.model.AccessoriesContext;

@RequiredArgsConstructor
@Component
public class AccessoriesFlow implements Flow<AccessoriesContext> {

	private final ZoneApprovalLeadTimeProcessor<AccessoriesContext> zoneApprovalLeadTimeProcessor;
	private final NoDataForCreditProcessor<AccessoriesContext> noDataForCreditProcessor;
	private final MspCreditProcessor<AccessoriesContext> mspCreditProcessor;
	private final MspDeliveryDateFilterProcessor mspDeliveryDateFilterProcessor;

	private final AvailabilityOptionsProcessor<AccessoriesContext> availabilityOptionsProcessor;
	private final AvailabilityOptionFilter<AccessoriesContext> availabilityOptionFilter;

	@Override
	public Mono<AccessoriesContext> process(AccessoriesContext context) {
		return zoneApprovalLeadTimeProcessor.process(context)
				.flatMap(noDataForCreditProcessor::process)
				.flatMap(availabilityOptionsProcessor::process)
				.flatMap(availabilityOptionFilter::process)
				.flatMap(mspCreditProcessor::process)
				.flatMap(mspDeliveryDateFilterProcessor::process);
	}
}
