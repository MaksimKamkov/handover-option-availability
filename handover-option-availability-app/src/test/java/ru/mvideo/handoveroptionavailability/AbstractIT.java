package ru.mvideo.handoveroptionavailability;

import org.junit.jupiter.api.extension.RegisterExtension;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.reactive.server.WebTestClient;
import ru.mvideo.handoveroptionavailability.scenarios.BatchOptionsScenarios;
import ru.mvideo.handoveroptionavailability.scenarios.DeliveryOptionsScenarios;
import ru.mvideo.handoveroptionavailability.scenarios.HandoverOptionsScenarios;
import ru.mvideo.handoveroptionavailability.scenarios.PickupOptionsScenarios;
import ru.mvideo.handoveroptionavailability.scenarios.StockObjectsScenarios;
import ru.mvideo.lastmile.starter.security.adapter.KAuthSecurityAdapterAutoConfiguration;

@ActiveProfiles("test")
@SpringBootTest(classes = Application.class,
		webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT
)
@EnableAutoConfiguration(exclude = KAuthSecurityAdapterAutoConfiguration.class)
public class AbstractIT {
	@Autowired
	protected WebTestClient webClient;
	@Autowired
	protected MessageLoader messageLoader;
	@RegisterExtension
	@Autowired
	protected BatchOptionsScenarios batchOptionsScenarios;
	@RegisterExtension
	@Autowired
	protected DeliveryOptionsScenarios deliveryScenarios;
	@RegisterExtension
	@Autowired
	protected PickupOptionsScenarios pickupOptionsScenarios;
	@RegisterExtension
	@Autowired
	protected StockObjectsScenarios scenarios;
	@RegisterExtension
	@Autowired
	protected HandoverOptionsScenarios handoverOptionsScenarios;

	protected <T> WebTestClient.ResponseSpec getPostResponse(String uri, T body) {
		return webClient
				.post()
				.uri(uri)
				.contentType(MediaType.APPLICATION_JSON)
				.bodyValue(body)
				.exchange();
	}

	protected WebTestClient.ResponseSpec getGetResponse(String uri) {
		return webClient
				.get()
				.uri(uri)
				.exchange();
	}

	protected <T> WebTestClient.ResponseSpec getPutResponse(String uri, T body) {
		return webClient
				.put()
				.uri(uri)
				.contentType(MediaType.APPLICATION_JSON)
				.bodyValue(body)
				.exchange();
	}

	protected <T> WebTestClient.ResponseSpec getPatchResponse(String uri, T body) {
		return webClient
				.patch()
				.uri(uri)
				.contentType(MediaType.APPLICATION_JSON)
				.bodyValue(body)
				.exchange();
	}

	protected WebTestClient.ResponseSpec getDeleteResponse(String uri) {
		return webClient
				.delete()
				.uri(uri)
				.exchange();
	}

}
