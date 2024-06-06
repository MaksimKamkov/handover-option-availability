package ru.mvideo.handoveroptionavailability.scenarios;

import java.io.IOException;
import org.junit.jupiter.api.extension.BeforeEachCallback;
import org.junit.jupiter.api.extension.ExtensionContext;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import ru.mvideo.handoveroptionavailability.wiremock.MspWireMockServer;
import ru.mvideo.handoveroptionavailability.wiremock.ObjectsPublicWireMockServer;
import ru.mvideo.handoveroptionavailability.wiremock.PriceRulesWireMockServer;
import ru.mvideo.handoveroptionavailability.wiremock.ProductsWireMockServer;
import ru.mvideo.handoveroptionavailability.wiremock.ProvidersWireMockServer;
import ru.mvideo.handoveroptionavailability.wiremock.ZoneInfoWireMockServer;
import ru.mvideo.handoveroptionavailability.wiremock.ZonePickupItemWireMockServer;

@Component
public class DeliveryOptionsScenarios implements BeforeEachCallback {

	@Autowired
	private ZoneInfoWireMockServer zoneInfoWireMockServer;
	@Autowired
	private ProvidersWireMockServer providersWireMockServer;
	@Autowired
	private PriceRulesWireMockServer priceRulesWireMockServer;
	@Autowired
	private ZonePickupItemWireMockServer zonePickupItemWireMockServer;
	@Autowired
	private MspWireMockServer mspWireMockServer;
	@Autowired
	private ProductsWireMockServer productsWireMockServer;
	@Autowired
	private ObjectsPublicWireMockServer objectsPublicWireMockServer;

	public void configureSuccessfulResponses() throws IOException {
		zoneInfoWireMockServer.newConfiguration()
				.fetchZoneIntersection("responses/zone-info/zone-intersection.json")
				.fetchZoneDetail("responses/zone-info/zone-detail.json")
				.start();
		providersWireMockServer.newConfiguration()
				.fetchHandoverOptions("responses/providers/providers.json")
				.start();
		priceRulesWireMockServer.newConfiguration()
				.fetchMinPriceRules("responses/price-rules/min-price-rules.json")
				.start();
		zonePickupItemWireMockServer.newConfiguration()
				.fetchZoneObjects("responses/pickup-items/zone-objects.json")
				.fetchZoneObjectsByRadius("responses/pickup-items/zone-objects-radius.json")
				.start();
		mspWireMockServer.newConfiguration()
				.fetchQuotas("responses/msp/quotas.json")
				.fetchMaterialAvailability("responses/msp/delivery.json")
				.start();
		productsWireMockServer.newConfiguration()
				.fetchProducts("responses/product-service/delivery/products.json")
				.start();
		objectsPublicWireMockServer.newConfiguration()
				.fetchObjectsInfo("responses/objects-public/response.json")
				.start();
	}

	@Override
	public void beforeEach(ExtensionContext extensionContext) throws Exception {
		zoneInfoWireMockServer.beforeEach(extensionContext);
		providersWireMockServer.beforeEach(extensionContext);
		priceRulesWireMockServer.beforeEach(extensionContext);
		zonePickupItemWireMockServer.beforeEach(extensionContext);
		mspWireMockServer.beforeEach(extensionContext);
		productsWireMockServer.beforeEach(extensionContext);
		objectsPublicWireMockServer.beforeEach(extensionContext);
	}
}
