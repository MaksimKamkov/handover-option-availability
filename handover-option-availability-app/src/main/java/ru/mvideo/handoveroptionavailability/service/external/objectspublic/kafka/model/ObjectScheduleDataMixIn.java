package ru.mvideo.handoveroptionavailability.service.external.objectspublic.kafka.model;

import com.fasterxml.jackson.annotation.JsonAlias;
import com.fasterxml.jackson.annotation.JsonIgnoreType;
import com.fasterxml.jackson.annotation.JsonProperty;

@JsonIgnoreType
public abstract class ObjectScheduleDataMixIn {
	@JsonProperty("scheduleId")
	@JsonAlias("attribute")
	String scheduleId;
}
