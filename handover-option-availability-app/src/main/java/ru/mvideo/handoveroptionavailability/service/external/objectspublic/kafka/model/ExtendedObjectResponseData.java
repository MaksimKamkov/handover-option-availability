package ru.mvideo.handoveroptionavailability.service.external.objectspublic.kafka.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import lombok.EqualsAndHashCode;
import ru.mvideo.oi.pbl.model.ObjectResponseData;

@Data
@EqualsAndHashCode(callSuper = true)
public class ExtendedObjectResponseData extends ObjectResponseData {
	@JsonProperty("generalType")
	private String generalType;
}
