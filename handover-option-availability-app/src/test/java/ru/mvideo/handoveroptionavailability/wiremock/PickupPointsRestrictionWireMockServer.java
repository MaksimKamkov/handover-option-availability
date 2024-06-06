package ru.mvideo.handoveroptionavailability.wiremock;

import static com.github.tomakehurst.wiremock.client.WireMock.aResponse;
import static com.github.tomakehurst.wiremock.client.WireMock.post;
import static com.github.tomakehurst.wiremock.client.WireMock.urlPathEqualTo;
import static com.github.tomakehurst.wiremock.core.WireMockConfiguration.options;
import static org.springframework.http.HttpHeaders.CONTENT_TYPE;
import static org.springframework.http.HttpStatus.OK;
import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;

import com.github.tomakehurst.wiremock.WireMockServer;
import java.io.IOException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import org.apache.commons.lang3.StringUtils;
import org.junit.jupiter.api.extension.BeforeEachCallback;
import org.junit.jupiter.api.extension.ExtensionContext;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import ru.mvideo.handoveroptionavailability.MessageLoader;

@Component
public class PickupPointsRestrictionWireMockServer implements BeforeEachCallback, InitializingBean {

	@Value("${ru.mvideo.lards.pickup-point-restriction.host}")
	private String host;

	@Autowired
	private MessageLoader messageLoader;

	private WireMockServer wireMockServer;

	@Override
	public void beforeEach(ExtensionContext extensionContext) throws Exception {
		wireMockServer.stop();
		wireMockServer.resetAll();
	}

	@Override
	public void afterPropertiesSet() throws Exception {
		final var port = Integer.parseInt(StringUtils.substringAfterLast(host, ":"));
		wireMockServer = new WireMockServer(options().bindAddress("localhost").port(port));
	}

	public PickupPointsRestrictionWireMockServer.MockConfiguration newConfiguration() {
		return new PickupPointsRestrictionWireMockServer.MockConfiguration();
	}

	public class MockConfiguration {

		private MockConfiguration() {
		}

		public PickupPointsRestrictionWireMockServer.MockConfiguration fetchBriefPickupPoints(String responseLocation) throws IOException {
			final var url = "/api/private/v1/knapsack/brief";
			wireMockServer.stubFor(post(url)
					.willReturn(aResponse()
							.withStatus(OK.value())
							.withHeader(CONTENT_TYPE, APPLICATION_JSON_VALUE)
							.withBody(messageLoader.loadFileContent(responseLocation))));
			return this;
		}

		public PickupPointsRestrictionWireMockServer.MockConfiguration fetchDetailPickupPoints(String responseLocation) throws IOException {
			final var url = "/api/private/v1/knapsack/detail";
			final var body = messageLoader.loadFileContent(responseLocation)
					.replace("leadTime", LocalDate.now().plusMonths(1L).format(DateTimeFormatter.ISO_DATE));
			wireMockServer.stubFor(post(urlPathEqualTo(url))
					.willReturn(aResponse()
							.withStatus(OK.value())
							.withHeader(CONTENT_TYPE, APPLICATION_JSON_VALUE)
							.withBody(body)));
			return this;
		}

		public PickupPointsRestrictionWireMockServer.MockConfiguration fetchBatchPickupPoints(String responseLocation) throws IOException {
			final var url = "/api/private/v1/knapsack/batch/brief";
			wireMockServer.stubFor(post(urlPathEqualTo(url))
					.willReturn(aResponse()
							.withStatus(OK.value())
							.withHeader(CONTENT_TYPE, APPLICATION_JSON_VALUE)
							.withBody(messageLoader.loadFileContent(responseLocation))));
			return this;
		}

		public void start() {
			if (wireMockServer.isRunning()) {
				throw new IllegalStateException("Wiremock server already started");
			}
			wireMockServer.start();
		}

	}
}
