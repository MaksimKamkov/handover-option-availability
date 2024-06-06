package ru.mvideo.handoveroptionavailability.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import ru.mvideo.handoveroptionavailability.model.external.msp.logistic.response.AvailabilityOption;
import ru.mvideo.handoveroptionavailability.model.external.msp.logistic.responsenextcalendar.AvailabilityDate;
import ru.mvideo.handoveroptionavailability.model.external.msp.logistic.responsenextcalendar.AvailabilityOptionNextCalendar;

@Mapper
public interface AvailabilityOptionMapper {

	@Mapping(target = "material", source = "calendar.material")
	@Mapping(target = "handoverType", source = "calendar.handoverType")
	@Mapping(target = "handoverObject", source = "calendar.handoverObject")
	@Mapping(target = "stockObject", source = "calendar.stockObject")
	@Mapping(target = "stockObjectPriority", source = "calendar.stockObjectPriority")
	@Mapping(target = "availableStock", source = "calendar.availableStock")
	@Mapping(target = "showCaseStock", source = "calendar.showCaseStock", defaultValue = "0")
	@Mapping(target = "minQuantity", source = "calendar.minQuantity")
	@Mapping(target = "freeAvailableStock", source = "calendar.freeAvailableStock")
	@Mapping(target = "prepaidOnly", source = "calendar.prepaidOnly")
	@Mapping(target = "preorderCalendar", source = "calendar.preorderCalendar")
	@Mapping(target = "preorderPhase", source = "calendar.preorderPhase")
	@Mapping(target = "storeType", source = "calendar.storeType")
	@Mapping(target = "schemaClass", source = "calendar.schemaClass")
	@Mapping(target = "availableDate", source = "date.availableDate")
	@Mapping(target = "validTo", source = "date.validTo")
	AvailabilityOption map(AvailabilityOptionNextCalendar calendar, AvailabilityDate date);

}
