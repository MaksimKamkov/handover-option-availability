package ru.mvideo.handoveroptionavailability.mapper;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.mapstruct.factory.Mappers;
import ru.mvideo.handoveroptionavailability.service.external.objectspublic.ObjectPublicConstants;
import ru.mvideo.handoveroptionavailability.service.external.objectspublic.kafka.model.ExtendedObjectResponseData;
import ru.mvideo.oi.pbl.model.ObjectAttributeData;
import ru.mvideo.oi.pbl.model.ObjectResponseData;
import ru.mvideo.oi.pbl.model.ObjectScheduleData;

class ObjectResponseDataMapperTest {

	private final ObjectResponseDataMapper mapper = Mappers.getMapper(ObjectResponseDataMapper.class);

	@Test
	void testToObjectResponseData() {
		List<ObjectScheduleData> expectedSchedules = List.of(
				ObjectScheduleData.builder()
						.scheduleId("deliverySchedule")
						.value("val1")
						.build(),
				ObjectScheduleData.builder()
						.scheduleId("workSchedule")
						.value("val2")
						.build()
		);
		ArrayList<ObjectScheduleData> schedules = new ArrayList<>(expectedSchedules);
		schedules.add(
				ObjectScheduleData.builder()
						.scheduleId("unexpected")
						.value("val3")
						.build()
		);
		ObjectAttributeData expectedAttribute = ObjectAttributeData.builder()
				.attribute("longitude")
				.value("attr1")
				.groupId("groupId1")
				.type("type1")
				.build();
		List<ObjectAttributeData> attributes = List.of(
				ObjectAttributeData.builder()
						.attribute("longitude")
						.value("attr1")
						.groupId("groupId1")
						.type("type1")
						.build(),
				ObjectAttributeData.builder()
						.attribute("unrecognized")
						.value("attr2")
						.groupId("groupId2")
						.type("type2")
						.build()
		);
		ExtendedObjectResponseData expected = new ExtendedObjectResponseData();
		expected.objectId("id");
		expected.objectStatus("status");
		expected.setGeneralType("type");
		expected.attributes(attributes);
		expected.schedules(schedules);

		ObjectResponseData actual = mapper.toObjectResponseData(expected, ObjectPublicConstants.REQUIRED_ATTRIBUTES);

		assertNotNull(actual);
		assertEquals(expected.getObjectId(), actual.getObjectId());
		assertEquals(expected.getObjectStatus(), actual.getObjectStatus());
		assertAttributes(Collections.singletonList(expectedAttribute), actual.getAttributes());
		assertSchedules(expectedSchedules, actual.getSchedules());
	}


	private void assertAttributes(List<ObjectAttributeData> expected, List<ObjectAttributeData> actual) {
		assertNotNull(actual);
		assertEquals(expected.size(), actual.size());
		for (int i = 0; i < expected.size(); i++) {
			ObjectAttributeData expectedAttribute = expected.get(i);
			ObjectAttributeData actualAttribute = actual.get(i);
			assertEquals(expectedAttribute.getAttribute(), actualAttribute.getAttribute());
			assertEquals(expectedAttribute.getValue(), actualAttribute.getValue());
			assertEquals(expectedAttribute.getGroupId(), actualAttribute.getGroupId());
			assertEquals(expectedAttribute.getType(), actualAttribute.getType());
		}
	}

	private void assertSchedules(List<ObjectScheduleData> expected, List<ObjectScheduleData> actual) {
		assertNotNull(actual);
		assertEquals(expected.size(), actual.size());
		for (int i = 0; i < expected.size(); i++) {
			ObjectScheduleData expectedSchedules = expected.get(i);
			ObjectScheduleData actualSchedules = actual.get(i);
			assertEquals(expectedSchedules.getScheduleId(), actualSchedules.getScheduleId());
			assertEquals(expectedSchedules.getValue(), actualSchedules.getValue());
		}
	}
}