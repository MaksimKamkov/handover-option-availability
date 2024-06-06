package ru.mvideo.handoveroptionavailability.config;

import static ru.mvideo.handoveroptionavailability.service.external.objectspublic.ObjectPublicConstants.REQUIRED_ATTRIBUTES;

import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.util.Pair;
import ru.mvideo.handoveroptionavailability.config.annotation.condition.ConditionalOnObjectsCacheEnabled;
import ru.mvideo.handoveroptionavailability.mapper.ObjectResponseDataMapper;
import ru.mvideo.handoveroptionavailability.service.external.objectspublic.ObjectsPublicClientCacheableServiceImpl;
import ru.mvideo.handoveroptionavailability.service.external.objectspublic.ObjectsPublicClientService;
import ru.mvideo.handoveroptionavailability.service.external.objectspublic.ObjectsPublicClientServiceImpl;
import ru.mvideo.handoveroptionavailability.service.external.objectspublic.kafka.KafkaObjectLocalCache;
import ru.mvideo.handoveroptionavailability.service.external.objectspublic.kafka.ObjectsKafkaConsumer;
import ru.mvideo.oi.pbl.api.client.ObjectsPublicApiV2;
import ru.mvideo.oi.pbl.model.ObjectResponseData;

@Configuration
public class ObjectsPublicClientConfig {

	@Bean
	@ConditionalOnObjectsCacheEnabled
	public KafkaObjectLocalCache kafkaObjectLocalCache(ObjectsKafkaConsumer consumer, ObjectResponseDataMapper mapper) {
		return new KafkaObjectLocalCache(consumer, data -> {
			ObjectResponseData cachedObject = mapper.toObjectResponseData(data.getMessageValue(), REQUIRED_ATTRIBUTES);
			return Pair.of(data.getMessageKey(), cachedObject);
		});
	}

	@Bean
	@ConditionalOnObjectsCacheEnabled
	public ObjectsPublicClientService objectsPublicClientCacheableService(KafkaObjectLocalCache kafkaObjectLocalCache) {
		return new ObjectsPublicClientCacheableServiceImpl(kafkaObjectLocalCache);
	}

	@Bean
	@ConditionalOnMissingBean(ObjectsPublicClientService.class)
	public ObjectsPublicClientService defaultObjectsPublicClientService(ObjectsPublicApiV2 client) {
		return new ObjectsPublicClientServiceImpl(client);
	}
}
