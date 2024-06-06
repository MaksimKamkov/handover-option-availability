package ru.mvideo.handoveroptionavailability.utils;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import java.io.IOException;
import ru.mvideo.handoveroptionavailability.model.HandoverOption;

public class HandoverOptionDeserializer extends JsonDeserializer<HandoverOption> {

	public HandoverOptionDeserializer() {
	}

	@Override
	public HandoverOption deserialize(JsonParser jsonParser, DeserializationContext deserializationContext) throws IOException {
		if (jsonParser == null) {
			return null;
		} else {
			String value = jsonParser.getValueAsString();
			return HandoverOption.fromValue(value);
		}
	}
}