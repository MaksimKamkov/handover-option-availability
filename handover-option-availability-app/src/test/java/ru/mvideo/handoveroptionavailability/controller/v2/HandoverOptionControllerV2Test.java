package ru.mvideo.handoveroptionavailability.controller.v2;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.function.Function;
import java.util.stream.Collectors;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import ru.mvideo.handoveroptionavailability.AbstractIT;
import ru.mvideo.handoveroptionavailability.model.Address;
import ru.mvideo.handoveroptionavailability.model.AddressDetails;
import ru.mvideo.handoveroptionavailability.model.BatchRequest;
import ru.mvideo.handoveroptionavailability.model.BriefRequest;
import ru.mvideo.handoveroptionavailability.model.CoordinatePoint;
import ru.mvideo.handoveroptionavailability.model.DeliveryRequestV2;
import ru.mvideo.handoveroptionavailability.model.HandoverOption;
import ru.mvideo.handoveroptionavailability.model.HandoverOptionType;
import ru.mvideo.handoveroptionavailability.model.Material;
import ru.mvideo.handoveroptionavailability.model.MaterialHandoverOptions;
import ru.mvideo.handoveroptionavailability.model.MaterialWithoutQty;
import ru.mvideo.handoveroptionavailability.model.PaymentMethod;
import ru.mvideo.handoveroptionavailability.model.PickupRequest;
import ru.mvideo.handoveroptionavailability.model.RetailBrand;
import ru.mvideo.handoveroptionavailability.security.WithMockUser;
import java.io.IOException;
import java.util.List;

@DisplayName("/api/v2/handover-options/delivery")
class HandoverOptionControllerV2Test extends AbstractIT {

	@Test
	@DisplayName("Получение списка доставок для операции delivery")
	@WithMockUser(authorities = "HANDOVER_OPTIONS_DELIVERY")
	void shouldReturnDeliveryOptions() throws IOException {
		deliveryScenarios.configureSuccessfulResponses();

		DeliveryRequestV2 deliveryRequestV2 = DeliveryRequestV2.builder()
				.regionId("S002")
				.includeStocks(true)
				.handoverOption(List.of(HandoverOption.ETA_DELIVERY))
				.retailBrand(RetailBrand.MVIDEO)
				.address(Address.builder()
						.representation("Москва, улица Большая Дмитровка, 26")
						.data(AddressDetails.builder()
								.streetType("ул")
								.street("Дмитровка Б.")
								.build())
						.coordinate(CoordinatePoint.builder()
								.latitude(55.764620)
								.longitude(37.61220)
								.qcGeo(CoordinatePoint.QcGeoEnum.EXACTLY)
								.build())
						.build())
				.materials(List.of(Material.builder()
						.material("50044074")
						.qty(1)
						.price(17990.0)
						.build()))
				.build();

		getPostResponse("/api/v2/handover-options/delivery", deliveryRequestV2)
				.expectStatus().isOk()
				.expectBody()
				.jsonPath("$[0].handoverOption").isEqualTo(HandoverOption.ETA_DELIVERY.getValue())
				.jsonPath("$[0].minPrice").isEqualTo(100)
				.jsonPath("$[0].eta").isEqualTo(120)
				.jsonPath("$[0].handoverOptionMaterial").isEqualTo("6004957")
				.jsonPath("$[0].availableAt[0].handoverObject").isEqualTo("S018");
	}

	@Test
	@DisplayName("Получение списка для операции pickup (pickup-seamless)")
	@WithMockUser(authorities = "HANDOVER_OPTIONS_PICKUP")
	void shouldReturnPickupSeamlessOptions() throws IOException {
		pickupOptionsScenarios.configureSeamlessResponses();

		PickupRequest pickupRequest = PickupRequest.builder()
				.regionId("S002")
				.retailBrand(RetailBrand.MVIDEO)
				.handoverOption(List.of(HandoverOption.PICKUP_SEAMLESS))
				.materials(List.of(
						Material.builder().material("10008177").qty(1).price(5000d).build(),
						Material.builder().material("10004682").qty(1).price(5000d).build()
				))
				.build();

		getPostResponse("/api/v2/handover-options/pickup", pickupRequest)
				.expectStatus().isOk()
				.expectBody()
				.jsonPath("$[0].handoverOption").isEqualTo(HandoverOption.PICKUP_SEAMLESS.getValue())
				.jsonPath("$[0].minPrice").isEqualTo(0)
				.jsonPath("$[0].handoverOptionMaterial").isEqualTo("6016405")
				.jsonPath("$[0].availableAt.length()").isEqualTo(24);
	}

