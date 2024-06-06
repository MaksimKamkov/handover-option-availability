package ru.mvideo.handoveroptionavailability.wiremock;

import static com.github.tomakehurst.wiremock.client.WireMock.aResponse;
import static com.github.tomakehurst.wiremock.client.WireMock.get;
import static com.github.tomakehurst.wiremock.client.WireMock.post;
import static com.github.tomakehurst.wiremock.client.WireMock.urlPathEqualTo;
import static com.github.tomakehurst.wiremock.core.WireMockConfiguration.options;
import static org.springframework.http.HttpHeaders.CONTENT_TYPE;
import static org.springframework.http.HttpStatus.OK;
import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;

import com.github.tomakehurst.wiremock.WireMockServer;
import java.io.IOException;
import org.apache.commons.lang3.StringUtils;
import org.junit.jupiter.api.extension.BeforeEachCallback;
import org.junit.jupiter.api.extension.ExtensionContext;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import ru.mvideo.handoveroptionavailability.MessageLoader;

@Component
public class PickupPointsWireMockServer implements BeforeEachCallback, InitializingBean {

	@Value("${mvideo.pickup-points.client.ms-pickup-points.base-url}")
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

	public MockConfiguration newConfiguration() {
		return new MockConfiguration();
	}

	public class MockConfiguration {

		private MockConfiguration() {
		}

		public MockConfiguration fetchCellLimits(String responseLocation) throws IOException {
			final var url = "/v2/cell-limits";
			wireMockServer.stubFor(get(url)
					.willReturn(aResponse()
							.withStatus(OK.value())
							.withHeader(CONTENT_TYPE, APPLICATION_JSON_VALUE)
							.withBody(messageLoader.loadFileContent(responseLocation))));
			return this;
		}

		public MockConfiguration fetchCompressedPickupPoints(String responseLocation) throws IOException {
			final var url = "/v2/pickup-points/compressed/cellLimit";
			wireMockServer.stubFor(post(urlPathEqualTo(url))
					.willReturn(aResponse()
							.withStatus(OK.value())
							.withHeader(CONTENT_TYPE, APPLICATION_JSON_VALUE)
							.withBody(messageLoader.loadFileContent(responseLocation))));
			return this;
		}

		public MockConfiguration fetchPickupPoints(String responseLocation) throws IOException {
			final var url = "/v2/pickup-points/id";
			wireMockServer.stubFor(get(urlPathEqualTo(url))
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
