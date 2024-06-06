package ru.mvideo.handoveroptionavailability.controller.v1;

import java.io.IOException;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.RegisterExtension;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.test.web.reactive.server.WebTestClient;
import ru.mvideo.handoveroptionavailability.AbstractIT;
import ru.mvideo.handoveroptionavailability.MessageLoader;
import ru.mvideo.handoveroptionavailability.model.AvailableHandoverOptionRequest;
import ru.mvideo.handoveroptionavailability.model.HandoverOption;
import ru.mvideo.handoveroptionavailability.model.HandoverOptionType;
import ru.mvideo.handoveroptionavailability.model.PickupRequest;
import ru.mvideo.handoveroptionavailability.scenarios.HandoverOptionsScenarios;
import ru.mvideo.handoveroptionavailability.scenarios.PickupOptionsScenarios;

class HandoverOptionControllerV1Test extends AbstractIT {
	@Autowired
	private WebTestClient webClient;
	@Autowired
	private MessageLoader messageLoader;
	@RegisterExtension
	@Autowired
	protected HandoverOptionsScenarios handoverOptionsScenarios;
	@RegisterExtension
	@Autowired
	protected PickupOptionsScenarios pickupOptionsScenarios;

	@Disabled
	@Test
	void shouldReturnHandoverOptions() throws IOException {
		handoverOptionsScenarios.configureSuccessfulResponses();

		webClient
				.post()
				.uri("/api/v1/handover-options")
				.contentType(MediaType.APPLICATION_JSON)
				.bodyValue(messageLoader.loadObject("requests/v1/handover-options/request.json", AvailableHandoverOptionRequest.class))
				.exchange()
				.expectStatus().isOk()
				.expectBody()
				.jsonPath("$[0].type").isEqualTo(HandoverOptionType.DELIVERY.getValue())
				.jsonPath("$[1].type").isEqualTo(HandoverOptionType.PICKUP.getValue());
	}

	@Disabled
	@Test
	void shouldReturnPickupSeamless() throws IOException {
		pickupOptionsScenarios.configureSeamlessResponses();

		webClient
				.post()
				.uri("/api/v1/handover-options/pickup")
				.contentType(MediaType.APPLICATION_JSON)
				.bodyValue(messageLoader.loadObject("requests/v1/pickup/request.json", PickupRequest.class))
				.exchange()
				.expectStatus().isOk()
				.expectBody()
				.jsonPath("$[0].handoverOption").isEqualTo(HandoverOption.PICKUP_SEAMLESS.getValue())
				.jsonPath("$[0].minPrice").isEqualTo(0)
				.jsonPath("$[0].handoverOptionMaterial").isEqualTo("6016405")
				.jsonPath("$[0].availableAt.length()").isEqualTo(23);
	}
}
