package ru.mvideo.handoveroptionavailability.utils;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializerProvider;
import java.io.IOException;
import java.time.LocalDateTime;
import java.time.ZoneId;

public class UnixTimeLocalDateTimeSerializer extends JsonSerializer<LocalDateTime> {

	@Override
	public void serialize(LocalDateTime localDateTime, JsonGenerator jsonGenerator,
	                      SerializerProvider serializerProvider) throws IOException {
		jsonGenerator.writeNumber(
				localDateTime.atZone(ZoneId.systemDefault()).toInstant().toEpochMilli());
	}
}