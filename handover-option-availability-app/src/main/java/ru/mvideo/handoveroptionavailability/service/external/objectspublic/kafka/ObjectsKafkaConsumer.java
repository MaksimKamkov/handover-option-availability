package ru.mvideo.handoveroptionavailability.service.external.objectspublic.kafka;

import org.springframework.stereotype.Component;
import ru.mvideo.dataflow.config.properties.KafkaConsumerProperties;
import ru.mvideo.dataflow.consumer.KafkaAutoAcknowledgeConsumer;
import ru.mvideo.handoveroptionavailability.config.annotation.condition.ConditionalOnObjectsCacheEnabled;
import ru.mvideo.handoveroptionavailability.service.external.objectspublic.kafka.model.ExtendedObjectResponseData;

@Component
@ConditionalOnObjectsCacheEnabled
public class ObjectsKafkaConsumer extends KafkaAutoAcknowledgeConsumer<ExtendedObjectResponseData> {

	public ObjectsKafkaConsumer(KafkaConsumerProperties properties) {
		super(properties, ExtendedObjectResponseData.class);
	}
}
