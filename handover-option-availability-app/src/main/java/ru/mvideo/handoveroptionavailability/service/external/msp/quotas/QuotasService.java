package ru.mvideo.handoveroptionavailability.service.external.msp.quotas;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.jetbrains.annotations.Nullable;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;
import ru.mvideo.handoveroptionavailability.client.MspClient;
import ru.mvideo.handoveroptionavailability.config.QuotaConfig;
import ru.mvideo.handoveroptionavailability.model.Material;
import ru.mvideo.handoveroptionavailability.model.RetailBrand;
import ru.mvideo.msp.quota.model.AvailableQuota;
import ru.mvideo.msp.quota.model.OrderPosition;
import ru.mvideo.msp.quota.model.QuotaAvailabilityRequest;
import ru.mvideo.msp.quota.model.QuotaAvailabilityRequestBody;
import ru.mvideo.msp.quota.model.QuotaAvailabilityResponse;
import ru.mvideo.msp.quota.model.QuotaAvailabilityResponseBody;
import ru.mvideo.msp.quota.model.QuotaParam;

@Slf4j
@Service
@RequiredArgsConstructor
public class QuotasService {

	private final MspClient client;
	private final QuotaConfig config;

	private static final DateTimeFormatter QUOTA_DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss");

	public Mono<QuotaAvailabilityResponse> getQuotas(List<Material> orderPositions, String rimCode, RetailBrand retailBrand,
	                                                 @Nullable LocalDate dateFrom) {
		if (retailBrand.equals(RetailBrand.ELDORADO)) {
			return Mono.just(getEldoradoResponse());
		}

		final var dateTime = dateFrom == null ? LocalDateTime.now() : dateFrom.atStartOfDay();
		final var request = createQuotaRequest(orderPositions, rimCode, dateTime);

		return client.getQuota(request)
				.onErrorResume(
						Exception.class,
						fallback -> {
							log.error("Quota service error: {}", fallback.getMessage());
							return Mono.just(new QuotaAvailabilityResponse());
						});
	}

	private QuotaAvailabilityRequest createQuotaRequest(List<Material> products, String rimCode, LocalDateTime dateFrom) {
		var request = new QuotaAvailabilityRequest();
		var quotaRequestBody = new QuotaAvailabilityRequestBody();
		request.setRequestBody(quotaRequestBody);

		quotaRequestBody.setQuotaService("1");
		quotaRequestBody.setRegion(rimCode);
		quotaRequestBody.setDateFrom(dateFrom.format(QUOTA_DATE_FORMATTER));
		quotaRequestBody.setDateTo(dateFrom.plusDays(config.getMaxDays()).format(QUOTA_DATE_FORMATTER));
		quotaRequestBody.setGetUnavailable(false);
		quotaRequestBody.setOrderPositions(products.stream()
				.map(orderPosition -> {
					var mspQuotaPosition = new OrderPosition();
					mspQuotaPosition.setMaterial(orderPosition.getMaterial());
					mspQuotaPosition.setQty(orderPosition.getQty());
					mspQuotaPosition.setPrice(orderPosition.getPrice());
					mspQuotaPosition.setHandoverObject(rimCode);
					return mspQuotaPosition;
				})
				.collect(Collectors.toList())
		);

		return request;
	}

	private QuotaAvailabilityResponse getEldoradoResponse() {
		var quotaParam = new QuotaParam();
		quotaParam.setKey("duration");
		quotaParam.setValue("1");

		var availableQuota = new AvailableQuota();
		availableQuota.setQuotaParams(List.of(quotaParam));
		availableQuota.setDate(LocalDate.now().plusDays(1));
		availableQuota.setPrice(0.0);

		var responseBody = new QuotaAvailabilityResponseBody();
		responseBody.setAvailableQuotes(List.of(availableQuota));

		var quotaAvailabilityResponse = new QuotaAvailabilityResponse();
		quotaAvailabilityResponse.setResponseBody(responseBody);

		return quotaAvailabilityResponse;
	}
}
