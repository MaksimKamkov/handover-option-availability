package ru.mvideo.handoveroptionavailability.mapper;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import org.junit.jupiter.api.Test;
import ru.mvideo.handoveroptionavailability.model.Address;
import ru.mvideo.handoveroptionavailability.model.AddressDetails;
import ru.mvideo.handoveroptionavailability.model.AvailableDate;
import ru.mvideo.handoveroptionavailability.model.AvailableHandoverOptionRequest;
import ru.mvideo.handoveroptionavailability.model.AvailableInterval;
import ru.mvideo.handoveroptionavailability.model.AvailablePickupPoint;
import ru.mvideo.handoveroptionavailability.model.BriefApplicableTo;
import ru.mvideo.handoveroptionavailability.model.BriefOption;
import ru.mvideo.handoveroptionavailability.model.BriefOptions;
import ru.mvideo.handoveroptionavailability.model.CoordinatePoint;
import ru.mvideo.handoveroptionavailability.model.DeliveryRequest;
import ru.mvideo.handoveroptionavailability.model.DetailedApplicableTo;
import ru.mvideo.handoveroptionavailability.model.HandoverOption;
import ru.mvideo.handoveroptionavailability.model.HandoverOptionType;
import ru.mvideo.handoveroptionavailability.model.Material;
import ru.mvideo.handoveroptionavailability.model.PartnerBrand;
import ru.mvideo.handoveroptionavailability.model.PickupAvailableAt;
import ru.mvideo.handoveroptionavailability.model.PickupPointType;
import ru.mvideo.handoveroptionavailability.model.RetailBrand;
import ru.mvideo.handoveroptionavailability.model.StockValue;
import ru.mvideo.handoveroptionavailability.model.SumObjectsOfDate;

import static org.junit.jupiter.api.Assertions.*;

public class HandoverOptionRqRsMapperTest {

	@Test
	void testAvailableHandoverOptionRequest2BriefRequestWithQuotaTrue() {
		var material = new Material();
		material.setMaterial("01234");
		material.setQty(2);
		material.setPrice(44.1);
		var request = new AvailableHandoverOptionRequest();
		request.setCachebust(true);
		request.setGetQuota(true);
		request.setRegionId("regId");
		request.setRetailBrand(RetailBrand.ELDORADO);
		request.setMaterials(List.of(material));

		var result = HandoverOptionRqRsMapper.INSTANCE.availableHandoverOptionRequest2BriefRequest(request);

		assertEquals(request.getCachebust(), result.getCachebust());
		assertEquals(HandoverOptionRqRsMapper.INTERVAL_DELIVERY_OPTION_LIST, result.getHandoverOption());
		assertEquals(List.of(material), result.getMaterials());
		assertEquals(request.getRegionId(), result.getRegionId());
		assertEquals(request.getRetailBrand(), result.getRetailBrand());
		assertFalse(result.getIncludePickupObject());
		assertNull(result.getPaymentMethod());
		assertFalse(result.getReturnPaymentConditions());
	}

	@Test
	void testAvailableHandoverOptionRequest2BriefRequestWithQuotaFalse() {
		var material = new Material();
		material.setMaterial("01234");
		material.setQty(2);
		material.setPrice(44.1);
		var request = new AvailableHandoverOptionRequest();
		request.setCachebust(true);
		request.setGetQuota(false);
		request.setRegionId("regId");
		request.setRetailBrand(RetailBrand.ELDORADO);
		request.setMaterials(List.of(material));

		var result = HandoverOptionRqRsMapper.INSTANCE.availableHandoverOptionRequest2BriefRequest(request);

		assertEquals(request.getCachebust(), result.getCachebust());
		assertEquals(HandoverOptionRqRsMapper.DELIVERY_OPTION_LIST, result.getHandoverOption());
		assertEquals(List.of(material), result.getMaterials());
		assertEquals(request.getRegionId(), result.getRegionId());
		assertEquals(request.getRetailBrand(), result.getRetailBrand());
		assertFalse(result.getIncludePickupObject());
		assertNull(result.getPaymentMethod());
		assertFalse(result.getReturnPaymentConditions());
	}

