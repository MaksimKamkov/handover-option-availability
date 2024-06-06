package ru.mvideo.handoveroptionavailability.model.external.msp.logistic.responsenextcalendar;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import ru.mvideo.handoveroptionavailability.model.external.msp.logistic.response.ResponseHeader;

@Data
public class MspResponseNextCalendar {

	@JsonProperty("ResponseHeader")
	private ResponseHeader responseHeader;

	@JsonProperty("ResponseBody")
	private ResponseBodyNextCalendar responseBody;
}