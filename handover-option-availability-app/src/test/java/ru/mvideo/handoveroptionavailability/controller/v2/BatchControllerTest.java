package ru.mvideo.handoveroptionavailability.controller.v2;

import java.io.IOException;
import java.util.List;
import java.util.stream.Stream;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import ru.mvideo.handoveroptionavailability.AbstractIT;
import ru.mvideo.handoveroptionavailability.model.BatchRequest;
import ru.mvideo.handoveroptionavailability.model.HandoverOption;
import ru.mvideo.handoveroptionavailability.model.MaterialWithoutQty;
import ru.mvideo.handoveroptionavailability.model.RetailBrand;

import static org.junit.jupiter.params.provider.Arguments.arguments;

@DisplayName("/api/v2/handover-options/batch")
public class BatchControllerTest extends AbstractIT {

    public BatchRequest getBatchBody(String regionId, RetailBrand retailBrand, List<HandoverOption> handoverOption, String material, Double price) {
        return BatchRequest.builder()
                .regionId(regionId)
                .retailBrand(retailBrand)
                .handoverOption(handoverOption)
                .materials(List.of(
                        MaterialWithoutQty.builder().material(material).price(price).build()))
                .build();
    }

    public BatchRequest getBatchBodyDoubleMaterial(String regionId, RetailBrand retailBrand, List<HandoverOption> handoverOption, String material1, Double price1, String material2, Double price2) {
        return BatchRequest.builder()
                .regionId(regionId)
                .retailBrand(retailBrand)
                .handoverOption(handoverOption)
                .materials(List.of(
                        MaterialWithoutQty.builder().material(material1).price(price1).build(),
                        MaterialWithoutQty.builder().material(material2).price(price2).build()))
                .build();
    }

    public BatchRequest getBatchBodyEmptyMaterial(String regionId, RetailBrand retailBrand, List<HandoverOption> handoverOption) {
        return BatchRequest.builder()
                .regionId(regionId)
                .retailBrand(retailBrand)
                .handoverOption(handoverOption)
                .materials(List.of())
                .build();
    }

    @Test
    @DisplayName("Вывод доступных способов получения товара. Опции pickup-partner, eta-delivery, pickup, interval-delivery")
    public void testShouldReturnBatchInfo() throws IOException {
        List<HandoverOption> handoverOption = List.of(HandoverOption.ETA_DELIVERY, HandoverOption.PICKUP, HandoverOption.INTERVAL_DELIVERY, HandoverOption.PICKUP_PARTNER);

        batchOptionsScenarios.configureSuccessfulResponses(
                "responses/zone-info/zone-intersection-msdelivery.json",
                "responses/providers/providers-calltovisit-s002.json",
                "responses/price-rules/batch-min-price-rules.json",
                "responses/pickup-items/zone-objects.json",
                "responses/objects-public/response.json",
                "responses/pickup-point-restriction/batch.json",
                "responses/msp/batch.json",
                "responses/product-service/delivery/products.json");

        BatchRequest batch = getBatchBody("S002", RetailBrand.MVIDEO, handoverOption, "50044074", 15909.0);

        getPostResponse("/api/v2/handover-options/batch", batch)
                .expectStatus()
                .isEqualTo(200)
                .expectBody()
                .jsonPath("$[0].material").isEqualTo("50044074")
                .jsonPath("$[0].handoverOption[0].handoverOption").isEqualTo("pickup-partner")
                .jsonPath("$[0].handoverOption[0].minPrice").isEqualTo(1.0)
                .jsonPath("$[0].handoverOption[1].handoverOption").isEqualTo("eta-delivery")
                .jsonPath("$[0].handoverOption[1].minPrice").isEqualTo(100)
                .jsonPath("$[0].handoverOption[1].eta").isEqualTo(120)
                .jsonPath("$[0].handoverOption[2].handoverOption").isEqualTo("pickup")
                .jsonPath("$[0].handoverOption[2].minPrice").isEqualTo(10)
                .jsonPath("$[0].handoverOption[3].handoverOption").isEqualTo("interval-delivery")
                .jsonPath("$[0].handoverOption[3].minPrice").isEqualTo(10);
    }

