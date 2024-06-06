package ru.mvideo.handoveroptionavailability.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import ru.mvideo.lib.client.LogHelper;

@Configuration
public class LogHelperConfig {

	@Bean
	public LogHelper clientLogHelper(ObjectMapper objectMapper) {
		return new LogHelper(objectMapper);
	}
}
