package ru.mvideo.handoveroptionavailability.model.external.msp.logistic.response;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

@Data
public class MspResponse {

	@JsonProperty("ResponseHeader")
	private ResponseHeader responseHeader;

	@JsonProperty("ResponseBody")
	private ResponseBody responseBody;
}
