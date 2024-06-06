package ru.mvideo.handoveroptionavailability.processor.filter;

import static org.junit.jupiter.api.Assertions.assertEquals;

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
import ru.mvideo.handoveroptionavailability.processor.model.HandoverObject;
import ru.mvideo.handoveroptionavailability.processor.model.OptionContext;
import ru.mvideo.lards.price.rules.model.MinPriceResponse;

@ExtendWith(MockitoExtension.class)
public class ObjectsInDeliveryRadiusProcessorTest {

	@InjectMocks
	private ObjectsInDeliveryRadiusProcessor<DeliveryContext> processor;

	@DisplayName("Все магазины в радиусе")
	@Test
	void executeProcessor() {

		DeliveryContext context = createContext(List.of(createOption()));
		final var result = processor.executeProcessor(context);

		StepVerifier.create(result)
				.consumeNextWith(ctx -> {
					assertEquals(2, context.handoverOptionContext().get(HandoverOption.ETA_DELIVERY.getValue()).getAvailabilityOptions().size());
					assertEquals(1, context.options().size());
				})
				.verifyComplete();
	}

	@DisplayName("Не все магазины в радиусе")
	@Test
	void executeProcessor1() {

		DeliveryContext context = createContext(List.of(createOption1()));
		final var result = processor.executeProcessor(context);

		StepVerifier.create(result)
				.consumeNextWith(ctx -> {
					assertEquals(0, context.handoverOptionContext().get(HandoverOption.ETA_DELIVERY.getValue()).getAvailabilityOptions().size());
					assertEquals(0, context.options().size());
				})
				.verifyComplete();
	}

	private MinPriceResponse createRule() {
		var rule = new MinPriceResponse();
		rule.setMinRadius(10.0);
		rule.setMaxRadius(20.0);
		return rule;
	}

	private List<HandoverObject> getHandoverObjects() {
		HandoverObject object1 = new HandoverObject();
		object1.setObjectId("S11");
		object1.setDistance(15.0);
		HandoverObject object2 = new HandoverObject();
		object2.setObjectId("S22");
		object2.setDistance(17.0);

		return List.of(object1, object2);
	}

	private List<HandoverObject> getHandoverObjects1() {
		HandoverObject object1 = new HandoverObject();
		object1.setObjectId("S11");
		object1.setDistance(21.0);
		HandoverObject object2 = new HandoverObject();
		object2.setObjectId("S22");
		object2.setDistance(27.0);

		return List.of(object1, object2);
	}

	private List<AvailabilityOption> getAvailabilityOptions() {
		AvailabilityOption option1 = new AvailabilityOption();
		option1.setHandoverObject("S11");
		AvailabilityOption option2 = new AvailabilityOption();
		option2.setHandoverObject("S22");
		return List.of(option1, option2);
	}

	private OptionContext createOption1() {
		OptionContext optionContext = new OptionContext();
		optionContext.setHandoverOption(HandoverOption.ETA_DELIVERY.getValue());
		var rule = createRule();
		optionContext.setMinPriceRule(rule);

		optionContext.setAvailabilityOptions(getAvailabilityOptions());

		optionContext.setHandoverObjects(getHandoverObjects1());

		return optionContext;
	}

	private OptionContext createOption() {
		OptionContext optionContext = new OptionContext();
		optionContext.setHandoverOption(HandoverOption.ETA_DELIVERY.getValue());
		var rule = createRule();
		optionContext.setMinPriceRule(rule);

		optionContext.setAvailabilityOptions(getAvailabilityOptions());

		optionContext.setHandoverObjects(getHandoverObjects());

		return optionContext;
	}

	private DeliveryContext createContext(List<OptionContext> optionContexts) {
		Map<String, OptionContext> handoverOptionContext = new HashMap<>();
		for (OptionContext optionContext : optionContexts) {
			handoverOptionContext.put(optionContext.getHandoverOption(), optionContext);
		}

		return DeliveryContext.builder()
				.requestHandoverOptions(Set.of(HandoverOption.ETA_DELIVERY.getValue()))
				.handoverOptionContext(handoverOptionContext)
				.build();
	}
}