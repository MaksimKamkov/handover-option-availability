package ru.mvideo.handoveroptionavailability.processor.response.option.brief;

import static org.mockito.ArgumentMatchers.anyList;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import ru.mvideo.handoveroptionavailability.mapper.BriefResponseMapper;
import ru.mvideo.handoveroptionavailability.model.BriefApplicableTo;
import ru.mvideo.handoveroptionavailability.model.HandoverOption;
import ru.mvideo.handoveroptionavailability.processor.model.BriefAndPickupContext;
import ru.mvideo.handoveroptionavailability.processor.model.OptionContext;
import ru.mvideo.msp.quota.model.QuotaAvailabilityResponse;
import ru.mvideo.msp.quota.model.QuotaAvailabilityResponseBody;

@ExtendWith(MockitoExtension.class)
public class IntervalBriefServiceTest {

	@InjectMocks
	private IntervalBriefService service;
	@Mock
	private BriefResponseMapper responseMapper;

	@DisplayName("Проверка того что сервис поддерживает опцию INTERVAL_DELIVERY")
	@Test
	public void shouldSupport() {
		final BriefAndPickupContext context = BriefAndPickupContext.builder()
				.requestHandoverOptions(Set.of(HandoverOption.INTERVAL_DELIVERY.getValue()))
				.build();

		Assertions.assertTrue(service.support(context));
	}

	@DisplayName("Проверка того что сервис не поддерживает опцию EXACTLY_TIME_DELIVERY")
	@Test
	public void shouldNonSupport() {
		final BriefAndPickupContext context = BriefAndPickupContext.builder()
				.requestHandoverOptions(Set.of(HandoverOption.EXACTLY_TIME_DELIVERY.getValue()))
				.build();

		Assertions.assertFalse(service.support(context));
	}

	@DisplayName("В случае отсутствия у товара, опции участвующие в задаче о рюкзаке должны быть исключены")
	@Test
	public void shouldReturnCommonAvailabilityDate() {
		Mockito.when(responseMapper.toApplicableTo(anyList())).thenReturn(List.of(
				BriefApplicableTo.builder().availabilityDate(LocalDate.now()).build(),
				BriefApplicableTo.builder().availabilityDate(LocalDate.now().plus(1, ChronoUnit.DAYS)).build()
		));

		final var optionContext = new OptionContext();
		optionContext.setHandoverOption(HandoverOption.INTERVAL_DELIVERY.getValue());
		optionContext.setAvailabilityOptions(List.of());
		final BriefAndPickupContext context = BriefAndPickupContext.builder()
				.requestHandoverOptions(Set.of(HandoverOption.INTERVAL_DELIVERY.getValue()))
				.handoverOptionContext(Map.of(HandoverOption.INTERVAL_DELIVERY.getValue(), optionContext))
				.build();
		final var quotas = new QuotaAvailabilityResponse();
		final var responseBody = new QuotaAvailabilityResponseBody();
		responseBody.setAvailableQuotes(List.of());
		quotas.setResponseBody(responseBody);
		context.quotas(quotas);

		final var option = service.prepareResponse(context);
		Assertions.assertEquals(0, option.getAvailabilityDate().compareTo(LocalDate.now().plus(1, ChronoUnit.DAYS)));
	}
}