    @Test
    @DisplayName("\"Вывод доступных способов получения товара. Опции electronic-delivery")
    public void testShouldReturnBatchInfoIncorrectHandoverOption() throws IOException {
        List<HandoverOption> handoverOption = List.of(HandoverOption.ELECTRONIC_DELIVERY);

        batchOptionsScenarios.configureDoubleMaterialBatchResponses(
                "batch/responses/catalog-service-6004613.json",
                "responses/zone-info/zone-intersection-msdelivery.json",
                "responses/pickup-items/zone-objects.json",
                "responses/objects-public/response.json",
                "responses/providers/providers-universal-electronic-delivery.json",
                "batch/responses/min-price-rules-electronic-delivery.json",
                "batch/responses/availability-electronic.json",
                "batch/responses/pickup-point-restriction-empty.json");

        BatchRequest batch = getBatchBody("S002", RetailBrand.MVIDEO, handoverOption, "6004613", 3999.5);

        getPostResponse("/api/v2/handover-options/batch", batch)
                .expectStatus()
                .isEqualTo(200)
                .expectBody()
                .jsonPath("$[0].material").isEqualTo("6004613")
                .jsonPath("$[0].handoverOption[0].handoverOption").isEqualTo("electronic-delivery")
                .jsonPath("$[0].handoverOption[0].minPrice").isEqualTo(10.0);
    }

    @Test
    @DisplayName("Вывод доступных способов получения товара. Опции pickup-partner, eta-delivery, pickup, interval-delivery")
    public void testShouldReturnBatchInfoDoubleMaterial() throws IOException {
        List<HandoverOption> handoverOption = List.of(HandoverOption.ETA_DELIVERY, HandoverOption.PICKUP, HandoverOption.INTERVAL_DELIVERY, HandoverOption.PICKUP_PARTNER);

        batchOptionsScenarios.configureSuccessfulResponses(
                "responses/zone-info/zone-intersection-msdelivery.json",
                "responses/providers/providers-calltovisit-s002.json",
                "responses/price-rules/batch-min-price-rules.json",
                "responses/pickup-items/zone-objects.json",
                "responses/objects-public/response.json",
                "responses/pickup-point-restriction/batch.json",
                "responses/msp/batch.json",
                "responses/product-service/delivery/products.json");

        BatchRequest batch = getBatchBodyDoubleMaterial("S002", RetailBrand.MVIDEO, handoverOption, "50044074", 15909.0, "50044075", 55909.0);

        getPostResponse("/api/v2/handover-options/batch", batch)
                .expectStatus()
                .isEqualTo(200)
                .expectBody()
                .jsonPath("[?(@.material==\"50044075\")].material").exists()
                .jsonPath("[?(@.material==\"50044075\")].handoverOption[0].handoverOption").isEqualTo("pickup-partner")
                .jsonPath("[?(@.material==\"50044075\")].handoverOption[0].minPrice").isEqualTo(2.0)
                .jsonPath("[?(@.material==\"50044075\")].handoverOption[1].handoverOption").isEqualTo("eta-delivery")
                .jsonPath("[?(@.material==\"50044075\")].handoverOption[1].minPrice").isEqualTo(200.0)
                .jsonPath("[?(@.material==\"50044075\")].handoverOption[1].eta").isEqualTo(120)
                .jsonPath("[?(@.material==\"50044075\")].handoverOption[2].handoverOption").isEqualTo("pickup")
                .jsonPath("[?(@.material==\"50044075\")].handoverOption[2].minPrice").isEqualTo(20.0)
                .jsonPath("[?(@.material==\"50044075\")].handoverOption[3].handoverOption").isEqualTo("interval-delivery")
                .jsonPath("[?(@.material==\"50044075\")].handoverOption[3].minPrice").isEqualTo(20.0)
                .jsonPath("[?(@.material==\"50044074\")].material").exists()
                .jsonPath("[?(@.material==\"50044074\")].handoverOption[0].handoverOption").isEqualTo("pickup-partner")
                .jsonPath("[?(@.material==\"50044074\")].handoverOption[0].minPrice").isEqualTo(1.0)
                .jsonPath("[?(@.material==\"50044074\")].handoverOption[1].handoverOption").isEqualTo("eta-delivery")
                .jsonPath("[?(@.material==\"50044074\")].handoverOption[1].minPrice").isEqualTo(100.0)
                .jsonPath("[?(@.material==\"50044074\")].handoverOption[1].eta").isEqualTo(120)
                .jsonPath("[?(@.material==\"50044074\")].handoverOption[2].handoverOption").isEqualTo("pickup")
                .jsonPath("[?(@.material==\"50044074\")].handoverOption[2].minPrice").isEqualTo(10.0)
                .jsonPath("[?(@.material==\"50044074\")].handoverOption[3].handoverOption").isEqualTo("interval-delivery")
                .jsonPath("[?(@.material==\"50044074\")].handoverOption[3].minPrice").isEqualTo(10.0);
    }

