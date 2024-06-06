package ru.mvideo.handoveroptionavailability.scenarios;

import java.io.IOException;
import org.junit.jupiter.api.extension.BeforeEachCallback;
import org.junit.jupiter.api.extension.ExtensionContext;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import ru.mvideo.handoveroptionavailability.service.external.pickuppoints.PickupPointsServiceImpl;
import ru.mvideo.handoveroptionavailability.wiremock.MspWireMockServer;
import ru.mvideo.handoveroptionavailability.wiremock.PickupPointsWireMockServer;

@Component
public class StockObjectsScenarios implements BeforeEachCallback {

	@Autowired
	private MspWireMockServer mspWireMockServer;
	@Autowired
	private PickupPointsWireMockServer pickupPointsWireMockServer;
	@Autowired
	private PickupPointsServiceImpl pickupPointsService;

	public void configureSuccessfulResponses() throws IOException {
		mspWireMockServer.newConfiguration()
				.fetchMaterialAvailability("responses/msp/material-availability.json")
				.start();

		pickupPointsWireMockServer.newConfiguration()
				.fetchCellLimits("responses/pickup-points/cell-limits.json")
				.fetchPickupPoints("responses/pickup-points/pickup-points.json")
				.start();

		pickupPointsService.warmUpCache().block();
	}

	@Override
	public void beforeEach(ExtensionContext extensionContext) throws Exception {
		mspWireMockServer.beforeEach(extensionContext);
		pickupPointsWireMockServer.beforeEach(extensionContext);
	}
}
