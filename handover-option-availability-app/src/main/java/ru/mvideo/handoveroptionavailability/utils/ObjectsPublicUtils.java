package ru.mvideo.handoveroptionavailability.utils;

import static ru.mvideo.handoveroptionavailability.service.external.objectspublic.ObjectPublicConstants.DELIVERY_SCHEDULE;
import static ru.mvideo.handoveroptionavailability.service.external.objectspublic.ObjectPublicConstants.IS_AUTO_COURIER_AVAILABLE;
import static ru.mvideo.handoveroptionavailability.service.external.objectspublic.ObjectPublicConstants.TIME_ZONE;
import static ru.mvideo.handoveroptionavailability.service.external.objectspublic.ObjectPublicConstants.WORK_SCHEDULE;

import java.time.LocalDate;
import java.time.LocalTime;
import java.time.ZoneId;
import java.util.Collection;
import java.util.Collections;
import java.util.Map;
import lombok.experimental.UtilityClass;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.collections4.ListUtils;
import org.apache.commons.lang3.BooleanUtils;
import ru.mvideo.handoveroptionavailability.processor.model.HandoverObject;
import ru.mvideo.oi.pbl.model.ObjectResponseData;

@UtilityClass
public class ObjectsPublicUtils {

	private static final String START = "start";
	private static final String END = "end";
	private static final String DAY = "day";
	private static final String DAY_OFF = "dayOff";
	private static final String WORKING_HOURS = "workingHours";


	public void enrichObjectsPublicInfo(ObjectResponseData objectData, HandoverObject object) {
		if (objectData == null) {
			return;
		}
		for (var attributeData : ListUtils.emptyIfNull(objectData.getAttributes())) {
			if (TIME_ZONE.equals(attributeData.getAttribute())) {
				var zoneId = ZoneId.of(attributeData.getValue());
				object.setTimeZone(zoneId);
			}
			if (IS_AUTO_COURIER_AVAILABLE.equals(attributeData.getAttribute())) {
				var isAutoCourierAvailable = BooleanUtils.toBoolean(attributeData.getValue());
				object.setIsAutoCourierAvailable(isAutoCourierAvailable);
			}
		}
		var deliveryHours = getDeliveryWorkingHours(objectData);
		if (deliveryHours.containsKey(START)) {
			object.setDeliveryStartTime(LocalTime.parse(deliveryHours.get(START)));
		}
		if (deliveryHours.containsKey(END)) {
			object.setDeliveryEndTime(LocalTime.parse(deliveryHours.get(END)));
		}
		var workingHours = getWorkingHours(objectData);
		if (workingHours.containsKey(START)) {
			object.setWorkStartTime(LocalTime.parse(workingHours.get(START)));
		}
		if (workingHours.containsKey(END)) {
			object.setWorkEndTime(LocalTime.parse(workingHours.get(END)));
		}
	}

	public Map<String, String> getDeliveryWorkingHours(ObjectResponseData objectData) {
		return getSchedules(objectData, DELIVERY_SCHEDULE);
	}

	public Map<String, String> getWorkingHours(ObjectResponseData objectData) {
		return getSchedules(objectData, WORK_SCHEDULE);
	}

	private Map<String, String> getSchedules(ObjectResponseData objectData, String fieldName) {
		if (CollectionUtils.isNotEmpty(objectData.getSchedules())) {
			var now = LocalDate.now().getDayOfWeek().name();
			for (var objectScheduleData : objectData.getSchedules()) {
				if (fieldName.equals(objectScheduleData.getScheduleId())) {
					var deliveryScheduleValue = (Collection<Map<String, Object>>) objectScheduleData.getValue();
					for (var days : deliveryScheduleValue) {
						if (now.equals(days.get(DAY))) {
							if ((Boolean) days.getOrDefault(DAY_OFF, Boolean.FALSE)) {
								return Collections.emptyMap();
							}
							return (Map<String, String>) days.getOrDefault(WORKING_HOURS, Collections.emptyMap());
						}
					}
				}
			}
		}
		return Collections.emptyMap();
	}
}