    public static Stream<Arguments> stringAndDoubleProvider() {
        String MOCK_PRICE_RULES = "responses/price-rules/batch-min-price-rules.json";
        String MOCK_PRICE_RULES_EMPTY = "responses/price-rules/batch-min-price-rules-empty.json";
        String MSG_DATE_NON_EXIST_HANDOVER_PRICE_RULES = "Отсутствуют данные в ответе сервиса handover-price-rules";
        String MSG_MATERIAL_NON_EXIST_CATALOG_SERVICE = "Отсутствует номенклатурный номер материала в catalog-service ";
        return Stream.of(
                arguments(MOCK_PRICE_RULES_EMPTY, "S002", "50044074", 45909.0, 1104, MSG_DATE_NON_EXIST_HANDOVER_PRICE_RULES),
                arguments(MOCK_PRICE_RULES, "S002", "00044074", 15909.0, 1105, MSG_MATERIAL_NON_EXIST_CATALOG_SERVICE),
                arguments(MOCK_PRICE_RULES_EMPTY, "S002", "50044074", -45909.0, 1104, MSG_DATE_NON_EXIST_HANDOVER_PRICE_RULES),
                arguments(MOCK_PRICE_RULES_EMPTY, "S002", "50044074", 0.0, 1104, MSG_DATE_NON_EXIST_HANDOVER_PRICE_RULES)
        );
    }

    @ParameterizedTest
    @MethodSource("stringAndDoubleProvider")
    @DisplayName("Вывод доступных способов получения товара")
    public void testShouldReturnBatchInfoNonExists(String mockPriceRules, String region, String material, Double price, int statusCode, String message) throws IOException {
        List<HandoverOption> handoverOption = List.of(HandoverOption.ETA_DELIVERY, HandoverOption.PICKUP, HandoverOption.INTERVAL_DELIVERY, HandoverOption.PICKUP_PARTNER);

        batchOptionsScenarios.configureSuccessfulResponses(
                "responses/zone-info/zone-intersection-msdelivery.json",
                "responses/providers/providers-calltovisit-s002.json",
                mockPriceRules,
                "responses/pickup-items/zone-objects.json",
                "responses/objects-public/response.json",
                "responses/pickup-point-restriction/batch.json",
                "responses/msp/batch.json",
                "responses/product-service/delivery/products.json");

        BatchRequest batch = getBatchBody(region, RetailBrand.MVIDEO, handoverOption, material, price);

        getPostResponse("/api/v2/handover-options/batch", batch)
                .expectStatus()
                .isEqualTo(500)
                .expectBody()
                .jsonPath("statusCode").isEqualTo(statusCode)
                .jsonPath("message").isEqualTo(message);
    }

    @Test
    @DisplayName("Вывод доступных способов получения товара. Список материалов пустой")
    public void testShouldReturnBatchInfoMaterialsEmpty() throws IOException {
        List<HandoverOption> handoverOption = List.of(HandoverOption.ETA_DELIVERY, HandoverOption.PICKUP, HandoverOption.INTERVAL_DELIVERY, HandoverOption.PICKUP_PARTNER);

        batchOptionsScenarios.configureSuccessfulResponses(
                "responses/zone-info/zone-intersection-msdelivery.json",
                "responses/providers/providers-calltovisit-s002.json",
                "responses/price-rules/batch-min-price-rules.json",
                "responses/pickup-items/zone-objects.json",
                "responses/objects-public/response.json",
                "responses/pickup-point-restriction/batch.json",
                "responses/msp/batch.json",
                "responses/product-service/delivery/products.json");

        BatchRequest batch = getBatchBodyEmptyMaterial("S002", RetailBrand.MVIDEO, handoverOption);

        getPostResponse("/api/v2/handover-options/batch", batch)
                .expectStatus()
                .isEqualTo(400)
                .expectBody()
                .jsonPath("statusCode").isEqualTo(1001)
                .jsonPath("message").isNotEmpty();
    }
}
