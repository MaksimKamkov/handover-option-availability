package ru.mvideo.handoveroptionavailability.scenarios;

import java.io.IOException;
import org.junit.jupiter.api.extension.BeforeEachCallback;
import org.junit.jupiter.api.extension.ExtensionContext;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import ru.mvideo.handoveroptionavailability.service.external.pickuppoints.PickupPointsService;
import ru.mvideo.handoveroptionavailability.wiremock.AvailabilityChainsWireMockServer;
import ru.mvideo.handoveroptionavailability.wiremock.HandoverOptionWireMockServer;
import ru.mvideo.handoveroptionavailability.wiremock.MspWireMockServer;
import ru.mvideo.handoveroptionavailability.wiremock.ObjectsPublicWireMockServer;
import ru.mvideo.handoveroptionavailability.wiremock.PickupPointsRestrictionWireMockServer;
import ru.mvideo.handoveroptionavailability.wiremock.PickupPointsWireMockServer;
import ru.mvideo.handoveroptionavailability.wiremock.PriceRulesWireMockServer;
import ru.mvideo.handoveroptionavailability.wiremock.ProductsWireMockServer;
import ru.mvideo.handoveroptionavailability.wiremock.ProvidersWireMockServer;
import ru.mvideo.handoveroptionavailability.wiremock.ZoneInfoWireMockServer;
import ru.mvideo.handoveroptionavailability.wiremock.ZonePickupItemWireMockServer;

@Component
public class PickupOptionsScenarios implements BeforeEachCallback {

	@Autowired
	private PickupPointsService pickupPointsService;
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
	private PickupPointsWireMockServer pickupPointsWireMockServer;
	@Autowired
	private ObjectsPublicWireMockServer objectsPublicWireMockServer;
	@Autowired
	private AvailabilityChainsWireMockServer availabilityChainsWireMockServer;
	@Autowired
	private PickupPointsRestrictionWireMockServer pickupPointsRestrictionWireMockServer;
	@Autowired
	private HandoverOptionWireMockServer handoverOptionWireMockServer;

	public void configureSeamlessResponses() throws IOException {
		zoneInfoWireMockServer.newConfiguration()
				.fetchZoneIntersection("pickup/seamless/responses/zone-intersection.json")
				.fetchZoneDetail("pickup/seamless/responses/zone-detail.json")
				.start();
		providersWireMockServer.newConfiguration()
				.fetchHandoverOptions("responses/providers/providers.json")
				.start();
		priceRulesWireMockServer.newConfiguration()
				.fetchMinPriceRules("pickup/seamless/responses/min-price-rules.json")
				.fetchPriceRules("pickup/seamless/responses/price-rules.json")
				.start();
		zonePickupItemWireMockServer.newConfiguration()
				.fetchZoneObjects("pickup/seamless/responses/zone-pickup-item.json")
				.start();
		mspWireMockServer.newConfiguration()
				.fetchMaterialAvailability("pickup/seamless/responses/availability.json")
				.start();
		productsWireMockServer.newConfiguration()
				.fetchProducts("pickup/seamless/responses/catalog-service.json")
				.start();
		objectsPublicWireMockServer.newConfiguration()
				.fetchObjectsInfo("pickup/seamless/responses/objects-public.json")
				.start();
		availabilityChainsWireMockServer.newConfiguration()
				.fetchRelatedObjects("pickup/seamless/responses/availability-chains.json")
				.start();

		pickupPointsService.warmUpCache().block();
	}

	public void configurePartnerResponses() throws IOException {
		zoneInfoWireMockServer.newConfiguration()
				.fetchZoneIntersection("pickup/partner/responses/zone-intersection.json")
				.fetchZoneDetail("pickup/partner/responses/zone-detail.json")
				.start();
		providersWireMockServer.newConfiguration()
				.fetchHandoverOptions("responses/providers/providers.json")
				.start();
		priceRulesWireMockServer.newConfiguration()
				.fetchMinPriceRules("pickup/partner/responses/min-price-rules.json")
				.fetchPriceRules("pickup/partner/responses/price-rules.json")
				.start();
		zonePickupItemWireMockServer.newConfiguration()
				.fetchZoneObjects("pickup/partner/responses/zone-pickup-item.json")
				.start();
		mspWireMockServer.newConfiguration()
				.fetchMaterialAvailability("pickup/partner/responses/availability.json")
				.start();
		productsWireMockServer.newConfiguration()
				.fetchProducts("pickup/partner/responses/catalog-service.json")
				.start();
		objectsPublicWireMockServer.newConfiguration()
				.fetchObjectsInfo("pickup/partner/responses/objects-public.json")
				.start();
		pickupPointsRestrictionWireMockServer.newConfiguration()
				.fetchDetailPickupPoints("pickup/partner/responses/pickup-point-restriction-response.json")
				.start();

		pickupPointsService.warmUpCache().block();
	}

	public void configureCreditResponses() throws IOException {
		zoneInfoWireMockServer.newConfiguration()
				.fetchZoneIntersection("pickup/credit/responses/zone-intersection.json")
				.fetchZoneDetail("pickup/credit/responses/zone-detail.json")
				.fetchZonePayment("responses/zone-info/zone-payment.json")
				.start();
		handoverOptionWireMockServer.newConfiguration()
				.fetchHandoverOptions("responses/handover-options/options.json")
				.fetchPaymentMethods("responses/handover-options/payment-methods.json")
				.fetchZoneOptionHandoverOptions("responses/providers/providers.json")
				.start();
		priceRulesWireMockServer.newConfiguration()
				.fetchMinPriceRules("pickup/credit/responses/min-price-rules.json")
				.fetchPriceRules("pickup/partner/responses/price-rules.json")
				.start();
		zonePickupItemWireMockServer.newConfiguration()
				.fetchZoneObjects("pickup/partner/responses/zone-pickup-item.json")
				.start();
		mspWireMockServer.newConfiguration()
				.fetchMaterialAvailability("pickup/credit/responses/availability.json")
				.start();
		productsWireMockServer.newConfiguration()
				.fetchProducts("pickup/credit/responses/catalog-service.json")
				.start();
		objectsPublicWireMockServer.newConfiguration()
				.fetchObjectsInfo("pickup/partner/responses/objects-public.json")
				.start();
		pickupPointsRestrictionWireMockServer.newConfiguration()
				.fetchDetailPickupPoints("pickup/partner/responses/pickup-point-restriction-response.json")
				.start();

		pickupPointsService.warmUpCache().block();
	}

	@Override
	public void beforeEach(ExtensionContext extensionContext) throws Exception {
		zoneInfoWireMockServer.beforeEach(extensionContext);
		providersWireMockServer.beforeEach(extensionContext);
		priceRulesWireMockServer.beforeEach(extensionContext);
		zonePickupItemWireMockServer.beforeEach(extensionContext);
		mspWireMockServer.beforeEach(extensionContext);
		productsWireMockServer.beforeEach(extensionContext);
		pickupPointsWireMockServer.beforeEach(extensionContext);
		objectsPublicWireMockServer.beforeEach(extensionContext);
		availabilityChainsWireMockServer.beforeEach(extensionContext);
		pickupPointsRestrictionWireMockServer.beforeEach(extensionContext);
		handoverOptionWireMockServer.beforeEach(extensionContext);
	}
}
