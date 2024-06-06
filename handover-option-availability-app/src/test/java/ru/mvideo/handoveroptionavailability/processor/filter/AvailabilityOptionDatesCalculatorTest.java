package ru.mvideo.handoveroptionavailability.processor.filter;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;
import reactor.test.StepVerifier;
import ru.mvideo.handoveroptionavailability.model.HandoverOption;
import ru.mvideo.handoveroptionavailability.model.external.msp.logistic.response.AvailabilityOption;
import ru.mvideo.handoveroptionavailability.processor.model.DeliveryContext;
import ru.mvideo.handoveroptionavailability.processor.model.OptionContext;

@ExtendWith(MockitoExtension.class)
class AvailabilityOptionDatesCalculatorTest {

	@InjectMocks
	private AvailabilityOptionDatesCalculator<DeliveryContext> processor;

	@DisplayName("Есть 2 товара на 1 объекте выдачи с разных стоков на разные даты. Товары на стоке доступны не только с той датой что в ответе msp но и позже")
	@Test
	void test_executeProcessor() {
		DeliveryContext context = createContext(List.of(createOptionContext()));
		final var result = processor.executeProcessor(context);

		StepVerifier.create(result)
				.consumeNextWith(ctx -> assertEquals(6,
						context.handoverOptionContext().get(HandoverOption.INTERVAL_DELIVERY.getValue()).getAvailabilityOptions().size()))
				.verifyComplete();
	}

	private OptionContext createOptionContext() {
		OptionContext optionContext = new OptionContext();
		optionContext.setHandoverOption(HandoverOption.INTERVAL_DELIVERY.getValue());
		optionContext.setAvailabilityOptions(getAvailabilityOptions());
		return optionContext;
	}

	private DeliveryContext createContext(List<OptionContext> optionContexts) {
		Map<String, OptionContext> handoverOptionContext = new HashMap<>();
		for (OptionContext optionContext : optionContexts) {
			handoverOptionContext.put(optionContext.getHandoverOption(), optionContext);
		}

		return DeliveryContext.builder()
				.requestHandoverOptions(Set.of(HandoverOption.INTERVAL_DELIVERY.getValue()))
				.handoverOptionContext(handoverOptionContext)
				.build();
	}

	private List<AvailabilityOption> getAvailabilityOptions() {
		AvailabilityOption option1 = new AvailabilityOption();
		option1.setHandoverObject("S11");
		option1.setStockObject("C1");
		option1.setMaterial("20037891");
		option1.setAvailableStock(5);
		option1.setShowCaseStock(0);
		option1.setAvailableDate(LocalDate.now());

		AvailabilityOption option2 = new AvailabilityOption();
		option2.setHandoverObject("S11");
		option2.setStockObject("C2");
		option2.setMaterial("50044074");
		option2.setAvailableStock(3);
		option2.setShowCaseStock(0);
		option2.setAvailableDate(LocalDate.now().plusDays(1));

		AvailabilityOption option3 = new AvailabilityOption();
		option3.setHandoverObject("S11");
		option3.setStockObject("C3");
		option3.setMaterial("50044074");
		option3.setAvailableStock(5);
		option3.setShowCaseStock(0);
		option3.setAvailableDate(LocalDate.now().plusDays(2));

		return List.of(option1, option2, option3);
	}
}