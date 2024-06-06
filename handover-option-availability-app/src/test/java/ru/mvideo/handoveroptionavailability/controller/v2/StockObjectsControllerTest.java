package ru.mvideo.handoveroptionavailability.controller.v2;

import java.io.IOException;
import java.util.List;
import org.hamcrest.Matchers;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import ru.mvideo.handoveroptionavailability.AbstractIT;
import ru.mvideo.handoveroptionavailability.model.HandoverOption;
import ru.mvideo.handoveroptionavailability.model.Material;
import ru.mvideo.handoveroptionavailability.model.RetailBrand;
import ru.mvideo.handoveroptionavailability.model.StockObjectsRequest;
import ru.mvideo.handoveroptionavailability.security.WithMockUser;

@DisplayName("/api/v2/handover-options/stock-objects")
public class StockObjectsControllerTest extends AbstractIT {

	public StockObjectsRequest getStockObjectsRequest(HandoverOption handoverOption) {
		return StockObjectsRequest.builder()
				.regionId("S002")
				.retailBrand(RetailBrand.MVIDEO)
				.handoverOption(handoverOption)
				.handoverObject("S026")
				.materials(List.of(Material.builder()
						.material("50044074")
						.qty(1)
						.price(5630.0)
						.build()))
				.build();
	}

	@Test
	@DisplayName("Получение списка стоков для опции pickup")
	@WithMockUser(authorities = "HANDOVER_OPTIONS_STOCKS")
	void shouldReturnPickupStocks() throws IOException {
		scenarios.configureSuccessfulResponses();

		StockObjectsRequest pickupRequest = getStockObjectsRequest(HandoverOption.PICKUP);

		getPostResponse("/api/v2/handover-options/stock-objects", pickupRequest)
				.expectStatus().isOk()
				.expectBody()
				.jsonPath("$[0].material").isEqualTo("50044074")
				.jsonPath("$[0].stocks[0].availableDate").isEqualTo("2021-08-31")
				.jsonPath("$[0].stocks", Matchers.hasSize(46));
	}

	@Test
	@DisplayName("Получение списка стоков для опции pickup-partner")
	@WithMockUser(authorities = "HANDOVER_OPTIONS_STOCKS")
	void shouldReturnPickupPartnerStocks() throws IOException {
		scenarios.configureSuccessfulResponses();

		StockObjectsRequest pickupRequest = StockObjectsRequest.builder()
				.regionId("S002")
				.retailBrand(RetailBrand.MVIDEO)
				.handoverOption(HandoverOption.PICKUP_PARTNER)
				.handoverObject("S026")
				.pickupPointId("8e46af69-71ee-465a-8ef2-d19f6721bcb9")
				.materials(List.of(Material.builder()
						.material("50044074")
						.qty(2)
						.price(5630.0)
						.build()))
				.build();

		getPostResponse("/api/v2/handover-options/stock-objects", pickupRequest)
				.expectStatus().isOk()
				.expectBody()
				.jsonPath("$[0].material").isEqualTo("50044074")
				.jsonPath("$[0].stocks", Matchers.hasSize(46));
	}

	@Test
	@DisplayName("Ошибка получение списка стоков для опции pickup-partner")
	@WithMockUser(authorities = "HANDOVER_OPTIONS_STOCKS")
	void shouldFailPickupPointIdMissed() {
		StockObjectsRequest pickupRequest = getStockObjectsRequest(HandoverOption.PICKUP_PARTNER);

		getPostResponse("/api/v2/handover-options/stock-objects", pickupRequest)
				.expectStatus()
				.is4xxClientError();
	}
}
