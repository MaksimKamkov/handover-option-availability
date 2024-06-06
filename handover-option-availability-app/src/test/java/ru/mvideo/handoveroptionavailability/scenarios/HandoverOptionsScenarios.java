package ru.mvideo.handoveroptionavailability.scenarios;

import java.io.IOException;
import org.junit.jupiter.api.extension.BeforeEachCallback;
import org.junit.jupiter.api.extension.ExtensionContext;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import ru.mvideo.handoveroptionavailability.service.external.pickuppoints.PickupPointsService;
import ru.mvideo.handoveroptionavailability.wiremock.HandoverOptionWireMockServer;
import ru.mvideo.handoveroptionavailability.wiremock.MspWireMockServer;
import ru.mvideo.handoveroptionavailability.wiremock.ObjectsPublicWireMockServer;
import ru.mvideo.handoveroptionavailability.wiremock.PickupPointsWireMockServer;
import ru.mvideo.handoveroptionavailability.wiremock.PriceRulesWireMockServer;
import ru.mvideo.handoveroptionavailability.wiremock.ProductsWireMockServer;
import ru.mvideo.handoveroptionavailability.wiremock.ZoneHandoverOptionWireMockServer;
import ru.mvideo.handoveroptionavailability.wiremock.ZoneInfoWireMockServer;
import ru.mvideo.handoveroptionavailability.wiremock.ZonePickupItemWireMockServer;

@Component
public class HandoverOptionsScenarios implements BeforeEachCallback {
	@Autowired
	private PickupPointsService pickupPointsService;
	@Autowired
	private ZoneInfoWireMockServer zoneInfoWireMockServer;
	@Autowired
	private ZoneHandoverOptionWireMockServer zoneHandoverOptionWireMockServer;
	@Autowired
	private HandoverOptionWireMockServer handoverOptionWireMockServer;
	@Autowired
	private PriceRulesWireMockServer priceRulesWireMockServer;
	@Autowired
	private ZonePickupItemWireMockServer zonePickupItemWireMockServer;
	@Autowired
	private MspWireMockServer mspWireMockServer;
	@Autowired
	private ProductsWireMockServer productsWireMockServer;
	@Autowired
	private PickupPointsWireMockServer pickupPointsWireMockServer;
	@Autowired
	private ObjectsPublicWireMockServer objectsPublicWireMockServer;

	public void configureSuccessfulResponses() throws IOException {
		pickupPointsWireMockServer.newConfiguration()
				.fetchCellLimits("responses/pickup-points/cell-limits.json")
				.fetchCompressedPickupPoints("responses/pickup-points/compressed-pickup-points.json")
				.start();
		zoneInfoWireMockServer.newConfiguration()
				.fetchZoneIntersection("responses/zone-info/zone-intersection.json")
				.fetchZoneDetail("responses/zone-info/zone-detail.json")
				.fetchZonePayment("responses/zone-info/zone-payment.json")
				.start();
		zoneHandoverOptionWireMockServer.newConfiguration()
				.fetchZoneOption("responses/zone-handover-option/zone-option.json")
				.start();
		handoverOptionWireMockServer.newConfiguration()
				.fetchHandoverOptions("responses/handover-options/options.json")
				.fetchPaymentMethods("responses/handover-options/payment-methods.json")
				.start();
		priceRulesWireMockServer.newConfiguration()
				.fetchPriceRules("responses/price-rules/price-rules.json")
				.start();
		zonePickupItemWireMockServer.newConfiguration()
				.fetchZoneObjects("responses/pickup-items/zone-objects.json")
				.start();
		mspWireMockServer.newConfiguration()
				.fetchQuotas("responses/msp/quotas.json")
				.fetchMaterialAvailability("responses/msp/material-availability.json")
				.start();
		productsWireMockServer.newConfiguration()
				.fetchProducts("responses/product-service/handover-options/products.json")
				.start();
		objectsPublicWireMockServer.newConfiguration()
				.fetchObjectsInfo("responses/objects-public/response.json")
				.start();

		pickupPointsService.warmUpCache().block();
	}

	public void configureSuccessfulResponses(String quotas, String materialAvailability) throws IOException {
		pickupPointsWireMockServer.newConfiguration()
				.fetchCellLimits("responses/pickup-points/cell-limits.json")
				.fetchCompressedPickupPoints("responses/pickup-points/compressed-pickup-points.json")
				.start();
		zoneInfoWireMockServer.newConfiguration()
				.fetchZoneIntersection("responses/zone-info/zone-intersection.json")
				.fetchZoneDetail("responses/zone-info/zone-detail.json")
				.fetchZonePayment("responses/zone-info/zone-payment.json")
				.start();
		zoneHandoverOptionWireMockServer.newConfiguration()
				.fetchZoneOption("responses/zone-handover-option/zone-option.json")
				.start();
		handoverOptionWireMockServer.newConfiguration()
				.fetchHandoverOptions("responses/handover-options/options.json")
				.fetchPaymentMethods("responses/handover-options/payment-methods.json")
				.fetchZoneOptionHandoverOptions("responses/providers/providers.json")
				.start();
		priceRulesWireMockServer.newConfiguration()
				.fetchPriceRules("responses/price-rules/price-rules.json")
				.fetchMinPriceRules("responses/price-rules/min-price-rules.json")
				.start();
		zonePickupItemWireMockServer.newConfiguration()
				.fetchZoneObjects("responses/pickup-items/zone-objects.json")
				.start();
		mspWireMockServer.newConfiguration()
				.fetchQuotas(quotas)
				.fetchMaterialAvailability(materialAvailability)
				.start();
		productsWireMockServer.newConfiguration()
				.fetchProducts("responses/product-service/handover-options/products.json")
				.start();
		objectsPublicWireMockServer.newConfiguration()
				.fetchObjectsInfo("responses/objects-public/response.json")
				.start();

		pickupPointsService.warmUpCache().block();
	}

	@Override
	public void beforeEach(ExtensionContext extensionContext) throws Exception {
		zoneInfoWireMockServer.beforeEach(extensionContext);
		zoneHandoverOptionWireMockServer.beforeEach(extensionContext);
		handoverOptionWireMockServer.beforeEach(extensionContext);
		priceRulesWireMockServer.beforeEach(extensionContext);
		zonePickupItemWireMockServer.beforeEach(extensionContext);
		mspWireMockServer.beforeEach(extensionContext);
		productsWireMockServer.beforeEach(extensionContext);
		pickupPointsWireMockServer.beforeEach(extensionContext);
		objectsPublicWireMockServer.beforeEach(extensionContext);
	}
}
