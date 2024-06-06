package ru.mvideo.handoveroptionavailability.config;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.MapperFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.databind.module.SimpleModule;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.codec.ServerCodecConfigurer;
import org.springframework.http.codec.json.Jackson2JsonDecoder;
import org.springframework.http.codec.json.Jackson2JsonEncoder;
import org.springframework.web.reactive.config.WebFluxConfigurer;
import ru.mvideo.handoveroptionavailability.model.HandoverOption;
import ru.mvideo.handoveroptionavailability.utils.HandoverOptionDeserializer;
import ru.mvideo.handoveroptionavailability.utils.HandoverOptionSerializer;

/**
 * Jackson databind {@link ObjectMapper} configuration.
 */
@Configuration
public class ObjectMapperConfig implements WebFluxConfigurer {

	private ObjectMapper objectMapper() {
		return new ObjectMapper()
				.registerModule(new JavaTimeModule())
				.registerModule(handoverOptionModule())
				.setSerializationInclusion(JsonInclude.Include.NON_NULL)
				.setSerializationInclusion(JsonInclude.Include.NON_EMPTY)
				.enable(DeserializationFeature.READ_ENUMS_USING_TO_STRING)
				.enable(SerializationFeature.WRITE_ENUMS_USING_TO_STRING)
				.enable(MapperFeature.ACCEPT_CASE_INSENSITIVE_ENUMS)
				.disable(SerializationFeature.FAIL_ON_EMPTY_BEANS)
				.disable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES)
				.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS)
				.disable(SerializationFeature.WRITE_DATE_TIMESTAMPS_AS_NANOSECONDS);
	}

	private SimpleModule handoverOptionModule() {
		return new SimpleModule()
				.addSerializer(HandoverOption.class, new HandoverOptionSerializer())
				.addDeserializer(HandoverOption.class, new HandoverOptionDeserializer());
	}

	@Override
	public void configureHttpMessageCodecs(ServerCodecConfigurer configurer) {
		configurer.defaultCodecs().jackson2JsonEncoder(new Jackson2JsonEncoder(objectMapper()));
		configurer.defaultCodecs().jackson2JsonDecoder(new Jackson2JsonDecoder(objectMapper()));
	}
}
