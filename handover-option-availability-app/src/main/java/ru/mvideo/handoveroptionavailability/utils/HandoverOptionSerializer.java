package ru.mvideo.handoveroptionavailability.utils;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializerProvider;
import java.io.IOException;
import ru.mvideo.handoveroptionavailability.model.HandoverOption;

public class HandoverOptionSerializer extends JsonSerializer<HandoverOption> {

	public HandoverOptionSerializer() {
	}

	@Override
	public void serialize(HandoverOption handoverOption, JsonGenerator jsonGenerator,
	                      SerializerProvider serializerProvider) throws IOException {
		jsonGenerator.writeString(handoverOption.getValue());
	}
}