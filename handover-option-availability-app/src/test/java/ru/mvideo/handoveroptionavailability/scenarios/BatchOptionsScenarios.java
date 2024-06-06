package ru.mvideo.handoveroptionavailability.scenarios;

import java.io.IOException;

import org.junit.jupiter.api.extension.BeforeEachCallback;
import org.junit.jupiter.api.extension.ExtensionContext;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import ru.mvideo.handoveroptionavailability.wiremock.MspWireMockServer;
import ru.mvideo.handoveroptionavailability.wiremock.ObjectsPublicWireMockServer;
import ru.mvideo.handoveroptionavailability.wiremock.PickupPointsRestrictionWireMockServer;
import ru.mvideo.handoveroptionavailability.wiremock.PriceRulesWireMockServer;
import ru.mvideo.handoveroptionavailability.wiremock.ProductsWireMockServer;
import ru.mvideo.handoveroptionavailability.wiremock.ProvidersWireMockServer;
import ru.mvideo.handoveroptionavailability.wiremock.ZoneInfoWireMockServer;
import ru.mvideo.handoveroptionavailability.wiremock.ZonePickupItemWireMockServer;

@Component
public class BatchOptionsScenarios implements BeforeEachCallback {

    @Autowired
    private ZoneInfoWireMockServer zoneInfoWireMockServer;
    @Autowired
    private ProvidersWireMockServer providersWireMockServer;
    @Autowired
    private PriceRulesWireMockServer priceRulesWireMockServer;
    @Autowired
    private ZonePickupItemWireMockServer zonePickupItemWireMockServer;
    @Autowired
    private ObjectsPublicWireMockServer objectsPublicWireMockServer;
    @Autowired
    private PickupPointsRestrictionWireMockServer pickupPointsRestrictionWireMockServer;
    @Autowired
    private MspWireMockServer mspWireMockServer;
    @Autowired
    private ProductsWireMockServer productsWireMockServer;

    public void configureSuccessfulResponses() throws IOException {
        zoneInfoWireMockServer.newConfiguration()
                .fetchZoneIntersection("responses/zone-info/zone-intersection.json")
                .start();
        providersWireMockServer.newConfiguration()
                .fetchHandoverOptions("responses/providers/providers.json")
                .start();
        priceRulesWireMockServer.newConfiguration()
                .fetchBatchMinPriceRules("responses/price-rules/batch-min-price-rules.json")
                .start();
        zonePickupItemWireMockServer.newConfiguration()
                .fetchZoneObjects("responses/pickup-items/zone-objects.json")
                .start();
        objectsPublicWireMockServer.newConfiguration()
                .fetchObjectsInfo("responses/objects-public/response.json")
                .start();
        pickupPointsRestrictionWireMockServer.newConfiguration()
                .fetchBatchPickupPoints("responses/pickup-point-restriction/batch.json")
                .start();
        mspWireMockServer.newConfiguration()
                .fetchMaterialAvailability("responses/msp/batch.json")
                .start();
        productsWireMockServer.newConfiguration()
                .fetchProducts("responses/product-service/delivery/products.json")
                .start();
    }

    public void configureDoubleMaterialBatchResponses() throws IOException {
        productsWireMockServer.newConfiguration()
                .fetchProducts("batch/responses/catalog-service.json")
                .start();
        zoneInfoWireMockServer.newConfiguration()
                .fetchZoneIntersection("responses/zone-info/zone-intersection.json")
                .start();
        zonePickupItemWireMockServer.newConfiguration()
                .fetchZoneObjects("responses/pickup-items/zone-objects.json")
                .start();
        objectsPublicWireMockServer.newConfiguration()
                .fetchObjectsInfo("responses/objects-public/response.json")
                .start();
        providersWireMockServer.newConfiguration()
                .fetchHandoverOptions("responses/providers/providers.json")
                .start();
        priceRulesWireMockServer.newConfiguration()
                .fetchBatchMinPriceRules("batch/responses/min-price-rules.json")
                .start();
        mspWireMockServer.newConfiguration()
                .fetchMaterialAvailability("batch/responses/availability.json", "batch/responses/availability2.json")
                .start();
        pickupPointsRestrictionWireMockServer.newConfiguration()
                .fetchBatchPickupPoints("batch/responses/pickup-point-restriction.json")
                .start();
    }

    public void configureSuccessfulResponses(String zoneIntersection, String handoverOptions, String batchMinPriceRules, String zoneObjects, String objectsInfo, String batchPickupPoints, String materialAvailability, String products) throws IOException {
        zoneInfoWireMockServer.newConfiguration().fetchZoneIntersection(zoneIntersection).start();
        providersWireMockServer.newConfiguration().fetchHandoverOptions(handoverOptions).start();
        priceRulesWireMockServer.newConfiguration().fetchBatchMinPriceRules(batchMinPriceRules).start();
        zonePickupItemWireMockServer.newConfiguration().fetchZoneObjects(zoneObjects).start();
        objectsPublicWireMockServer.newConfiguration().fetchObjectsInfo(objectsInfo).start();
        pickupPointsRestrictionWireMockServer.newConfiguration().fetchBatchPickupPoints(batchPickupPoints).start();
        mspWireMockServer.newConfiguration().fetchMaterialAvailability(materialAvailability).start();
        productsWireMockServer.newConfiguration().fetchProducts(products).start();
    }

    public void configureDoubleMaterialBatchResponses(String catalog_service, String zone_intersection_msdelivery, String zone_objects, String objects_public_response, String providers_universal_electronic_delivery, String min_price, String availability_electronic, String pickup_point_restriction_empty) throws IOException {
        productsWireMockServer.newConfiguration().fetchProducts(catalog_service).start();
        zoneInfoWireMockServer.newConfiguration().fetchZoneIntersection(zone_intersection_msdelivery).start();
        zonePickupItemWireMockServer.newConfiguration().fetchZoneObjects(zone_objects).start();
        objectsPublicWireMockServer.newConfiguration().fetchObjectsInfo(objects_public_response).start();
        providersWireMockServer.newConfiguration().fetchHandoverOptions(providers_universal_electronic_delivery).start();
        priceRulesWireMockServer.newConfiguration().fetchBatchMinPriceRules(min_price).start();
        mspWireMockServer.newConfiguration().fetchMaterialAvailability(availability_electronic).start();
        pickupPointsRestrictionWireMockServer.newConfiguration().fetchBatchPickupPoints(pickup_point_restriction_empty).start();
    }

    @Override
    public void beforeEach(ExtensionContext extensionContext) throws Exception {
        zoneInfoWireMockServer.beforeEach(extensionContext);
        providersWireMockServer.beforeEach(extensionContext);
        priceRulesWireMockServer.beforeEach(extensionContext);
        zonePickupItemWireMockServer.beforeEach(extensionContext);
        objectsPublicWireMockServer.beforeEach(extensionContext);
        pickupPointsRestrictionWireMockServer.beforeEach(extensionContext);
        mspWireMockServer.beforeEach(extensionContext);
        productsWireMockServer.beforeEach(extensionContext);
    }
}
