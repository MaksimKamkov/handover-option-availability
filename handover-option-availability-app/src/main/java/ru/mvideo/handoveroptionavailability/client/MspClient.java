package ru.mvideo.handoveroptionavailability.client;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpHeaders;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;
import ru.mvideo.handoveroptionavailability.config.externalclient.AuthorizedClientProperties;
import ru.mvideo.handoveroptionavailability.model.external.msp.logistic.request.MspRequest;
import ru.mvideo.handoveroptionavailability.model.external.msp.logistic.responsenextcalendar.MspResponseNextCalendar;
import ru.mvideo.lib.client.BaseClient;
import ru.mvideo.lib.client.LogHelper;
import ru.mvideo.msp.quota.model.QuotaAvailabilityRequest;
import ru.mvideo.msp.quota.model.QuotaAvailabilityResponse;

@Slf4j
@Component
public class MspClient extends BaseClient {

	private static final String AVAILABILITY_DATES_PATH = "/logistic/rest/availability/dates";
	private static final String QUOTA_GET_PATH = "/quota/rest/get";

	private static final ParameterizedTypeReference<MspResponseNextCalendar> MSP_LOGISTIC_TYPE_NEXT_CALENDAR =
			new ParameterizedTypeReference<>() {
			};
	private static final ParameterizedTypeReference<QuotaAvailabilityResponse> QUOTA_RESPONSE_TYPE =
			new ParameterizedTypeReference<>() {
			};

	private final AuthorizedClientProperties clientProperties;

	public MspClient(@Qualifier("mspLogisticWebClient") WebClient webClient,
	                 @Qualifier("mspLogisticClientProperties") AuthorizedClientProperties clientProperties,
	                 @Qualifier("clientLogHelper") LogHelper logHelper) {
		super(webClient, logHelper, log);
		this.clientProperties = clientProperties;
	}

	public Mono<MspResponseNextCalendar> fetchMaterialAvailabilityCalendar(MspRequest request) {
		var headers = new HttpHeaders();
		headers.setBasicAuth(clientProperties.getUsername(), clientProperties.getPassword());

		return postMono(uriBuilder ->
						uriBuilder.path(AVAILABILITY_DATES_PATH)
								.build(),
				request,
				headers,
				MSP_LOGISTIC_TYPE_NEXT_CALENDAR
		);
	}

	public Mono<QuotaAvailabilityResponse> getQuota(QuotaAvailabilityRequest request) {
		var headers = new HttpHeaders();
		headers.setBasicAuth(clientProperties.getUsername(), clientProperties.getPassword());

		return postMono(uriBuilder ->
						uriBuilder.path(QUOTA_GET_PATH)
								.build(),
				request,
				headers,
				QUOTA_RESPONSE_TYPE
		);
	}
}
