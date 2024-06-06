package ru.mvideo.handoveroptionavailability.config.bpp;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.config.BeanPostProcessor;
import org.springframework.stereotype.Component;
import ru.mvideo.handoveroptionavailability.service.external.objectspublic.kafka.model.ObjectScheduleDataMixIn;
import ru.mvideo.oi.pbl.model.ObjectScheduleData;

@Component
public class KafkaObjectMapperBeanPostProcessor implements BeanPostProcessor {

	@Override
	public Object postProcessAfterInitialization(Object bean, String beanName) throws BeansException {
		if ("kafkaObjectMapper".equals(beanName)) {
			ObjectMapper kafkaObjectMapper = (ObjectMapper) bean;
			kafkaObjectMapper.addMixIn(ObjectScheduleData.class, ObjectScheduleDataMixIn.class);
		}
		return bean;
	}
}
