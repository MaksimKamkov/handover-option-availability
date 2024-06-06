package ru.mvideo.handoveroptionavailability.service.external.objectspublic.kafka;

import static ru.mvideo.handoveroptionavailability.service.external.objectspublic.ObjectPublicConstants.GENERAL_TYPE;
import static ru.mvideo.handoveroptionavailability.service.external.objectspublic.ObjectPublicConstants.OBJECT_STATUS;

import java.util.function.Function;
import org.springframework.data.util.Pair;
import ru.mvideo.dataflow.model.KafkaAutoAckRecord;
import ru.mvideo.handoveroptionavailability.cache.KafkaLocalCache;
import ru.mvideo.handoveroptionavailability.service.external.objectspublic.kafka.model.ExtendedObjectResponseData;
import ru.mvideo.oi.pbl.model.ObjectResponseData;

public class KafkaObjectLocalCache extends KafkaLocalCache<ExtendedObjectResponseData, String, ObjectResponseData> {

	public KafkaObjectLocalCache(ObjectsKafkaConsumer consumer,
	                             Function<KafkaAutoAckRecord<ExtendedObjectResponseData>, Pair<String, ObjectResponseData>> dataTransformFn) {
		super(consumer, dataTransformFn);
	}

	protected boolean isDataApplicable(KafkaAutoAckRecord<ExtendedObjectResponseData> record) {
		ExtendedObjectResponseData data = record.getMessageValue();
		return OBJECT_STATUS.equals(data.getObjectStatus()) && GENERAL_TYPE.equals(data.getGeneralType());
	}
}
