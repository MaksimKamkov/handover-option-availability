package ru.mvideo.handoveroptionavailability.service.external.objectspublic;

import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import reactor.test.StepVerifier;
import ru.mvideo.handoveroptionavailability.service.external.objectspublic.kafka.KafkaObjectLocalCache;
import ru.mvideo.lards.geospatial.model.GeoPoint;
import ru.mvideo.oi.pbl.model.ObjectAttributeData;
import ru.mvideo.oi.pbl.model.ObjectResponseData;
import ru.mvideo.oi.pbl.model.ObjectScheduleData;

@ExtendWith(MockitoExtension.class)
class ObjectsPublicClientCacheableServiceImplTest {

	@Mock
	private KafkaObjectLocalCache kafkaObjectLocalCache;

	@InjectMocks
	private ObjectsPublicClientCacheableServiceImpl service;

	@Test
	void getCoordinateHandoverObject() {
		ObjectResponseData responseData = createResponse();
		GeoPoint expected = new GeoPoint(1.0, 2.0);

		when(kafkaObjectLocalCache.get(anyString())).thenReturn(responseData);

		StepVerifier.create(service.getCoordinateHandoverObject("test"))
				.expectNext(expected)
				.verifyComplete();
	}

	@Test
	void getHandoverObjectsDetails() {
		ObjectResponseData response = createResponse();

		when(kafkaObjectLocalCache.getAll(anyList())).thenReturn(Map.of("id", response));

		StepVerifier.create(service.getHandoverObjectsDetails(Collections.singletonList("id")))
				.expectNext(response)
				.verifyComplete();
	}


	private ObjectResponseData createResponse() {
		return ObjectResponseData.builder()
				.objectId("id")
				.objectStatus("status")
				.schedules(List.of(
						ObjectScheduleData.builder()
								.scheduleId("deliverySchedule")
								.value("val1")
								.build(),
						ObjectScheduleData.builder()
								.scheduleId("workSchedule")
								.value("val2")
								.build()
				))
				.attributes(List.of(
						ObjectAttributeData.builder()
								.attribute("longitude")
								.value("2.0")
								.groupId("groupId1")
								.type("type1")
								.build(),
						ObjectAttributeData.builder()
								.attribute("latitude")
								.value("1.0")
								.groupId("groupId2")
								.type("type2")
								.build()
				))
				.build();
	}
}