package ru.mvideo.handoveroptionavailability.mapper;

import java.util.Collections;
import java.util.List;
import java.util.Objects;
import org.mapstruct.Builder;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.ReportingPolicy;
import org.mapstruct.factory.Mappers;
import ru.mvideo.handoveroptionavailability.model.Address;
import ru.mvideo.handoveroptionavailability.model.AvailabilityApplicableTo;
import ru.mvideo.handoveroptionavailability.model.AvailableHandoverOptionRequest;
import ru.mvideo.handoveroptionavailability.model.BriefApplicableTo;
import ru.mvideo.handoveroptionavailability.model.BriefOptions;
import ru.mvideo.handoveroptionavailability.model.BriefRequest;
import ru.mvideo.handoveroptionavailability.model.CoordinatePoint;
import ru.mvideo.handoveroptionavailability.model.DeliveryProvidersRequest;
import ru.mvideo.handoveroptionavailability.model.DeliveryProvidersRequestV2;
import ru.mvideo.handoveroptionavailability.model.DeliveryRequest;
import ru.mvideo.handoveroptionavailability.model.DeliveryRequestV2;
import ru.mvideo.handoveroptionavailability.model.HandoverOption;
import ru.mvideo.handoveroptionavailability.model.HandoverOptionsBriefResponse;

@Mapper(unmappedTargetPolicy = ReportingPolicy.IGNORE, builder = @Builder(disableBuilder = true))
public interface HandoverOptionRqRsMapper {

	HandoverOptionRqRsMapper INSTANCE = Mappers.getMapper(HandoverOptionRqRsMapper.class);

	List<HandoverOption> DELIVERY_OPTION_LIST = List.of(
			HandoverOption.DPD_DELIVERY,
			HandoverOption.ETA_DELIVERY,
			HandoverOption.EXACTLY_TIME_DELIVERY,
			HandoverOption.ELECTRONIC_DELIVERY,
			HandoverOption.PICKUP,
			HandoverOption.PICKUP_PARTNER,
			HandoverOption.PICKUP_SEAMLESS
	);

	List<HandoverOption> INTERVAL_DELIVERY_OPTION_LIST = List.of(
			HandoverOption.INTERVAL_DELIVERY,
			HandoverOption.DPD_DELIVERY,
			HandoverOption.ETA_DELIVERY,
			HandoverOption.EXACTLY_TIME_DELIVERY,
			HandoverOption.ELECTRONIC_DELIVERY,
			HandoverOption.PICKUP,
			HandoverOption.PICKUP_PARTNER,
			HandoverOption.PICKUP_SEAMLESS
	);

	@Mapping(source = "getQuota", target = "handoverOption")
	BriefRequest availableHandoverOptionRequest2BriefRequest(AvailableHandoverOptionRequest request);

	default List<HandoverOption> getQuota2HandoverOption(Boolean getQuota) {
		return getQuota ? INTERVAL_DELIVERY_OPTION_LIST : DELIVERY_OPTION_LIST;
	}

	HandoverOptionsBriefResponse briefOptions2HandoverOptionsBriefResponse(BriefOptions response);

	default AvailabilityApplicableTo briefApplicableTo2AvailabilityApplicableTo(BriefApplicableTo briefApplicableTo) {
		var availabilityApplicableTo = new AvailabilityApplicableTo();
		availabilityApplicableTo.setMaterial(briefApplicableTo.getMaterial());
		availabilityApplicableTo.setAvailabilityDate(briefApplicableTo.getAvailabilityDate());
		availabilityApplicableTo.setQty(briefApplicableTo.getQty());
		availabilityApplicableTo.setPrepaidQty(briefApplicableTo.getPrepaidQty());
		availabilityApplicableTo.setShowcaseQty(Objects.equals(briefApplicableTo.getQty(), briefApplicableTo.getShowcaseQty()));
		return availabilityApplicableTo;
	}

	@Mapping(source = "destination", target = "address.representation", defaultExpression = "java(getDefaultRepresentation(deliveryRequest.getAddress()))")
	@Mapping(source = "coordinate", target = "address.coordinate", defaultExpression = "java(getDefaultCoordinate(deliveryRequest.getAddress()))")
	@Mapping(target = "handoverOption", expression = "java(handoverOptionToList(request.getHandoverOption()))")
	@Mapping(source = "address.data", target = "address.data")
	DeliveryRequestV2 deliveryRequest2DeliveryRequestV2(DeliveryRequest request);

	default List<HandoverOption> handoverOptionToList(HandoverOption handoverOption) {
		return Collections.singletonList(handoverOption);
	}

	default String getDefaultRepresentation(Address address) {
		return address == null ? null : address.getRepresentation();
	}

	default CoordinatePoint getDefaultCoordinate(Address address) {
		return address == null ? null : address.getCoordinate();
	}

	@Mapping(source = "recipient.address", target = "recipient.address.representation")
	@Mapping(source = "recipient.coordinate", target = "recipient.address.coordinate")
	@Mapping(source = "recipient.objectId", target = "recipient.objectId")
	@Mapping(source = "source.address", target = "source.address.representation")
	@Mapping(source = "source.coordinate", target = "source.address.coordinate")
	@Mapping(source = "source.objectId", target = "source.objectId")
	DeliveryProvidersRequestV2 deliveryProvidersRequest2DeliveryProvidersRequestV2(DeliveryProvidersRequest request);
}
