package ru.mvideo.handoveroptionavailability.model.external.msp.logistic.request;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Builder;
import lombok.Data;
import lombok.ToString;

@ToString
@Builder
@Data
public class MspRequest {

	@JsonProperty("RequestBody")
	private RequestBody requestBody;
}
