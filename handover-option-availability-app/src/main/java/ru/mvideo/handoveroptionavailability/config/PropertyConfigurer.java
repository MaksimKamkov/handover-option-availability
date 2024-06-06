package ru.mvideo.handoveroptionavailability.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import ru.mvideo.dataflow.config.properties.KafkaConsumerProperties;

@Configuration
public class PropertyConfigurer {

	@Bean
	@ConfigurationProperties("handover-option-availability.config.seamless")
	public DeliveryTimeProperties seamlessProperties() {
		return new DeliveryTimeProperties();
	}

	@Bean
	@ConfigurationProperties("handover-option-availability.config.exactly-time-delivery")
	public DeliveryTimeProperties exactlyTimeDeliveryProperties() {
		return new DeliveryTimeProperties();
	}

	@Bean("objectsKafkaConsumerProperties")
	@ConfigurationProperties("handover-option-availability.config.kafka.objects-consumer")
	public KafkaConsumerProperties objectsKafkaConsumerProperties() {
		return new KafkaConsumerProperties();
	}
}