	@Test
	void testBriefOptions2HandoverOptionsBriefResponse() {
		var availablePickupPoint = new AvailablePickupPoint();
		availablePickupPoint.setPickupPointId("147");
		availablePickupPoint.setPickupPointType(PickupPointType.POSTAMAT);
		availablePickupPoint.setLatitude(21.1);
		availablePickupPoint.setLongitude(30.1);
		availablePickupPoint.setLeadTimeMax(LocalDate.MAX);
		availablePickupPoint.setLeadTimeMin(LocalDate.MIN);
		var detailedApplicableTo = new DetailedApplicableTo();
		detailedApplicableTo.setMaterial("0147");
		detailedApplicableTo.setHandoverObjectQty(4);
		detailedApplicableTo.setPrepaidQty(2);
		detailedApplicableTo.setQty(6);
		detailedApplicableTo.setShowcaseQty(1);
		detailedApplicableTo.setStocks(Map.of("any", StockValue.builder().prepaid(true).priority(1).qty(1).showcaseQty(1).build()));
		var availableDate = new AvailableDate();
		availableDate.setDate(LocalDate.now());
		availableDate.setTime("14:00");
		availableDate.setApplicableTo(List.of(detailedApplicableTo));
		var pickupAvailableAt = new PickupAvailableAt();
		pickupAvailableAt.setHandoverObject(HandoverOption.PICKUP.getValue());
		pickupAvailableAt.setPartnerBrand(PartnerBrand.PICK_POINT);
		pickupAvailableAt.setAvailableMaterials(List.of(availableDate));
		pickupAvailableAt.setAvailablePickupPoints(List.of(availablePickupPoint));
		var briefApplicableTo = new BriefApplicableTo();
		briefApplicableTo.setAvailabilityDate(LocalDate.now());
		briefApplicableTo.setMaterial("01234");
		briefApplicableTo.setQty(3);
		briefApplicableTo.setPrepaidQty(1);
		briefApplicableTo.setShowcaseQty(2);
		briefApplicableTo.setAvailableAt(List.of(pickupAvailableAt));
		var availableInterval = new AvailableInterval();
		availableInterval.setAvailabilityDate(LocalDate.now());
		availableInterval.setType("type");
		availableInterval.setMinPrice(43.1);
		availableInterval.setDateTimeFrom("13:00");
		availableInterval.setDateTimeTo("14:00");
		var briefOption = new BriefOption();
		var sumObjectsOfDate = new SumObjectsOfDate();
		sumObjectsOfDate.setAvailabilityDate(LocalDate.now());
		sumObjectsOfDate.setQtyObjects(5);
		briefOption.setHandoverOption(HandoverOption.PICKUP);
		briefOption.setAvailabilityDate(LocalDate.now());
		briefOption.setApplicableTo(List.of(briefApplicableTo));
		briefOption.setPartnerBrand(List.of(PartnerBrand.FIVE, PartnerBrand.POST));
		briefOption.setAvailableIntervals(List.of(availableInterval));
		briefOption.setSumObjectsOfDate(List.of(sumObjectsOfDate));
		var response = new BriefOptions();
		response.setType(HandoverOptionType.PICKUP);
		response.setOptions(List.of(briefOption));

		var result = HandoverOptionRqRsMapper.INSTANCE.briefOptions2HandoverOptionsBriefResponse(response);

		assertEquals(response.getType(), result.getType());
		assertEquals(response.getOptions().get(0).getHandoverOption(), result.getOptions().get(0).getHandoverOption());
		assertEquals(response.getOptions().get(0).getAvailabilityDate(), result.getOptions().get(0).getAvailabilityDate());
		assertEquals(response.getOptions().get(0).getEta(), result.getOptions().get(0).getEta());
		assertEquals(response.getOptions().get(0).getMinPrice(), result.getOptions().get(0).getMinPrice());
		assertEquals(response.getOptions().get(0).getTime(), result.getOptions().get(0).getTime());
		assertEquals(response.getOptions().get(0).getAvailableIntervals(), result.getOptions().get(0).getAvailableIntervals());
		assertEquals(response.getOptions().get(0).getPartnerBrand(), result.getOptions().get(0).getPartnerBrand());
		assertEquals(response.getOptions().get(0).getSumObjectsOfDate(), result.getOptions().get(0).getSumObjectsOfDate());
		assertEquals(response.getOptions().get(0).getApplicableTo().get(0).getAvailabilityDate(), result.getOptions().get(0).getApplicableTo().get(0).getAvailabilityDate());
		assertEquals(response.getOptions().get(0).getApplicableTo().get(0).getMaterial(), result.getOptions().get(0).getApplicableTo().get(0).getMaterial());
		assertEquals(response.getOptions().get(0).getApplicableTo().get(0).getQty(), result.getOptions().get(0).getApplicableTo().get(0).getQty());
		assertEquals(response.getOptions().get(0).getApplicableTo().get(0).getPrepaidQty(), result.getOptions().get(0).getApplicableTo().get(0).getPrepaidQty());
		assertEquals(Objects.equals(response.getOptions().get(0).getApplicableTo().get(0).getQty(), response.getOptions().get(0).getApplicableTo().get(0).getShowcaseQty()), result.getOptions().get(0).getApplicableTo().get(0).getShowcaseQty());
	}

