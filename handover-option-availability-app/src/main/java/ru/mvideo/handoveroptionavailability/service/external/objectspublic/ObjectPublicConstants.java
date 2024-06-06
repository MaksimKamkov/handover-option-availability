package ru.mvideo.handoveroptionavailability.service.external.objectspublic;

import java.util.List;
import java.util.Set;
import lombok.experimental.UtilityClass;

@UtilityClass
public class ObjectPublicConstants {
	public static final String LATITUDE = "latitude";
	public static final String LONGITUDE = "longitude";
	public static final String IS_AUTO_COURIER_AVAILABLE = "isAutoCourierAvailable";
	public static final String DELIVERY_SCHEDULE = "deliverySchedule";
	public static final String TIME_ZONE = "timeZone";
	public static final String WORK_SCHEDULE = "workSchedule";
	public static final String OBJECT_STATUS = "OPEN";
	public static final String GENERAL_TYPE = "SHOP";
	public static final Set<String> REQUIRED_ATTRIBUTES = Set.of(LONGITUDE, LATITUDE, TIME_ZONE, IS_AUTO_COURIER_AVAILABLE,
			DELIVERY_SCHEDULE, WORK_SCHEDULE);
	public static final List<String> H_O_ATTRIBUTES = List.of(DELIVERY_SCHEDULE, WORK_SCHEDULE, TIME_ZONE, IS_AUTO_COURIER_AVAILABLE);
	public static final List<String> H_O_COORDINATE_ATTRIBUTES = List.of(LONGITUDE, LATITUDE);
}