	@Test
	@DisplayName("Получение списка для операции pickup (pickup-partner)")
	@WithMockUser(authorities = "HANDOVER_OPTIONS_PICKUP")
	void shouldReturnPickupPartnerOptions() throws IOException {
		pickupOptionsScenarios.configurePartnerResponses();

		PickupRequest pickupRequest = PickupRequest.builder()
				.regionId("S002")
				.retailBrand(RetailBrand.MVIDEO)
				.handoverOption(List.of(HandoverOption.PICKUP_PARTNER))
				.materials(List.of(
						Material.builder().material("10008177").qty(1).price(5000d).build()
				))
				.build();

		getPostResponse("/api/v2/handover-options/pickup", pickupRequest)
				.expectStatus().isOk()
				.expectBody()
				.jsonPath("$[0].handoverOption").isEqualTo(HandoverOption.PICKUP_PARTNER.getValue())
				.jsonPath("$[0].minPrice").isEqualTo(0)
				.jsonPath("$[0].handoverOptionMaterial").isEqualTo("6004956")
				.jsonPath("$[0].availableAt.length()").isEqualTo(1)
				.jsonPath("$[0].availableAt[0].partnerBrand").isEqualTo("POST")
				.jsonPath("$[0].availableAt[0].availablePickupPoints.length()").isEqualTo(1)
				.jsonPath("$[0].availableAt[0].availablePickupPoints[0].pickupPointType").isEqualTo("pickup-point");
	}

	@Test
	@DisplayName("Получение списка для операции batch с двумя материалами")
	@WithMockUser(authorities = "HANDOVER_OPTIONS_BATCH")
	@Disabled
	void shouldReturnBatchOptions() throws IOException {
		batchOptionsScenarios.configureDoubleMaterialBatchResponses();

		BatchRequest batchRequest = BatchRequest.builder()
				.regionId("S002")
				.retailBrand(RetailBrand.MVIDEO)
				.handoverOption(List.of(HandoverOption.PICKUP, HandoverOption.PICKUP_PARTNER, HandoverOption.ETA_DELIVERY))
				.materials(List.of(
						MaterialWithoutQty.builder().material("20030013").price(2000d).build(),
						MaterialWithoutQty.builder().material("20030060").price(3000d).build()
				))
				.build();

		getPostResponse("/api/v2/handover-options/batch", batchRequest)
				.expectStatus().isOk()
				.expectBodyList(MaterialHandoverOptions.class)
				.consumeWith(result -> {
					var actualMap = result.getResponseBody().stream()
							.collect(Collectors.toMap(MaterialHandoverOptions::getMaterial, Function.identity()));
					Assertions.assertEquals(2, actualMap.size());
					MaterialHandoverOptions first = actualMap.get("20030013");
					MaterialHandoverOptions second = actualMap.get("20030060");

					Assertions.assertNotNull(first);
					Assertions.assertEquals(2, first.getHandoverOption().size());
					Assertions.assertEquals("eta-delivery", first.getHandoverOption().get(0).getHandoverOption());
					Assertions.assertEquals(300, first.getHandoverOption().get(0).getMinPrice());
					Assertions.assertEquals(120, first.getHandoverOption().get(0).getEta());

					Assertions.assertEquals("pickup", first.getHandoverOption().get(1).getHandoverOption());
					Assertions.assertEquals(200, first.getHandoverOption().get(1).getMinPrice());

					Assertions.assertNotNull(second);
					Assertions.assertEquals(3, second.getHandoverOption().size());
					Assertions.assertEquals("pickup-partner", second.getHandoverOption().get(0).getHandoverOption());
					Assertions.assertEquals(100, second.getHandoverOption().get(0).getMinPrice());
					Assertions.assertEquals(2, second.getHandoverOption().get(0).getPartnerBrand().size());

					Assertions.assertEquals("eta-delivery", second.getHandoverOption().get(1).getHandoverOption());
					Assertions.assertEquals(300, second.getHandoverOption().get(1).getMinPrice());
					Assertions.assertEquals(120, second.getHandoverOption().get(1).getEta());

					Assertions.assertEquals("pickup", second.getHandoverOption().get(2).getHandoverOption());
					Assertions.assertEquals(200, second.getHandoverOption().get(2).getMinPrice());
				});
	}

