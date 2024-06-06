package ru.mvideo.handoveroptionavailability.processor.filter;

import java.util.Collections;
import java.util.Map;
import java.util.Set;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import reactor.test.StepVerifier;
import ru.mvideo.handoveroptionavailability.model.HandoverOption;
import ru.mvideo.handoveroptionavailability.processor.model.DeliveryContext;
import ru.mvideo.handoveroptionavailability.processor.model.OptionContext;

@ExtendWith(MockitoExtension.class)
public class AvailabilityOptionFilterTest {

	private final AvailabilityOptionFilter<DeliveryContext> processor = new AvailabilityOptionFilter<>();

	@Test
	public void shouldDisableOption() {
		final var context = DeliveryContext.builder()
				.requestHandoverOptions(Set.of(HandoverOption.ETA_DELIVERY.getValue(), HandoverOption.EXACTLY_TIME_DELIVERY.getValue()))
				.handoverOptionContext(Map.of(
						HandoverOption.ETA_DELIVERY.getValue(), new OptionContext(),
						HandoverOption.EXACTLY_TIME_DELIVERY.getValue(), new OptionContext()
				))
				.build();
		context.providers(Collections.emptyList());
		context.availabilityOptions(Collections.emptyList());

		StepVerifier.create(processor.executeProcessor(context))
				.assertNext(c -> Assertions.assertTrue(context.options().isEmpty()))
				.verifyComplete();
	}
}
