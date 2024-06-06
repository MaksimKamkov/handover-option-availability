package ru.mvideo.handoveroptionavailability.service.external.objectspublic.kafka;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Stream;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.util.Pair;
import reactor.core.publisher.Flux;
import ru.mvideo.dataflow.model.KafkaAutoAckRecord;
import ru.mvideo.handoveroptionavailability.service.external.objectspublic.kafka.model.ExtendedObjectResponseData;
import ru.mvideo.oi.pbl.model.ObjectAttributeData;
import ru.mvideo.oi.pbl.model.ObjectResponseData;
import ru.mvideo.oi.pbl.model.ObjectScheduleData;

@ExtendWith(MockitoExtension.class)
class KafkaObjectLocalCacheTest {

	@Mock
	private ObjectsKafkaConsumer consumer;

	@Mock
	private Function<KafkaAutoAckRecord<ExtendedObjectResponseData>, Pair<String, ObjectResponseData>> fn;

	@InjectMocks
	private KafkaObjectLocalCache cache;

	public static Stream<List<String>> extractedKeys() {
		return Stream.of(
				Collections.singletonList("test"),
				List.of("test", "aaaa", "bbbb")
		);
	}

	@AfterEach
	public void shutdown() {
		cache.destroy();
	}

	@Test
	void testGet() {
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
		List<ObjectAttributeData> attributes = List.of(
				ObjectAttributeData.builder()
						.attribute("longitude")
						.value("attr1")
						.groupId("groupId1")
						.type("type1")
						.build()
		);
		ExtendedObjectResponseData expected = new ExtendedObjectResponseData();
		expected.objectId("id");
		expected.objectStatus("OPEN");
		expected.setGeneralType("SHOP");
		expected.attributes(attributes);
		expected.schedules(expectedSchedules);

		when(consumer.getData()).thenReturn(Flux.just(new KafkaAutoAckRecord<>("test", expected)));
		when(fn.apply(any())).thenReturn(Pair.of("test", expected));
		cache.processObjectData();

		ObjectResponseData actual = cache.get("test");

		assertNotNull(actual);
		assertEquals(expected, actual);
	}

	@Test
	void testGetNullable() {
		when(consumer.getData()).thenReturn(Flux.empty());
		cache.processObjectData();

		ObjectResponseData actual = cache.get("test");

		assertNull(actual);
	}

	@ParameterizedTest
	@MethodSource("extractedKeys")
	void testGetAll(List<String> extractedKeys) {
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
		List<ObjectAttributeData> attributes = List.of(
				ObjectAttributeData.builder()
						.attribute("longitude")
						.value("attr1")
						.groupId("groupId1")
						.type("type1")
						.build()
		);
		ExtendedObjectResponseData expected = new ExtendedObjectResponseData();
		expected.objectId("id");
		expected.objectStatus("OPEN");
		expected.setGeneralType("SHOP");
		expected.attributes(attributes);
		expected.schedules(expectedSchedules);

		when(consumer.getData()).thenReturn(Flux.just(new KafkaAutoAckRecord<>("test", expected)));
		when(fn.apply(any())).thenReturn(Pair.of("test", expected));
		cache.processObjectData();

		Map<String, ObjectResponseData> actual = cache.getAll(extractedKeys);

		assertNotNull(actual);
		assertEquals(1, actual.size());
		assertEquals(expected, actual.get("test"));
	}
}