	@ParameterizedTest(name = "Получение списка доставок для операции delivery с PaymentMethod=CREDIT, PaymentConditions={2} и qty={0}")
	@CsvSource({
			"1,   583, false",
			"584, 989, false",
			"990, [],  false",
			"1,   583, true",
			"584, 989, true",
			"990, [],  true"
	})
	@WithMockUser(authorities = "HANDOVER_OPTIONS_DELIVERY")
	void shouldReturnDeliveryOptionsPaymentCredit(int reqQty, String result, Boolean returnPaymentCondition) throws IOException {
		DateTimeFormatter availableDateFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
		handoverOptionsScenarios.configureSuccessfulResponses("responses/msp/quotas.json", "responses/msp/credit-delivery.json");

		DeliveryRequestV2 deliveryRequestV2 = DeliveryRequestV2.builder()
				.regionId("S002")
				.retailBrand(RetailBrand.MVIDEO)
				.returnPaymentConditions(returnPaymentCondition)
				.includeStocks(false)
				.paymentMethod(PaymentMethod.CREDIT)
				.address(Address.builder()
						.representation("Москва, улица Большая Дмитровка, 26")
						.coordinate(CoordinatePoint.builder()
								.latitude(55.764620)
								.longitude(37.61220)
								.qcGeo(CoordinatePoint.QcGeoEnum.EXACTLY)
								.build())
						.build())
				.materials(List.of(Material.builder()
						.material("50044074")
						.qty(reqQty)
						.price(17990.0)
						.build()))
				.build();

		if(reqQty > 0 && reqQty < 989) {
			if(returnPaymentCondition == false) {
				getPostResponse("/api/v2/handover-options/delivery", deliveryRequestV2)
						.expectStatus().isOk()
						.expectBody()
						.jsonPath("$[0].handoverOption").isEqualTo(HandoverOption.INTERVAL_DELIVERY.getValue())
						.jsonPath("$[0].minPrice").isEqualTo("0.0")
						.jsonPath("$[0].availabilityDate").isEqualTo(availableDateFormatter.format(LocalDateTime.now().plusDays(3)))
						.jsonPath("$[0].maxDeliveryDate").isEqualTo(availableDateFormatter.format(LocalDateTime.now().plusDays(30)))
						.jsonPath("$[0].paymentConditions").doesNotExist()
						.jsonPath("$[0].availableAt.length()").isEqualTo(1)
						.jsonPath("$[0].availableAt[0].availableDates.length()").isEqualTo(1)
						.jsonPath("$[0].availableAt[0].availableDates[0].date").isEqualTo(availableDateFormatter.format(LocalDateTime.now().plusDays(3)))
						.jsonPath("$[0].availableAt[0].availableDates[0].applicableTo[0].material").isEqualTo("50044074")
						.jsonPath("$[0].availableAt[0].availableDates[0].applicableTo[0].qty").isEqualTo(result);
			} if(returnPaymentCondition == true) {
				getPostResponse("/api/v2/handover-options/delivery", deliveryRequestV2)
						.expectStatus().isOk()
						.expectBody()
						.jsonPath("$[0].handoverOption").isEqualTo(HandoverOption.INTERVAL_DELIVERY.getValue())
						.jsonPath("$[0].minPrice").isEqualTo("0.0")
						.jsonPath("$[0].availabilityDate").isEqualTo(availableDateFormatter.format(LocalDateTime.now().plusDays(3)))
						.jsonPath("$[0].maxDeliveryDate").isEqualTo(availableDateFormatter.format(LocalDateTime.now().plusDays(30)))
						.jsonPath("$[0].paymentConditions.length()").isEqualTo(1)
						.jsonPath("$[0].availableAt.length()").isEqualTo(1)
						.jsonPath("$[0].paymentConditions[0]").isEqualTo(PaymentMethod.CREDIT.getValue())
						.jsonPath("$[0].availableAt[0].availableDates.length()").isEqualTo(1)
						.jsonPath("$[0].availableAt[0].availableDates[0].date").isEqualTo(availableDateFormatter.format(LocalDateTime.now().plusDays(3)))
						.jsonPath("$[0].availableAt[0].availableDates[0].applicableTo[0].material").isEqualTo("50044074")
						.jsonPath("$[0].availableAt[0].availableDates[0].applicableTo[0].qty").isEqualTo(result);
			}
		} else {
			getPostResponse("/api/v2/handover-options/delivery", deliveryRequestV2)
					.expectStatus().isOk()
					.expectBody().json(result);
		}
	}

