package ru.mvideo.handoveroptionavailability.wiremock;

import static com.github.tomakehurst.wiremock.client.WireMock.aResponse;
import static com.github.tomakehurst.wiremock.client.WireMock.post;
import static com.github.tomakehurst.wiremock.core.WireMockConfiguration.options;
import static org.springframework.http.HttpHeaders.CONTENT_TYPE;
import static org.springframework.http.HttpStatus.OK;
import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;

import com.github.tomakehurst.wiremock.stubbing.Scenario;
import com.github.tomakehurst.wiremock.WireMockServer;
import java.io.IOException;
import java.time.LocalDateTime;
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
public class MspWireMockServer implements BeforeEachCallback, InitializingBean {

	private static final DateTimeFormatter AVAILABLE_DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd");
	private static final DateTimeFormatter VALID_TO_FORMATTER = DateTimeFormatter.ofPattern("dd.MM.yyyy");

	@Value("${ru.mvideo.msp.base-url}")
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

		public MockConfiguration fetchQuotas(String responseLocation) throws IOException {
			final var url = "/quota/rest/get";
			wireMockServer.stubFor(post(url)
					.willReturn(aResponse()
							.withStatus(OK.value())
							.withHeader(CONTENT_TYPE, APPLICATION_JSON_VALUE)
							.withBody(messageLoader.loadFileContent(responseLocation))));
			return this;
		}

        public MockConfiguration fetchMaterialAvailability(String... responseLocations) throws IOException {
            final var url = "/logistic/rest/availability/dates";
            for (int i = 0; i < responseLocations.length; i++) {
                final var content = messageLoader.loadFileContent(responseLocations[i])
                        .replace("availableDateToday", AVAILABLE_DATE_FORMATTER.format(LocalDateTime.now()))
                        .replace("availableDateTomorrow", AVAILABLE_DATE_FORMATTER.format(LocalDateTime.now().plusDays(1)))
                        .replace("availableDatePlusTwoDays", AVAILABLE_DATE_FORMATTER.format(LocalDateTime.now().plusDays(2)))
                        .replace("availableDatePlusThreeDays", AVAILABLE_DATE_FORMATTER.format(LocalDateTime.now().plusDays(3)))
                        .replace("validToToday", VALID_TO_FORMATTER.format(LocalDateTime.now()))
                        .replace("validToTomorrow", VALID_TO_FORMATTER.format(LocalDateTime.now().plusDays(1)))
                        .replace("validToPlusTwoDays", VALID_TO_FORMATTER.format(LocalDateTime.now().plusDays(2)))
                        .replace("validToPlusThreeDays", VALID_TO_FORMATTER.format(LocalDateTime.now().plusDays(3)));
                var scenario = post(url).inScenario("fetchMaterialAvailability");
                if (i == 0) {
                    scenario.whenScenarioStateIs(Scenario.STARTED);
                } else {
                    scenario.whenScenarioStateIs(String.valueOf(i - 1));
                }

                wireMockServer.stubFor(
                        scenario.willReturn(aResponse()
                                        .withStatus(OK.value())
                                        .withHeader(CONTENT_TYPE, APPLICATION_JSON_VALUE)
                                        .withBody(content))
                                .willSetStateTo(String.valueOf(i)));
            }

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