	@Test
	void testDeliveryRequest2DeliveryRequestV2() {
		var request = new DeliveryRequest();
		var address = new Address();
		var addressDetails = new AddressDetails();
		addressDetails.setArea("area");
		addressDetails.setCity("city");
		addressDetails.setStreet("street");
		addressDetails.setHouse("5");
		address.setData(addressDetails);
		var coordinatePoint = new CoordinatePoint();
		coordinatePoint.setLatitude(21.1);
		coordinatePoint.setLongitude(11.0);
		var material = new Material();
		material.setMaterial("01234");
		material.setQty(2);
		material.setPrice(44.1);
		request.setRegionId("S002");
		request.setHandoverOption(HandoverOption.ETA_DELIVERY);
		request.setDestination("ул. Краснопреснинская д.1");
		request.setCoordinate(coordinatePoint);
		request.setAddress(address);
		request.setIncludeStocks(true);
		request.setRetailBrand(RetailBrand.ELDORADO);
		request.setMaterials(List.of(material));

		var result = HandoverOptionRqRsMapper.INSTANCE.deliveryRequest2DeliveryRequestV2(request);

		assertEquals(List.of(request.getHandoverOption()), result.getHandoverOption());
		assertEquals(request.getAddress().getData(), result.getAddress().getData());
		assertEquals(request.getMaterials(), result.getMaterials());
		assertEquals(request.getRegionId(), result.getRegionId());
		assertEquals(request.getRetailBrand(), result.getRetailBrand());
		assertEquals(request.getCoordinate(), result.getAddress().getCoordinate());
		assertEquals(request.getDestination(), result.getAddress().getRepresentation());
		assertEquals(request.getIncludeStocks(), result.getIncludeStocks());
	}

	@Test
	void testDeliveryRequest2DeliveryRequestV2WithDefaults() {
		var request = new DeliveryRequest();
		var address = new Address();
		var addressDetails = new AddressDetails();
		var coordinatePoint = new CoordinatePoint();
		coordinatePoint.setLatitude(22.1);
		coordinatePoint.setLongitude(12.0);
		addressDetails.setArea("area");
		addressDetails.setCity("city");
		addressDetails.setStreet("street");
		addressDetails.setHouse("5");
		address.setData(addressDetails);
		address.setCoordinate(coordinatePoint);
		address.setRepresentation("ул. Краснопреснинская д.2");
		var material = new Material();
		material.setMaterial("01234");
		material.setQty(2);
		material.setPrice(44.1);
		request.setRegionId("S002");
		request.setHandoverOption(HandoverOption.ETA_DELIVERY);
		request.setAddress(address);
		request.setIncludeStocks(true);
		request.setRetailBrand(RetailBrand.ELDORADO);
		request.setMaterials(List.of(material));

		var result = HandoverOptionRqRsMapper.INSTANCE.deliveryRequest2DeliveryRequestV2(request);

		assertEquals(List.of(request.getHandoverOption()), result.getHandoverOption());
		assertEquals(request.getAddress().getData(), result.getAddress().getData());
		assertEquals(request.getMaterials(), result.getMaterials());
		assertEquals(request.getRegionId(), result.getRegionId());
		assertEquals(request.getRetailBrand(), result.getRetailBrand());
		assertEquals(request.getAddress().getCoordinate(), result.getAddress().getCoordinate());
		assertEquals(request.getAddress().getRepresentation(), result.getAddress().getRepresentation());
		assertEquals(request.getIncludeStocks(), result.getIncludeStocks());
	}
}