	@ParameterizedTest(name = "Получение списка для операции pickup с PaymentMethod=CREDIT, PaymentConditions={2} и qty={0}")
	@CsvSource({
			"1,   495, false, S085",
			"500, 912, false, S716",
			"913, [],  false, null",
			"1,   495, true,  S085",
			"584, 912, true,  S716",
			"913, [],  true,  null"
	})
	@WithMockUser(authorities = "HANDOVER_OPTIONS_PICKUP")
	void shouldReturnPickupOptionsPaymentCredit(int reqQty, String result, Boolean returnPaymentCondition, String stockObject) throws IOException {
		DateTimeFormatter availableDateFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
		pickupOptionsScenarios.configureCreditResponses();

		PickupRequest pickupRequest = PickupRequest.builder()
				.regionId("S002")
				.retailBrand(RetailBrand.MVIDEO)
				.returnPaymentConditions(returnPaymentCondition)
				.includeStocks(false)
				.paymentMethod(PaymentMethod.CREDIT)
				.materials(List.of(
						Material.builder().material("50044074").qty(reqQty).price(5000d).build()
				))
				.build();

		if(reqQty > 0 && reqQty < 912) {
			if(returnPaymentCondition == false) {
				getPostResponse("/api/v2/handover-options/pickup", pickupRequest)
						.expectStatus().isOk()
						.expectBody()
						.jsonPath("$[0].handoverOption").isEqualTo(HandoverOption.PICKUP.getValue())
						.jsonPath("$[0].minPrice").isEqualTo("200.0")
						.jsonPath("$[0].handoverOptionMaterial").isEqualTo("6004956")
						.jsonPath("$[0].availabilityDate").isEqualTo(availableDateFormatter.format(LocalDateTime.now().plusDays(1)))
						.jsonPath("$[0].maxDeliveryDate").isEqualTo(availableDateFormatter.format(LocalDateTime.now().plusDays(30)))
						.jsonPath("$[0].paymentConditions").doesNotExist()
						.jsonPath("$[0].availableAt.length()").isEqualTo(1)
						.jsonPath("$[0].availableAt[0].handoverObject").isEqualTo(stockObject)
						.jsonPath("$[0].availableAt[0].availableMaterials.length()").isEqualTo(1)
						.jsonPath("$[0].availableAt[0].availableMaterials[0].date").isEqualTo(availableDateFormatter.format(LocalDateTime.now().plusDays(1)))
						.jsonPath("$[0].availableAt[0].availableMaterials[0].applicableTo.length()").isEqualTo(1)
						.jsonPath("$[0].availableAt[0].availableMaterials[0].applicableTo[0].material").isEqualTo("50044074")
						.jsonPath("$[0].availableAt[0].availableMaterials[0].applicableTo[0].qty").isEqualTo(result);
			} if(returnPaymentCondition == true) {
				getPostResponse("/api/v2/handover-options/pickup", pickupRequest)
						.expectStatus().isOk()
						.expectBody()
						.jsonPath("$[0].handoverOption").isEqualTo(HandoverOption.PICKUP.getValue())
						.jsonPath("$[0].minPrice").isEqualTo("200.0")
						.jsonPath("$[0].handoverOptionMaterial").isEqualTo("6004956")
						.jsonPath("$[0].availabilityDate").isEqualTo(availableDateFormatter.format(LocalDateTime.now().plusDays(1)))
						.jsonPath("$[0].maxDeliveryDate").isEqualTo(availableDateFormatter.format(LocalDateTime.now().plusDays(30)))
						.jsonPath("$[0].paymentConditions.length()").isEqualTo(1)
						.jsonPath("$[0].availableAt.length()").isEqualTo(1)
						.jsonPath("$[0].paymentConditions[0]").isEqualTo(PaymentMethod.CREDIT.getValue())
						.jsonPath("$[0].availableAt.length()").isEqualTo(1)
						.jsonPath("$[0].availableAt[0].handoverObject").isEqualTo(stockObject)
						.jsonPath("$[0].availableAt[0].availableMaterials.length()").isEqualTo(1)
						.jsonPath("$[0].availableAt[0].availableMaterials[0].date").isEqualTo(availableDateFormatter.format(LocalDateTime.now().plusDays(1)))
						.jsonPath("$[0].availableAt[0].availableMaterials[0].applicableTo.length()").isEqualTo(1)
						.jsonPath("$[0].availableAt[0].availableMaterials[0].applicableTo[0].material").isEqualTo("50044074")
						.jsonPath("$[0].availableAt[0].availableMaterials[0].applicableTo[0].qty").isEqualTo(result);
			}
		} else {
			getPostResponse("/api/v2/handover-options/pickup", pickupRequest)
					.expectStatus().isOk()
					.expectBody().json(result);
		}
	}

	@ParameterizedTest(name = "Получение списка доступных способов получения товаров с PaymentMethod=CREDIT, PaymentConditions={2} и qty={0}")
	@CsvSource({
			"1,   583, false",
			"584, 989, false",
			"990, [],  false",
			"1,   583, true",
			"584, 989, true",
			"990, [],  true"
	})
	@WithMockUser(authorities = "HANDOVER_OPTIONS")
	void shouldReturnHandoverOptionsPaymentCredit(int reqQty, String result, Boolean returnPaymentCondition) throws IOException {
		DateTimeFormatter availableDateFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
		handoverOptionsScenarios.configureSuccessfulResponses("responses/msp/quotas.json", "responses/msp/credit-delivery.json");

		BriefRequest briefRequest = BriefRequest.builder()
				.regionId("S002")
				.retailBrand(RetailBrand.MVIDEO)
				.returnPaymentConditions(returnPaymentCondition)
				.paymentMethod(PaymentMethod.CREDIT)
				.materials(List.of(
						Material.builder().material("50044074").qty(reqQty).price(5000d).build()
				))
				.build();

		if(reqQty > 0 && reqQty < 989) {
			if(returnPaymentCondition == false) {
				getPostResponse("/api/v2/handover-options", briefRequest)
						.expectStatus().isOk()
						.expectBody()
						.jsonPath("$[0].type").isEqualTo(HandoverOptionType.DELIVERY.getValue())
						.jsonPath("$[0].options.length()").isEqualTo(1)
						.jsonPath("$[0].options[0].handoverOption").isEqualTo(HandoverOption.INTERVAL_DELIVERY.getValue())
						.jsonPath("$[0].options[0].minPrice").isEqualTo("0.0")
						.jsonPath("$[0].options[0].availabilityDate").isEqualTo(availableDateFormatter.format(LocalDateTime.now().plusDays(3)))
						.jsonPath("$[0].options[0].maxDeliveryDate").isEqualTo(availableDateFormatter.format(LocalDateTime.now().plusDays(30)))
						.jsonPath("$[0].options[0].paymentConditions").doesNotExist()
						.jsonPath("$[0].options[0].availableIntervals.length()").isEqualTo(1)
						.jsonPath("$[0].options[0].availableIntervals[0].minPrice").isEqualTo("0.0")
						.jsonPath("$[0].options[0].availableIntervals[0].type").isEqualTo("regular")
						.jsonPath("$[0].options[0].applicableTo.length()").isEqualTo(1)
						.jsonPath("$[0].options[0].applicableTo[0].material").isEqualTo("50044074")
						.jsonPath("$[0].options[0].applicableTo[0].qty").isEqualTo(result)
						.jsonPath("$[0].options[0].applicableTo[0].availabilityDate").isEqualTo(availableDateFormatter.format(LocalDateTime.now().plusDays(3)))
						.jsonPath("$[1].type").isEqualTo(HandoverOptionType.PICKUP.getValue())
						.jsonPath("$[1].options.length()").isEqualTo(1)
						.jsonPath("$[1].options[0].handoverOption").isEqualTo(HandoverOption.PICKUP.getValue())
						.jsonPath("$[1].options[0].minPrice").isEqualTo("0.0")
						.jsonPath("$[1].options[0].availabilityDate").isEqualTo(availableDateFormatter.format(LocalDateTime.now().plusDays(3)))
						.jsonPath("$[1].options[0].maxDeliveryDate").isEqualTo(availableDateFormatter.format(LocalDateTime.now().plusDays(30)))
						.jsonPath("$[1].options[0].paymentConditions").doesNotExist()
						.jsonPath("$[1].options[0].sumObjectsOfDate.length()").isEqualTo(1)
						.jsonPath("$[1].options[0].sumObjectsOfDate[0].qtyObjects").isEqualTo(1)
						.jsonPath("$[1].options[0].applicableTo.length()").isEqualTo(1)
						.jsonPath("$[1].options[0].applicableTo[0].material").isEqualTo("50044074")
						.jsonPath("$[1].options[0].applicableTo[0].qty").isEqualTo(result)
						.jsonPath("$[1].options[0].applicableTo[0].availabilityDate").isEqualTo(availableDateFormatter.format(LocalDateTime.now().plusDays(3)));
			} if(returnPaymentCondition == true) {
				getPostResponse("/api/v2/handover-options", briefRequest)
						.expectStatus().isOk()
						.expectBody()
						.jsonPath("$[0].type").isEqualTo(HandoverOptionType.DELIVERY.getValue())
						.jsonPath("$[0].options.length()").isEqualTo(1)
						.jsonPath("$[0].options[0].handoverOption").isEqualTo(HandoverOption.INTERVAL_DELIVERY.getValue())
						.jsonPath("$[0].options[0].minPrice").isEqualTo("0.0")
						.jsonPath("$[0].options[0].availabilityDate").isEqualTo(availableDateFormatter.format(LocalDateTime.now().plusDays(3)))
						.jsonPath("$[0].options[0].maxDeliveryDate").isEqualTo(availableDateFormatter.format(LocalDateTime.now().plusDays(30)))
						.jsonPath("$[0].options[0].paymentConditions.length()").isEqualTo(1)
						.jsonPath("$[0].options[0].paymentConditions[0]").isEqualTo(PaymentMethod.CREDIT.getValue())
						.jsonPath("$[0].options[0].availableIntervals.length()").isEqualTo(1)
						.jsonPath("$[0].options[0].availableIntervals[0].minPrice").isEqualTo("0.0")
						.jsonPath("$[0].options[0].availableIntervals[0].type").isEqualTo("regular")
						.jsonPath("$[0].options[0].applicableTo.length()").isEqualTo(1)
						.jsonPath("$[0].options[0].applicableTo[0].material").isEqualTo("50044074")
						.jsonPath("$[0].options[0].applicableTo[0].qty").isEqualTo(result)
						.jsonPath("$[0].options[0].applicableTo[0].availabilityDate").isEqualTo(availableDateFormatter.format(LocalDateTime.now().plusDays(3)))
						.jsonPath("$[1].type").isEqualTo(HandoverOptionType.PICKUP.getValue())
						.jsonPath("$[1].options.length()").isEqualTo(1)
						.jsonPath("$[1].options[0].handoverOption").isEqualTo(HandoverOption.PICKUP.getValue())
						.jsonPath("$[1].options[0].minPrice").isEqualTo("0.0")
						.jsonPath("$[1].options[0].availabilityDate").isEqualTo(availableDateFormatter.format(LocalDateTime.now().plusDays(3)))
						.jsonPath("$[1].options[0].maxDeliveryDate").isEqualTo(availableDateFormatter.format(LocalDateTime.now().plusDays(30)))
						.jsonPath("$[1].options[0].paymentConditions.length()").isEqualTo(1)
						.jsonPath("$[1].options[0].paymentConditions[0]").isEqualTo(PaymentMethod.CREDIT.getValue())
						.jsonPath("$[1].options[0].sumObjectsOfDate.length()").isEqualTo(1)
						.jsonPath("$[1].options[0].sumObjectsOfDate[0].qtyObjects").isEqualTo(1)
						.jsonPath("$[1].options[0].applicableTo.length()").isEqualTo(1)
						.jsonPath("$[1].options[0].applicableTo[0].material").isEqualTo("50044074")
						.jsonPath("$[1].options[0].applicableTo[0].qty").isEqualTo(result)
						.jsonPath("$[1].options[0].applicableTo[0].availabilityDate").isEqualTo(availableDateFormatter.format(LocalDateTime.now().plusDays(3)));;
			}
		} else {
			getPostResponse("/api/v2/handover-options", briefRequest)
					.expectStatus().isOk()
					.expectBody().json(result);
		}
	}
}
